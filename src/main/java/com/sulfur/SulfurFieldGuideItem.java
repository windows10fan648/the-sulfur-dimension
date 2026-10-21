package com.sulfur;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.TooltipDisplay;

/** Compact in-game list of the Sulfur Dimension content and mechanics. */
public final class SulfurFieldGuideItem extends Item {
    public SulfurFieldGuideItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.literal("The Sulfur Dimension Field Guide"));
        tooltip.accept(Component.literal("Blocks: Sulfur, Cinnabar, Potent Sulfur"));
        tooltip.accept(Component.literal("Hazards: Acid Water and noxious gas"));
        tooltip.accept(Component.literal("Protection: Respirator Helmet"));
        tooltip.accept(Component.literal("Portal: 4x5 Sulfur frame + Flint and Steel"));
        tooltip.accept(Component.literal("Mobs: Sulfur Cube"));
        tooltip.accept(Component.literal("Find: abandoned Sulfur Labs in the Overworld"));
    }
}
