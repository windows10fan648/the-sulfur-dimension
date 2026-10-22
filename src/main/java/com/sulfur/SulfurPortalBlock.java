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
    private static final int SEARCH_RADIUS = 512;

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
                        new Vec3(landing.getX() + 0.5, landing.getY(), landing.getZ() + 0.5),
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

    /** Find exposed sulfur terrain, searching outward and skipping water-covered columns. */
    private static BlockPos findSurface(ServerLevel level) {
        int centerX = 0;
        int centerZ = 0;
        BlockPos dryFallback = null;
        for (int radius = 0; radius <= SEARCH_RADIUS; radius += 16) {
            for (int dx = -radius; dx <= radius; dx += 16) {
                for (int dz = -radius; dz <= radius; dz += 16) {
                    if (radius != 0 && Math.abs(dx) != radius && Math.abs(dz) != radius) continue;
                    int x = centerX + dx;
                    int z = centerZ + dz;
                    int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
                    BlockPos ground = new BlockPos(x, y - 1, z);
                    BlockState floor = level.getBlockState(ground);
                    if (!floor.isAir() && floor.getFluidState().isEmpty() && !floor.is(SulfurDimension.ACID_WATER)
                            && level.canSeeSkyFromBelowWater(new BlockPos(x, y, z))
                            && clearLandingSpace(level, x, y, z)) {
                        BlockPos candidate = new BlockPos(x, y, z);
                        if (dryFallback == null) dryFallback = candidate;
                        if (floor.is(Blocks.SULFUR)) return candidate;
                    }
                }
            }
        }
        // If generated terrain has no exposed sulfur, use the nearest dry surface we found and
        // turn its landing pad into sulfur instead of dropping the player onto a distant sky platform.
        if (dryFallback != null) return makeSulfurLandingPad(level, dryFallback);

        // Fully flooded or unusual custom terrain: make a solid pad at the origin's surface level.
        BlockPos fallback = new BlockPos(centerX, level.getHeight(Heightmap.Types.WORLD_SURFACE, centerX, centerZ), centerZ);
        return makeSulfurLandingPad(level, fallback);
    }

    private static BlockPos makeSulfurLandingPad(ServerLevel level, BlockPos landing) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                level.setBlock(landing.offset(dx, -1, dz), Blocks.SULFUR.defaultBlockState(), 2);
                for (int dy = 0; dy < 3; dy++) {
                    level.setBlock(landing.offset(dx, dy, dz), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
        return landing;
    }

    private static boolean clearLandingSpace(ServerLevel level, int x, int y, int z) {
        for (int dy = 0; dy < 3; dy++) {
            BlockState state = level.getBlockState(new BlockPos(x, y + dy, z));
            if (!state.isAir() || !state.getFluidState().isEmpty()) return false;
        }
        return true;
    }
}
