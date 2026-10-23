package com.sulfur;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** Handles the hidden marker embedded in the data-driven abandoned-lab structure. */
public final class SulfurLab {
    private static final String BOSS_MARKER_TAG = "sulfur_dimension:abandoned_lab_boss";

    private SulfurLab() {}

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register(SulfurLab::onEntityLoad);
    }

    private static void onEntityLoad(Entity entity, ServerLevel level) {
        if (!level.dimension().equals(Level.OVERWORLD)
                || !EntityType.getKey(entity.getType()).equals(Identifier.withDefaultNamespace("armor_stand"))
                || !entity.entityTags().contains(BOSS_MARKER_TAG)) {
            return;
        }

        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        entity.discard();
        SulfurLabBoss.spawn(level, x, y, z);
    }
}
