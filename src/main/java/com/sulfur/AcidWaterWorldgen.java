package com.sulfur;

import java.util.List;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/** Adds rare acid-water pools to hot Overworld biomes during new chunk generation. */
public final class AcidWaterWorldgen {
    private AcidWaterWorldgen() {}

    public static void register() {
        ResourceKey<PlacedFeature> pools = ResourceKey.create(
                Registries.PLACED_FEATURE,
                SulfurDimension.id("acid_water_pools"));
        for (String biome : List.of("desert", "badlands", "eroded_badlands", "wooded_badlands")) {
            ResourceKey<Biome> biomeKey = ResourceKey.create(
                    Registries.BIOME,
                    Identifier.withDefaultNamespace(biome));
            BiomeModifications.addFeature(
                    BiomeSelectors.includeByKey(biomeKey),
                    GenerationStep.Decoration.LAKES,
                    pools);
        }
    }
}
