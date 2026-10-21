package com.sulfur;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

/** The Sulfur Dimension. Vanilla 26.3 sulfur content is intentionally reused. */
public final class SulfurDimension implements ModInitializer {
    public static final String MOD_ID = "sulfur_dimension";

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static final ResourceKey<Level> SULFUR_WORLD = ResourceKey.create(Registries.DIMENSION, id("sulfur"));

    private static ResourceKey<Block> blockKey(String name) {
        return ResourceKey.create(Registries.BLOCK, id(name));
    }

    private static ResourceKey<Item> itemKey(String name) {
        return ResourceKey.create(Registries.ITEM, id(name));
    }

    private static BlockBehaviour.Properties liquid(String name) {
        return BlockBehaviour.Properties.of()
                .setId(blockKey(name))
                .mapColor(MapColor.COLOR_GREEN)
                .noCollision()
                .instabreak()
                .sound(SoundType.WET_GRASS);
    }

    private static Item.Properties itemProperties(String name) {
        return new Item.Properties().setId(itemKey(name));
    }

    /** Custom acid water remains mod content; sulfur blocks are vanilla 26.3 registries. */
    public static final Block ACID_WATER = registerBlock("acid_water", new AcidWaterBlock(liquid("acid_water")));
    public static final Block SULFUR_PORTAL = registerPortalBlock("sulfur_portal", new SulfurPortalBlock(
            BlockBehaviour.Properties.of()
                    .setId(blockKey("sulfur_portal"))
                    .noCollision()
                    .strength(-1.0f)
                    .sound(SoundType.GLASS)
                    .lightLevel(state -> 11)));
    public static final Item SULFUR_GEM = registerItem("sulfur_gem", new Item(itemProperties("sulfur_gem")));
    public static final Item SULFUR_FIELD_GUIDE = registerItem("sulfur_field_guide", new SulfurFieldGuideItem(itemProperties("sulfur_field_guide").stacksTo(1)));
    public static final Item ACID_WATER_BUCKET = registerItem("acid_water_bucket", new AcidWaterBucketItem(itemProperties("acid_water_bucket").stacksTo(1)));
    public static final Item RESPIRATOR_HELMET = registerItem("respirator_helmet", new Item(itemProperties("respirator_helmet").durability(165).equippable(EquipmentSlot.HEAD)));
    public static final ResourceKey<CreativeModeTab> SULFUR_TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, id("main"));

    private static Block registerBlock(String name, Block block) {
        Registry.register(BuiltInRegistries.BLOCK, id(name), block);
        Registry.register(BuiltInRegistries.ITEM, id(name), new BlockItem(block, itemProperties(name)));
        return block;
    }

    private static Block registerPortalBlock(String name, Block block) {
        Registry.register(BuiltInRegistries.BLOCK, id(name), block);
        Registry.register(BuiltInRegistries.ITEM, id(name), new BlockItem(block, itemProperties(name)));
        return block;
    }

    private static Item registerItem(String name, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, id(name), item);
    }

    @Override
    public void onInitialize() {
        // Official minecraft:sulfur_caves supplies the Sulfur Cube and vanilla sulfur worldgen.
        SulfurPortal.register();
        SulfurLab.register();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id("main"), CreativeModeTab.builder(CreativeModeTab.Row.TOP, 7)
                .title(Component.translatable("itemGroup.sulfur_dimension.main"))
                .icon(() -> new ItemStack(SULFUR_GEM))
                .displayItems((parameters, output) -> {
                    output.accept(ACID_WATER);
                    output.accept(SULFUR_PORTAL);
                    output.accept(SULFUR_GEM);
                    output.accept(SULFUR_FIELD_GUIDE);
                    output.accept(ACID_WATER_BUCKET);
                    output.accept(RESPIRATOR_HELMET);
                })
                .build());
    }
}
