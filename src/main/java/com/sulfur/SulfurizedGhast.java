package com.sulfur;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.level.Level;

/** A sulfur-corrupted Ghast that retains vanilla Ghast flight and player-targeting AI. */
public final class SulfurizedGhast extends Ghast {
    public SulfurizedGhast(EntityType<? extends Ghast> type, Level level) {
        super(type, level);
    }
}
