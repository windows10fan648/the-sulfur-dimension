package com.sulfur.client;

import com.sulfur.SulfurDimension;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.Level;

/** Client-side sulfur portal tint and animated Acid Water rendering. */
public final class SulfurDimensionClient implements ClientModInitializer {
    private static net.minecraft.resources.ResourceKey<Level> previousDimension;
    private static int tintTicks;

    @Override
    public void onInitializeClient() {
        FluidRenderingRegistry.register(
                SulfurDimension.ACID_STILL,
                SulfurDimension.ACID_FLOWING,
                new FluidModel.Unbaked(
                        new Material(Identifier.fromNamespaceAndPath("minecraft", "block/water_still")),
                        new Material(Identifier.fromNamespaceAndPath("minecraft", "block/water_flow")),
                        new Material(Identifier.fromNamespaceAndPath("minecraft", "block/water_overlay")),
                        BlockTintSources.constant(ARGB.opaque(0x9DCF3A))
                )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null) {
                previousDimension = null;
                tintTicks = 0;
                return;
            }

            var currentDimension = client.level.dimension();
            if (previousDimension != null && !previousDimension.equals(currentDimension)
                    && currentDimension.equals(SulfurDimension.SULFUR_WORLD)) {
                tintTicks = 60;
            }
            previousDimension = currentDimension;
            if (tintTicks > 0) tintTicks--;
        });

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(SulfurDimension.MOD_ID, "portal_tint"), (graphics, delta) -> {
            if (tintTicks <= 0) return;
            float progress = (60.0f - tintTicks) / 60.0f;
            float envelope = (float) Math.sin(progress * Math.PI);
            int alpha = Math.round(150.0f * envelope);
            if (alpha > 0) {
                graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), (alpha << 24) | 0x00D8B548);
            }
        });
    }
}
