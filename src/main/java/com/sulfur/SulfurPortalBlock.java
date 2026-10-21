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
                BlockPos surface = findSurface(destination);
                BlockPos spawn = surface.above();
                ensurePairedPortal(destination, spawn);
                TeleportTransition transition = new TeleportTransition(
                        destination,
                        new Vec3(spawn.getX() + 0.5, spawn.getY() + 1.0, spawn.getZ() + 0.5),
                        Vec3.ZERO,
                        entity.getYRot(),
                        entity.getXRot(),
                        TeleportTransition.DO_NOTHING);
                Entity teleported = entity.teleport(transition);
                if (teleported != null) {
                    teleported.setPortalCooldown(80);
                }
            }
        }
        super.entityInside(state, level, pos, entity, effects, suffocates);
    }

    private static void ensurePairedPortal(ServerLevel level, BlockPos center) {
        // A standard 4x5 frame in the X/Y plane, with the portal axis along Z.
        BlockPos origin = center.offset(-1, -1, 0);
        ensureLandingPlatform(level, origin);
        for (int w = 0; w < 4; w++) {
            for (int h = 0; h < 5; h++) {
                BlockPos target = origin.offset(w, h, 0);
                boolean frame = w == 0 || w == 3 || h == 0 || h == 4;
                if (frame) {
                    level.setBlockAndUpdate(target, Blocks.SULFUR.defaultBlockState());
                } else {
                    level.setBlockAndUpdate(target, SulfurDimension.SULFUR_PORTAL.defaultBlockState()
                            .setValue(BlockStateProperties.AXIS, net.minecraft.core.Direction.Axis.Z));
                }
            }
        }
    }

    /** Gives a liquid fallback solid footing instead of leaving the portal over open water. */
    private static void ensureLandingPlatform(ServerLevel level, BlockPos origin) {
        int floorY = origin.getY() - 1;
        for (int x = origin.getX() - 2; x <= origin.getX() + 5; x++) {
            for (int z = origin.getZ() - 2; z <= origin.getZ() + 2; z++) {
                BlockPos floor = new BlockPos(x, floorY, z);
                BlockState state = level.getBlockState(floor);
                if (state.isAir() || !state.getFluidState().isEmpty() || state.is(SulfurDimension.ACID_WATER)) {
                    level.setBlockAndUpdate(floor, Blocks.SULFUR.defaultBlockState());
                }
            }
        }
    }

    private static BlockPos findSurface(ServerLevel level) {
        int minY = level.getMinY();
        int maxY = level.getMaxY();
        BlockPos best = null;
        for (int radius = 0; radius <= 128 && best == null; radius += 4) {
            for (int x = -radius; x <= radius; x += radius == 0 ? 1 : 4) {
                for (int z = -radius; z <= radius; z += radius == 0 ? 1 : 4) {
                    int baseY = findSolidSurfaceY(level, x, z, minY, maxY);
                    if (baseY > minY + 2 && (best == null || baseY > best.getY())) {
                        best = new BlockPos(x, baseY, z);
                    }
                }
            }
        }
        if (best != null) {
            return best;
        }
        BlockPos aboveLiquid = findAirAboveLiquid(level, minY, maxY);
        return aboveLiquid != null ? aboveLiquid : new BlockPos(0, minY + 64, 0);
    }

    /** Returns the first air block above the highest ordinary solid block in a column. */
    private static int findSolidSurfaceY(ServerLevel level, int x, int z, int minY, int maxY) {
        for (int y = maxY - 1; y >= minY + 1; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(pos);
            if (!state.isAir()
                    && state.getFluidState().isEmpty()
                    && !state.is(SulfurDimension.ACID_WATER)
                    && !state.is(Blocks.BEDROCK)
                    && !state.getCollisionShape(level, pos).isEmpty()
                    && isDryPortalClear(level, x, z, y + 1, maxY)
                    && level.canSeeSkyFromBelowWater(new BlockPos(x, y + 1, z))) {
                return y + 1;
            }
        }
        return minY;
    }

    private static boolean isDryPortalClear(ServerLevel level, int x, int z, int startY, int maxY) {
        for (int y = startY; y < Math.min(startY + 4, maxY); y++) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(pos);
            if (!state.isAir() || !state.getFluidState().isEmpty() || state.is(SulfurDimension.ACID_WATER)) {
                return false;
            }
        }
        return true;
    }

    /** If the dimension is mostly liquid, place the portal in the first dry air column above a liquid surface. */
    private static BlockPos findAirAboveLiquid(ServerLevel level, int minY, int maxY) {
        for (int y = minY + 1; y < maxY - 5; y++) {
            BlockState below = level.getBlockState(new BlockPos(0, y, 0));
            if ((!below.getFluidState().isEmpty() || below.is(SulfurDimension.ACID_WATER))
                    && isDryPortalClear(level, 0, 0, y + 1, maxY)
                    && level.canSeeSkyFromBelowWater(new BlockPos(0, y + 1, 0))) {
                return new BlockPos(0, y + 1, 0);
            }
        }
        return null;
    }
}
