package com.sulfur;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.item.component.WrittenBookContent;

/** A signed vanilla written book containing practical guidance for the Sulfur Dimension. */
public final class SulfurFieldGuideItem extends WrittenBookItem {
    public SulfurFieldGuideItem(Properties properties) {
        super(properties.component(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(
                Filterable.passThrough("The Sulfur Field Guide"),
                "The Sulfur Dimension",
                0,
                List.of(
                        page("THE SULFUR FIELD GUIDE\n\nA traveler's introduction to the Sulfur Dimension, its hazards, and its strange life."),
                        page("OPENING A PORTAL\n\nBuild a sulfur-block frame 4 blocks wide and 5 blocks tall. Leave a 2-by-3 opening. Light it with Flint and Steel, then step inside."),
                        page("TERRAIN\n\nSulfur and cinnabar shape the land. Look for sulfur pools, springs, spikes, and caves. The dimension's terrain is unlike the Overworld."),
                        page("HAZARDS\n\nAcid water is dangerous. Avoid swimming in it. Sulfur gas can harm the unprotected. Wear a Respirator Helmet while exploring hazardous areas."),
                        page("CREATURES\n\nSulfur Cubes inhabit this dimension. Sniffers are rare visitors. Give unfamiliar creatures space until you know what they do."),
                        page("ABANDONED LABS\n\nOld laboratories are hidden in the Overworld. Use /locate structure sulfur_dimension:abandoned_lab to find the nearest one."),
                        page("LAB NOTES\n\nThe scientist inside may be hostile. Prepare before entering, watch your surroundings, and bring supplies for the journey home."),
                        page("FIELD ADVICE\n\nCarry protection, keep clear of acid pools, and note the route back to the portal. Careful explorers live to write the next page.")
                ),
                true
        )));
    }

    private static Filterable<Component> page(String text) {
        return Filterable.passThrough(Component.literal(text));
    }
}
