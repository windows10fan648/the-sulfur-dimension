package com.sulfur.client;

import com.sulfur.SulfurDimension;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

/** Client-side sulfur portal transition tint. */
public final class SulfurDimensionClient implements ClientModInitializer {
    private static net.minecraft.resources.ResourceKey<Level> previousDimension;
    private static int flashTicks;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null) {
                previousDimension = null;
                flashTicks = 0;
                return;
            }
            var currentDimension = client.level.dimension();
            if (previousDimension != null && !previousDimension.equals(currentDimension)
                    && (currentDimension.equals(SulfurDimension.SULFUR_WORLD)
                    || previousDimension.equals(SulfurDimension.SULFUR_WORLD))) {
                flashTicks = 24;
            }
            previousDimension = currentDimension;
            if (flashTicks > 0) flashTicks--;
        });
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(SulfurDimension.MOD_ID, "portal_tint"), (graphics, delta) -> {
            if (flashTicks <= 0) return;
            int alpha = Math.min(80, flashTicks * 4);
            graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), (alpha << 24) | 0x00D8B548);
        });
    }
}
