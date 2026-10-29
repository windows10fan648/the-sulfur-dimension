package com.sulfur;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

/** Custom yellow-green portal interior connecting the Overworld and Sulfur Dimension. */
public final class SulfurPortalBlock extends Block {
    private static final int FAILED_TRAVEL_COOLDOWN = 20;
    private static final int SUCCESSFUL_TRAVEL_COOLDOWN = 80;
    private static final int PORTAL_INTERIOR_HEIGHT = 3;

    public SulfurPortalBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(BlockStateProperties.AXIS, Direction.Axis.X));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.AXIS);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                net.minecraft.world.entity.InsideBlockEffectApplier effects, boolean suffocates) {
        if (level instanceof ServerLevel source && !entity.isOnPortalCooldown()) {
            boolean returningToOverworld = source.dimension().equals(SulfurDimension.SULFUR_WORLD);
            ServerLevel destination = source.getServer().getLevel(
                    returningToOverworld ? Level.OVERWORLD : SulfurDimension.SULFUR_WORLD);
            if (destination != null) {
                // Short failure cooldown prevents per-tick chunk searches but permits prompt retry.
                entity.setPortalCooldown(FAILED_TRAVEL_COOLDOWN);
                PortalConstructionPolicy.Orientation orientation =
                        PortalConstructionPolicy.orientationForPortalAxis(
                                state.getValue(BlockStateProperties.AXIS) == Direction.Axis.X);
                Destination target = findDestination(destination, pos,
                        !returningToOverworld, entity.getBbHeight(), orientation);
                if (target != null && (target.existing()
                        || constructPortal(destination, target.origin(), target.orientation()))) {
                    BlockPos landing = landingPosition(target.origin(), target.orientation());
                    entity.setPortalCooldown(SUCCESSFUL_TRAVEL_COOLDOWN);
                    TeleportTransition transition = new TeleportTransition(
                            destination,
                            new Vec3(landing.getX() + 0.5, landing.getY(), landing.getZ() + 0.5),
                            Vec3.ZERO,
                            entity.getYRot(), entity.getXRot(), TeleportTransition.DO_NOTHING);
                    Entity teleported = entity.teleport(transition);
                    if (teleported != null) teleported.setPortalCooldown(SUCCESSFUL_TRAVEL_COOLDOWN);
                    else entity.setPortalCooldown(FAILED_TRAVEL_COOLDOWN);
                }
            }
        }
        super.entityInside(state, level, pos, entity, effects, suffocates);
    }

    /** Search for an existing complete portal first, then retain safe constructible candidates. */
    private static Destination findDestination(ServerLevel level, BlockPos sourcePortal,
                                               boolean preferSulfur, float entityHeight,
                                               PortalConstructionPolicy.Orientation preferredOrientation) {
        if (entityHeight > PORTAL_INTERIOR_HEIGHT) return null;
        int centerChunkX = sourcePortal.getX() >> 4;
        int centerChunkZ = sourcePortal.getZ() >> 4;
        List<Destination> candidates = new ArrayList<>();
        Destination reusable = null;
        long reusableDistance = Long.MAX_VALUE;

        for (PortalSearchPolicy.ChunkOffset chunkOffset : PortalSearchPolicy.searchOffsets()) {
            int chunkX = centerChunkX + chunkOffset.x();
            int chunkZ = centerChunkZ + chunkOffset.z();
            // Exactly one forced chunk load per search offset; total cannot exceed 25.
            level.getChunkAt(new BlockPos(chunkX << 4, level.getMinY(), chunkZ << 4));

            for (PortalSearchPolicy.ColumnOffset column : PortalSearchPolicy.columnsWithinChunk(
                    sourcePortal.getX() & 15, sourcePortal.getZ() & 15)) {
                int x = (chunkX << 4) + column.x();
                int z = (chunkZ << 4) + column.z();
                int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);

                // Existing portals can raise the sampled height. Check the small vertical band
                // around the local surface rather than scanning a chunk's entire height.
                int firstPortalY = Math.max(level.getMinY() + 1, surfaceY - 5);
                int lastPortalY = Math.min(level.getMaxY() - 5, surfaceY + 1);
                for (int baseY = firstPortalY; baseY <= lastPortalY; baseY++) {
                    for (PortalConstructionPolicy.Orientation orientation
                            : PortalConstructionPolicy.Orientation.values()) {
                        BlockPos origin = portalOrigin(x, z, baseY, orientation);
                        if (isCompletePortal(level, origin, orientation)) {
                            BlockPos landing = landingPosition(origin, orientation);
                            long dx = landing.getX() - (long) sourcePortal.getX();
                            long dy = landing.getY() - (long) sourcePortal.getY();
                            long dz = landing.getZ() - (long) sourcePortal.getZ();
                            long distance = dx * dx + dy * dy + dz * dz;
                            if (distance < reusableDistance) {
                                reusable = new Destination(origin, true, false, orientation);
                                reusableDistance = distance;
                            }
                        }
                    }
                }

                // The portal's bottom frame sits on a dry, sturdy surface; the player arrives
                // one block above it in the 3-block clear opening.
                if (entityHeight > PORTAL_INTERIOR_HEIGHT || surfaceY <= level.getMinY()
                        || surfaceY + 5 > level.getMaxY()) continue;
                BlockPos origin = portalOrigin(x, z, surfaceY, preferredOrientation);
                Optional<List<PortalConstructionPolicy.Placement>> plan =
                        buildPlan(level, origin, preferredOrientation);
                if (plan.isEmpty()) continue;

                BlockState floor = level.getBlockState(origin.below());
                candidates.add(new Destination(origin, false, floor.is(Blocks.SULFUR), preferredOrientation));
            }
        }

        if (reusable != null) return reusable;
        return PortalSearchPolicy.selectCandidate(candidates,
                candidate -> PortalSearchPolicy.isPreferred(preferSulfur, candidate.sulfurSurface()));
    }

    private static BlockPos portalOrigin(int interiorX, int interiorZ, int baseY,
                                         PortalConstructionPolicy.Orientation orientation) {
        return orientation == PortalConstructionPolicy.Orientation.WIDTH_X
                ? new BlockPos(interiorX - 1, baseY, interiorZ)
                : new BlockPos(interiorX, baseY, interiorZ - 1);
    }

    private static BlockPos landingPosition(BlockPos origin,
                                             PortalConstructionPolicy.Orientation orientation) {
        return orientation == PortalConstructionPolicy.Orientation.WIDTH_X
                ? origin.offset(1, 1, 0) : origin.offset(0, 1, 1);
    }

    private static Optional<List<PortalConstructionPolicy.Placement>> buildPlan(
            ServerLevel level, BlockPos origin, PortalConstructionPolicy.Orientation orientation) {
        return PortalConstructionPolicy.planBuild(
                offset -> classify(level.getBlockState(origin.offset(offset.x(), offset.y(), offset.z()))),
                offset -> isSafeSupport(level, origin.offset(offset.x(), offset.y(), offset.z())),
                orientation);
    }

    private static boolean isCompletePortal(ServerLevel level, BlockPos origin,
                                            PortalConstructionPolicy.Orientation orientation) {
        return PortalConstructionPolicy.isCompletePortal(
                        offset -> classify(level.getBlockState(origin.offset(offset.x(), offset.y(), offset.z()))),
                        orientation)
                && hasSafeSupport(level, origin, orientation);
    }

    private static PortalConstructionPolicy.ExistingCell classify(BlockState state) {
        if (state.isAir()) return PortalConstructionPolicy.ExistingCell.AIR;
        if (!state.getFluidState().isEmpty()) return PortalConstructionPolicy.ExistingCell.FLUID;
        if (state.is(Blocks.SULFUR)) return PortalConstructionPolicy.ExistingCell.SULFUR;
        if (state.is(SulfurDimension.SULFUR_PORTAL)) {
            return state.getValue(BlockStateProperties.AXIS) == Direction.Axis.Z
                    ? PortalConstructionPolicy.ExistingCell.PORTAL_Z
                    : PortalConstructionPolicy.ExistingCell.PORTAL_X;
        }
        return PortalConstructionPolicy.ExistingCell.OCCUPIED;
    }

    private static boolean hasSafeSupport(ServerLevel level, BlockPos origin,
                                          PortalConstructionPolicy.Orientation orientation) {
        for (int x = 0; x < 4; x++) {
            BlockPos support = orientation == PortalConstructionPolicy.Orientation.WIDTH_X
                    ? origin.offset(x, -1, 0) : origin.offset(0, -1, x);
            if (!isSafeSupport(level, support)) return false;
        }
        return true;
    }

    private static boolean isSafeSupport(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isFaceSturdy(level, pos, Direction.UP)
                && state.getFluidState().isEmpty()
                && !state.is(SulfurDimension.ACID_WATER)
                && !state.is(Blocks.MAGMA_BLOCK)
                && !state.is(Blocks.CACTUS)
                && !state.is(Blocks.CAMPFIRE)
                && !state.is(Blocks.SOUL_CAMPFIRE);
    }

    /** Preflight the whole structure, then apply it; roll back if any block write fails. */
    private static boolean constructPortal(ServerLevel level, BlockPos origin,
                                           PortalConstructionPolicy.Orientation orientation) {
        Optional<List<PortalConstructionPolicy.Placement>> planned = buildPlan(level, origin, orientation);
        if (planned.isEmpty()) return false;

        List<BlockPos> changed = new ArrayList<>();
        try {
            for (PortalConstructionPolicy.Placement placement : planned.get()) {
                PortalConstructionPolicy.Offset offset = placement.offset();
                BlockPos pos = origin.offset(offset.x(), offset.y(), offset.z());
                BlockState state = placement.kind() == PortalConstructionPolicy.PlacementKind.SULFUR_FRAME
                        ? Blocks.SULFUR.defaultBlockState()
                        : SulfurDimension.SULFUR_PORTAL.defaultBlockState()
                                .setValue(BlockStateProperties.AXIS,
                                        orientation == PortalConstructionPolicy.Orientation.WIDTH_X
                                                ? Direction.Axis.Z : Direction.Axis.X);
                // Record before writing so rollback also covers a write that throws mid-update.
                changed.add(pos);
                if (!level.setBlock(pos, state, 2)) {
                    rollbackConstruction(level, changed);
                    return false;
                }
            }
            return true;
        } catch (RuntimeException failure) {
            try {
                rollbackConstruction(level, changed);
            } catch (RuntimeException rollbackFailure) {
                failure.addSuppressed(rollbackFailure);
            }
            throw failure;
        }
    }

    private static void rollbackConstruction(ServerLevel level, List<BlockPos> changed) {
        for (int i = changed.size() - 1; i >= 0; i--) {
            BlockPos pos = changed.get(i);
            // The plan only ever replaces air, so rollback to air cannot erase pre-existing terrain.
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        }
    }

    private record Destination(BlockPos origin, boolean existing, boolean sulfurSurface,
                               PortalConstructionPolicy.Orientation orientation) {}
}
