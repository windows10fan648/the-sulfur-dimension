package com.sulfur.client;

import com.sulfur.SulfurDimension;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.GhastRenderer;
import net.minecraft.client.renderer.entity.state.GhastRenderState;
import net.minecraft.resources.Identifier;

/** Uses the vanilla Ghast model and animation with the Sulfurized skin. */
public final class SulfurizedGhastRenderer extends GhastRenderer {
    private static final Identifier TEXTURE = SulfurDimension.id("textures/entity/sulfurized_ghast.png");

    public SulfurizedGhastRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public Identifier getTextureLocation(GhastRenderState state) {
        return TEXTURE;
    }
}
