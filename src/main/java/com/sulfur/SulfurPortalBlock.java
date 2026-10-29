package com.sulfur;

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
                // Collision callbacks run every tick, so guard before doing chunk work.
                entity.setPortalCooldown(80);
                BlockPos landing = findSurface(destination, pos, !returningToOverworld);
                if (landing != null) {
                    ensurePairedPortal(destination, landing);
                    TeleportTransition transition = new TeleportTransition(
                            destination,
                            new Vec3(landing.getX() + 0.5, landing.getY(), landing.getZ() + 0.5),
                            Vec3.ZERO,
                            entity.getYRot(), entity.getXRot(), TeleportTransition.DO_NOTHING);
                    Entity teleported = entity.teleport(transition);
                    if (teleported != null) teleported.setPortalCooldown(80);
                }
            }
        }
        super.entityInside(state, level, pos, entity, effects, suffocates);
    }

    private static void ensurePairedPortal(ServerLevel level, BlockPos landing) {
        BlockPos origin = landing.offset(-1, -1, 0);
        // Preserve existing terrain and fluids; only fill actual air blocks.
        for (int w = 0; w < 4; w++) {
            for (int h = 0; h < 5; h++) {
                BlockPos target = origin.offset(w, h, 0);
                boolean frame = w == 0 || w == 3 || h == 0 || h == 4;
                BlockState existing = level.getBlockState(target);
                if (frame) {
                    if (existing.isAir()) {
                        level.setBlock(target, Blocks.SULFUR.defaultBlockState(), 2);
                    }
                } else if (existing.isAir()) {
                    level.setBlock(target, SulfurDimension.SULFUR_PORTAL.defaultBlockState()
                            .setValue(BlockStateProperties.AXIS, Direction.Axis.Z), 2);
                }
            }
        }
    }

    /** Find safe terrain near the source portal, preferring sulfur only when entering that dimension. */
    private static BlockPos findSurface(ServerLevel level, BlockPos sourcePortal, boolean preferSulfur) {
        int centerChunkX = sourcePortal.getX() >> 4;
        int centerChunkZ = sourcePortal.getZ() >> 4;
        BlockPos fallback = null;
        for (PortalSearchPolicy.ChunkOffset offset : PortalSearchPolicy.searchOffsets()) {
            int chunkX = centerChunkX + offset.x();
            int chunkZ = centerChunkZ + offset.z();
            // The shared plan has a strict 25-chunk ceiling; never scan from world origin.
            level.getChunkAt(new BlockPos(chunkX << 4, level.getMinY(), chunkZ << 4));
            int x = (chunkX << 4) + 8;
            int z = (chunkZ << 4) + 8;
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
            if (y <= level.getMinY() + 1 || y + 3 > level.getMaxY()) continue;
            BlockState floor = level.getBlockState(new BlockPos(x, y - 1, z));
            if (floor.isAir() || !floor.getFluidState().isEmpty()
                    || floor.is(SulfurDimension.ACID_WATER)
                    || !clearLandingSpace(level, x, y, z)) continue;
            BlockPos candidate = new BlockPos(x, y, z);
            if (fallback == null) fallback = candidate;
            if (PortalSearchPolicy.isPreferred(preferSulfur, floor.is(Blocks.SULFUR))) return candidate;
        }
        // Controlled fallback to a safe dry surface; no terrain excavation or artificial pad.
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
