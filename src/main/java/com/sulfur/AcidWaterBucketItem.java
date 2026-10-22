package com.sulfur;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.material.Fluid;

/** Vanilla bucket interactions for collecting and placing the custom Acid Water fluid. */
public final class AcidWaterBucketItem extends BucketItem {
    public AcidWaterBucketItem(Fluid fluid, Properties properties) {
        super(fluid, properties);
    }
}
