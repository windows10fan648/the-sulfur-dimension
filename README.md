# The Sulfur Dimension

A Fabric adventure mod that takes you to a hazardous world of sulfur caves, cinnabar, acid water, and strange life.

## Requirements

- Minecraft: **Java Edition 26.3**
- Java: **25 or newer**
- Fabric Loader: **0.19.5 or newer**
- Fabric API: **the build for Minecraft 26.3**

This is a Fabric mod; it is not built for Forge or NeoForge.

## Installation

1. Install Fabric Loader for Minecraft 26.3 and launch the game once.
2. Download and install Fabric API for Minecraft 26.3.
3. Put the mod JAR in your Minecraft `mods` folder alongside Fabric API.
4. Launch the Fabric 26.3 profile.

Back up existing worlds before installing or updating the mod. New world-generation settings affect newly generated chunks; existing terrain is not retroactively regenerated.

## Getting started

Build a vertical sulfur-block portal frame with an outer size of 4 blocks wide by 5 blocks high. The frame has a 2-by-3 opening. Light a frame block with Flint and Steel, then walk into the portal to travel between the Overworld and the Sulfur Dimension.

The dimension features sulfur and cinnabar terrain, sulfur pools and springs, sulfur spikes, Sulfur Cubes, and rare Sniffers. Avoid acid water and noxious sulfur gas; the Respirator Helmet helps protect against environmental effects.

Abandoned laboratories generate as you explore the Overworld. Use `/locate structure sulfur_dimension:abandoned_lab` while in the Overworld to find one. A scientist boss guards each lab. Rare Acid Water pools can generate in deserts and badlands, including desert-village biomes.

## Highlights

- Sulfur Dimension with its own terrain settings and sulfur/cinnabar generation.
- Dimension-specific world generation excludes Overworld stone, dirt, tuff, and ore features; vanilla Overworld generation remains unchanged.
- Sulfur springs are preserved with custom templates that replace tuff, stone, and granite with sulfur.
- Rare natural Sniffer spawns, alongside vanilla Sulfur Caves creatures.
- Flowing Acid Water, rare desert and badlands pools, respirator equipment, utility items, and 21 advancements.
- Abandoned laboratories registered as native structures for `/locate structure`, with the scientist boss.
- Sulfur-yellow portal-arrival tint and surface-aware portal landings.

## Build from source

Use Java 25 and the Gradle wrapper:

```bash
./gradlew clean build --no-configuration-cache
```

On Windows, run:

```bat
gradlew.bat clean build --no-configuration-cache
```

The distributable mod JAR is written to `build/libs/`. The project targets Minecraft 26.3 with Fabric Loom and official Mojang mappings.

## License

The project is licensed under the MIT License; see [`LICENSE`](LICENSE).

## Credits and references

The dimension builds on Minecraft's official Sulfur Caves and Sulfur Cube content. See Mojang's [Chaos Cubed Drop](https://www.minecraft.net/en-us/updates/chaos-cubed-drop) and [Minecraft 26.2 Snapshot 1](https://www.minecraft.net/en-us/article/minecraft-26-2-snapshot-1) articles.
