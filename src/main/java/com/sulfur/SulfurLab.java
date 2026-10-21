package com.sulfur;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.minecraft.commands.Commands;

/** Deterministic, rare abandoned laboratory feature for the Overworld. */
public final class SulfurLab {
    private SulfurLab() {}

    public static void register() {
        // Do not mutate chunks from CHUNK_LOAD or CHUNK_GENERATE. Those callbacks are part of
        // the chunk lifecycle and can leave a world waiting on its own generation work.
        ServerTickEvents.END_LEVEL_TICK.register(SulfurLab::onWorldTick);
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(Commands.literal("locate")
                        .then(Commands.literal("abandoned_lab")
                                .executes(context -> locateNearest(context.getSource().getPlayerOrException())))));
    }

    private static void onWorldTick(ServerLevel level) {
        if (!level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD)
                || level.getGameTime() % 20L != 0L) {
            return;
        }
        for (ServerPlayer player : level.players()) {
            int playerChunkX = player.blockPosition().getX() >> 4;
            int playerChunkZ = player.blockPosition().getZ() >> 4;
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    tryGenerate(level, playerChunkX + dx, playerChunkZ + dz);
                }
            }
        }
    }

    private static boolean tryGenerate(ServerLevel level, int chunkX, int chunkZ) {
        long hash = (long) chunkX * 341873128712L ^ (long) chunkZ * 132897987541L ^ 0x5A17F00DL;
        if ((hash & 63L) != 7L) {
            return false;
        }
        int x = (chunkX << 4) + 8;
        int z = (chunkZ << 4) + 8;
        BlockPos center = new BlockPos(x, 0, z);
        // Never force-load a chunk from the generator. The player-radius check above should
        // already have loaded it, and this guard makes that contract explicit.
        if (!level.hasChunkAt(center)) {
            return false;
        }
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos floor = new BlockPos(x, y - 1, z);
        if (!isDrySite(level, floor, x, z)) {
            return false;
        }
        BlockPos chestPos = floor.offset(-3, 2, -1);
        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity) {
            return false;
        }
        buildLab(level, floor);
        fillChest(level, chestPos);
        // Spawn the persistent boss inside the room, on its floor, rather than at its edge.
        SulfurLabBoss.spawn(level, x + 1.5, y + 0.05, z + 1.5);
        return true;
    }

    private static int locateNearest(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        if (!level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD)) {
            player.sendSystemMessage(Component.literal("Abandoned Labs can only be located from the Overworld."));
            return 0;
        }
        int startX = player.blockPosition().getX() >> 4;
        int startZ = player.blockPosition().getZ() >> 4;
        // Inspect only the deterministic lab-chunk pattern. Do not force-load terrain while locating.
        for (int radius = 0; radius <= 128; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (radius != 0 && Math.abs(dx) != radius && Math.abs(dz) != radius) continue;
                    int cx = startX + dx;
                    int cz = startZ + dz;
                    long hash = (long) cx * 341873128712L ^ (long) cz * 132897987541L ^ 0x5A17F00DL;
                    if ((hash & 63L) != 7L) continue;
                    BlockPos center = new BlockPos((cx << 4) + 8, 0, (cz << 4) + 8);
                    level.getChunkAt(center);
                    int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, center.getX(), center.getZ());
                    BlockPos chest = center.offset(-3, surfaceY + 1, -1);
                    boolean alreadyBuilt = level.getBlockEntity(chest) instanceof ChestBlockEntity;
                    if (alreadyBuilt || tryGenerate(level, cx, cz)) {
                        player.sendSystemMessage(Component.literal("Nearest Abandoned Lab: X=" + center.getX()
                                + " Y=" + surfaceY + " Z=" + center.getZ()));
                        player.sendSystemMessage(Component.literal("Use /tp " + center.getX() + " " + surfaceY
                                + " " + center.getZ() + " to travel there."));
                        return 1;
                    }
                }
            }
        }
        player.sendSystemMessage(Component.literal("No generated Abandoned Lab found nearby. Explore new Overworld terrain; labs generate as you explore."));
        return 0;
    }

    private static boolean isDrySite(ServerLevel level, BlockPos floor, int x, int z) {
        BlockState ground = level.getBlockState(floor);
        if (ground.isAir() || !ground.getFluidState().isEmpty()) {
            return false;
        }
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                for (int dy = 1; dy <= 6; dy++) {
                    BlockState state = level.getBlockState(new BlockPos(x + dx, floor.getY() + dy, z + dz));
                    if (!state.isAir() || !state.getFluidState().isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static void buildLab(ServerLevel level, BlockPos floor) {
        int x0 = floor.getX();
        int y0 = floor.getY();
        int z0 = floor.getZ();
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                set(level, new BlockPos(x0 + dx, y0, z0 + dz), Blocks.STONE_BRICKS.defaultBlockState());
                for (int dy = 1; dy <= 5; dy++) {
                    boolean wall = dx == -4 || dx == 4 || dz == -3 || dz == 3;
                    if (wall) {
                        BlockState material = ((dx + dz + dy) & 3) == 0
                                ? Blocks.MOSSY_COBBLESTONE.defaultBlockState()
                                : Blocks.COBBLESTONE.defaultBlockState();
                        set(level, new BlockPos(x0 + dx, y0 + dy, z0 + dz), material);
                    } else {
                        set(level, new BlockPos(x0 + dx, y0 + dy, z0 + dz), Blocks.AIR.defaultBlockState());
                    }
                }
                set(level, new BlockPos(x0 + dx, y0 + 6, z0 + dz), Blocks.MOSSY_COBBLESTONE.defaultBlockState());
            }
        }

        // Broken sulfur portal in the rear of the lab. The missing pieces must be repaired.
        for (int w = 0; w < 4; w++) {
            for (int h = 0; h < 5; h++) {
                boolean frame = w == 0 || w == 3 || h == 0 || h == 4;
                boolean missing = (w == 3 && h == 4) || (w == 0 && h == 0) || (w == 3 && h == 1);
                if (frame && !missing) {
                    set(level, new BlockPos(x0 - 1 + w, y0 + 1 + h, z0 + 2), Blocks.SULFUR.defaultBlockState());
                }
            }
        }
        set(level, new BlockPos(x0 - 3, y0 + 2, z0 - 1), Blocks.CHEST.defaultBlockState()
                .setValue(ChestBlock.FACING, Direction.SOUTH));
        set(level, new BlockPos(x0 + 3, y0 + 2, z0 - 1), Blocks.IRON_BARS.defaultBlockState());
        set(level, new BlockPos(x0 + 2, y0 + 2, z0 - 1), Blocks.IRON_BARS.defaultBlockState());
        set(level, new BlockPos(x0, y0 + 1, z0 - 2), Blocks.CRAFTING_TABLE.defaultBlockState());
        set(level, new BlockPos(x0 + 1, y0 + 1, z0 - 2), Blocks.FURNACE.defaultBlockState());
        set(level, new BlockPos(x0 - 2, y0 + 5, z0), Blocks.LANTERN.defaultBlockState());
    }

    private static void fillChest(ServerLevel level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof ChestBlockEntity chest)) {
            return;
        }
        chest.setItem(0, new ItemStack(Items.SULFUR, 8));
        chest.setItem(1, new ItemStack(Items.SULFUR, 8));
        chest.setItem(2, new ItemStack(Items.CINNABAR, 4));
        chest.setItem(3, new ItemStack(Items.FLINT_AND_STEEL, 1));
        chest.setItem(4, new ItemStack(Items.IRON_INGOT, 3));
        chest.setItem(5, new ItemStack(Items.BREAD, 3));
        chest.setItem(6, new ItemStack(SulfurDimension.SULFUR_GEM, 2));
        chest.setItem(7, new ItemStack(SulfurDimension.RESPIRATOR_HELMET, 1));
        chest.setChanged();
    }

    private static void set(ServerLevel level, BlockPos pos, BlockState state) {
        level.setBlockAndUpdate(pos, state);
    }
}
