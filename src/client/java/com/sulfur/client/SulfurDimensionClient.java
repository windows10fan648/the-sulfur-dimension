package com.sulfur.client;

import com.sulfur.SulfurDimension;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

/** Client-side sulfur portal transition tint. */
public final class SulfurDimensionClient implements ClientModInitializer {
    private static net.minecraft.resources.ResourceKey<Level> previousDimension;
    private static int tintTicks;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null) {
                previousDimension = null;
                tintTicks = 0;
                return;
            }

            var currentDimension = client.level.dimension();
            if (previousDimension != null && !previousDimension.equals(currentDimension)
                    && currentDimension.equals(SulfurDimension.SULFUR_WORLD)) {
                // A 3-second easing pulse gives the dimension-entry transition time to read clearly.
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
