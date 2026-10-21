# Official Minecraft 26.3 sulfur findings

Verified against the locally cached Minecraft 26.3 official game data and Mojang’s official articles.

- Official mob entity ID: `minecraft:sulfur_cube`, class `net.minecraft.world.entity.monster.cubemob.SulfurCube`.
- Official biome ID: `minecraft:sulfur_caves`, with sulfur cube natural spawning and vanilla cave-spawn entries.
- Official sulfur blocks/resources include the registered sulfur and cinnabar building set, `minecraft:potent_sulfur`, sulfur spikes, and vanilla loot/recipes.
- Official placed features used by vanilla Sulfur Caves include `minecraft:sulfur_pool`, `minecraft:rooted_sulfur_spring`, `minecraft:sulfur_spike_cluster`, and `minecraft:sulfur_spike`.
- The vanilla 26.3 `data/minecraft/worldgen/biome/sulfur_caves.json` uses array carvers, 12 generation feature stages, `minecraft:gameplay/natural_mob_spawns`, visual attributes, and `minecraft:sulfur_cube` rather than a mod-defined entity.

Sources:
- https://www.minecraft.net/en-us/updates/chaos-cubed-drop
- https://www.minecraft.net/en-us/article/minecraft-26-2-snapshot-1

## Nausea mechanic

Minecraft 26.3 does not apply Nausea merely for entering the `minecraft:sulfur_caves` biome. The official `PotentSulfurBlockEntity` applies `MobEffects.NAUSEA` for 80 ticks to living entities near reachable noxious gas. The effect check uses the entity eye position, a maximum squared distance of 9 (three blocks), and a vertical gas path through water/passable blocks above the Potent Sulfur source. The official server ticker repeats the application while the gas source is active. The mod now uses the official biome and official block entity, so this behavior comes from vanilla; its mixin only removes the effect for players wearing the mod Respirator Helmet. Custom Acid Water follows the same respirator exception.

## Chunk-loading diagnosis and lab fix

Fabric API 26.3 documents `ServerChunkEvents.CHUNK_LOAD` as firing after a chunk is already in the level. Its `CHUNK_GENERATE` callback is also described as firing when a newly generated chunk is loaded into the server level, so neither callback is a safe place for this lab's repeated `setBlockAndUpdate` calls and entity insertion. The Fabric structure guidance recommends data-driven worldgen structures instead of ad-hoc lifecycle mutation, and Fabric API issue #654 documents how worldgen/structure work can leave worlds lagging or unable to save when generation state is disturbed.

This release removes both chunk callbacks. The lab is now generated from `ServerTickEvents.END_LEVEL_TICK`, only for already-loaded Overworld chunks within a player's nearby radius, and it explicitly checks `hasChunkAt` before writing. This avoids recursively forcing or mutating chunks during their generation/load lifecycle while retaining deterministic lab placement.

Sources:
- https://github.com/FabricMC/fabric/blob/26.3/fabric-lifecycle-events-v1/src/main/java/net/fabricmc/fabric/api/event/lifecycle/v1/ServerChunkEvents.java
- https://wiki.fabricmc.net/tutorial:event_index
- https://wiki.fabricmc.net/tutorial:structures_old
- https://github.com/FabricMC/fabric-api/issues/654
