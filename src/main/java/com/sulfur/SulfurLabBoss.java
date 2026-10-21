package com.sulfur;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** A tough vanilla-style scientist using the official Zombie model and AI. */
public final class SulfurLabBoss {
    private SulfurLabBoss() {}

    public static void spawn(ServerLevel level, double x, double y, double z) {
        Zombie scientist = new Zombie(level);
        scientist.setPos(x, y, z);
        scientist.setYRot(0.0f);
        scientist.setXRot(0.0f);
        scientist.setCustomName(Component.literal("Dr. Elias Sulfur, Age 70"));
        scientist.setCustomNameVisible(true);
        scientist.setPersistenceRequired();
        scientist.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        scientist.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.LEATHER_CHESTPLATE));
        scientist.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.LEATHER_LEGGINGS));
        scientist.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        if (scientist.getAttribute(Attributes.MAX_HEALTH) != null) {
            scientist.getAttribute(Attributes.MAX_HEALTH).setBaseValue(80.0);
        }
        if (scientist.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            scientist.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(10.0);
        }
        if (scientist.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
            scientist.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.30);
        }
        scientist.setHealth(80.0f);
        level.addFreshEntity(scientist);
    }
}
