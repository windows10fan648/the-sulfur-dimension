package com.sulfur.client;

import net.fabricmc.api.ClientModInitializer;

/** Official minecraft:sulfur_cube owns its vanilla renderer; no custom renderer is needed. */
public final class SulfurDimensionClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
    }
}
