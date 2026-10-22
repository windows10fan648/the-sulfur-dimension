package com.sulfur;

import net.minecraft.core.BlockPos;
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
    private static final int SEARCH_RADIUS = 96;

    public SulfurPortalBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(BlockStateProperties.AXIS, net.minecraft.core.Direction.Axis.X));
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
            ServerLevel destination = source.getServer().getLevel(returningToOverworld ? Level.OVERWORLD : SulfurDimension.SULFUR_WORLD);
            if (destination != null) {
                BlockPos landing = findSurface(destination);
                ensurePairedPortal(destination, landing);
                TeleportTransition transition = new TeleportTransition(
                        destination,
                        new Vec3(landing.getX() + 0.5, landing.getY() + 1.0, landing.getZ() + 0.5),
                        Vec3.ZERO,
                        entity.getYRot(), entity.getXRot(), TeleportTransition.DO_NOTHING);
                Entity teleported = entity.teleport(transition);
                if (teleported != null) teleported.setPortalCooldown(80);
            }
        }
        super.entityInside(state, level, pos, entity, effects, suffocates);
    }

    private static void ensurePairedPortal(ServerLevel level, BlockPos landing) {
        BlockPos origin = landing.offset(-1, -1, 0);
        ensureLandingPlatform(level, origin);
        for (int w = 0; w < 4; w++) {
            for (int h = 0; h < 5; h++) {
                BlockPos target = origin.offset(w, h, 0);
                boolean frame = w == 0 || w == 3 || h == 0 || h == 4;
                BlockState state = frame ? Blocks.SULFUR.defaultBlockState()
                        : SulfurDimension.SULFUR_PORTAL.defaultBlockState().setValue(BlockStateProperties.AXIS, net.minecraft.core.Direction.Axis.Z);
                // Suppress neighbor-shape cascades: this is a fixed, validated landing structure.
                level.setBlock(target, state, 2);
            }
        }
    }

    private static void ensureLandingPlatform(ServerLevel level, BlockPos origin) {
        int floorY = origin.getY() - 1;
        for (int x = origin.getX() - 2; x <= origin.getX() + 5; x++) {
            for (int z = origin.getZ() - 2; z <= origin.getZ() + 2; z++) {
                BlockPos floor = new BlockPos(x, floorY, z);
                BlockState state = level.getBlockState(floor);
                if (state.isAir() || !state.getFluidState().isEmpty() || state.is(SulfurDimension.ACID_WATER)) {
                    level.setBlock(floor, Blocks.SULFUR.defaultBlockState(), 2);
                }
            }
        }
    }

    /** Prefer dry, sky-exposed land near the origin; never treat a cave roof as the surface. */
    private static BlockPos findSurface(ServerLevel level) {
        int centerX = 0;
        int centerZ = 0;
        for (int radius = 0; radius <= SEARCH_RADIUS; radius += 8) {
            for (int dx = -radius; dx <= radius; dx += 8) {
                for (int dz = -radius; dz <= radius; dz += 8) {
                    if (radius != 0 && Math.abs(dx) != radius && Math.abs(dz) != radius) continue;
                    int x = centerX + dx;
                    int z = centerZ + dz;
                    // MOTION_BLOCKING_NO_LEAVES can report the top of an underground cave.
                    // WORLD_SURFACE points at the actual topmost terrain instead.
                    int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
                    BlockPos ground = new BlockPos(x, y - 1, z);
                    BlockState floor = level.getBlockState(ground);
                    if (!floor.isAir() && floor.getFluidState().isEmpty() && !floor.is(SulfurDimension.ACID_WATER)
                            && level.canSeeSkyFromBelowWater(new BlockPos(x, y, z))
                            && clearLandingSpace(level, x, y, z)) {
                        return new BlockPos(x, y, z);
                    }
                }
            }
        }
        // Guaranteed dry fallback for unusual custom worldgen with no accessible land nearby.
        // Last resort: build a dry, open-air platform near the top of the build range.
        BlockPos fallback = new BlockPos(centerX, level.getMaxY() - 16, centerZ);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                level.setBlock(fallback.offset(dx, -1, dz), Blocks.SULFUR.defaultBlockState(), 2);
                for (int dy = 0; dy < 3; dy++) {
                    level.setBlock(fallback.offset(dx, dy, dz), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
        return fallback;
    }

    private static boolean clearLandingSpace(ServerLevel level, int x, int y, int z) {
        for (int dy = 0; dy < 3; dy++) {
            BlockState state = level.getBlockState(new BlockPos(x, y + dy, z));
            if (!state.isAir() || !state.getFluidState().isEmpty()) return false;
        }
        return true;
    }
}
