# The Sulfur Dimension

Fabric 26.3 / Java 25 mod for a toxic sulfur world. The project uses the official Minecraft 26.3 Mojang mappings and includes official Minecraft 26.3 Sulfur Caves, Sulfur Cube, sulfur/cinnabar block set, sulfur pools, springs, spikes, plus custom acid-water, respirator, and utility content. The dimension uses a fixed vanilla Sulfur Caves biome with the official worldgen and renderer.

## Build

On Windows:

```bat
gradlew.bat clean build --no-configuration-cache
```

On Linux/macOS:

```bash
export JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64
./gradlew clean build --no-configuration-cache
```

The compiled mod is written to `build/libs/the-sulfur-dimension-1.0.0.jar`. The repository policy in `settings.gradle` is intentionally `RepositoriesMode.PREFER_PROJECT`, which allows Fabric Loom to use its injected Minecraft/Mojang repositories.

## Design notes

The Java sources use 26.3 Mojang-mapped packages such as `net.minecraft.resources.Identifier`, `net.minecraft.core.Registry`, `net.minecraft.world.level.block.Block`, and `net.minecraft.world.item.Item`. The dimension is data-pack registered at `data/sulfur_dimension/dimension/sulfur.json`; its pale-lime fog and acid-green palette are defined by the sulfur biomes. `AcidWaterBlock` is intentionally a lightweight vanilla-style liquid hazard block rather than a full fluid API implementation, making it robust across the 26.3 API transition.

The environmental nausea hook uses the dimension key and respirator head slot. Direct acid contact and potent sulfur stepping deal damage once per second. Replace the included 16x16 color swatches with authored vanilla-inspired textures if distributing the mod.

## Vanilla sulfur integration

The dimension uses a private copy of the official Sulfur Caves biome and its own noise settings. This preserves the vanilla `minecraft:sulfur_cube`, sulfur pools, rooted sulfur springs, sulfur spikes, and sulfur/cinnabar generation bands while preventing Overworld stone, dirt, tuff, and ore features from appearing in this dimension. Its private spring features and templates replace vanilla tuff with sulfur. Sniffers can also spawn naturally as a rare creature. The vanilla Overworld biome and generator settings are unchanged. The mod no longer registers a duplicate Sulfur Slime or duplicate sulfur/cinnabar blocks.

Official references: [Chaos Cubed Drop](https://www.minecraft.net/en-us/updates/chaos-cubed-drop) and [Minecraft 26.2 Snapshot 1](https://www.minecraft.net/en-us/article/minecraft-26-2-snapshot-1).

## Sulfur portal

Build a vertical 4×5 Nether-style frame using standard `minecraft:sulfur` blocks: four frame blocks across the bottom, four across the top, and two frame blocks on each side. Leave a 2×3 air opening in the center, then light any frame block with Flint and Steel. The six interior blocks become the Sulfur Portal. The frame may be oriented along either horizontal axis.

Walking into the portal uses Minecraft 26.3 portal processing and transfers entities between the Overworld and `sulfur_dimension:sulfur`; the destination is the target dimension’s respawn position.

## Achievements

The mod adds 21 advancements: the root achievement “What is this place?” plus 20 follow-up milestones for entering the dimension, collecting sulfur and cinnabar materials, finding Potent Sulfur and spikes, preparing a respirator and acid-water bucket, crafting portal frames, and exploring with vanilla tools.
