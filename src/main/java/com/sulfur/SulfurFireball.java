package com.sulfur;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Ghast-fired fireball that deals five damage and nauseates players for one minute. */
public final class SulfurFireball extends LargeFireball {
    public SulfurFireball(Level level, LivingEntity owner, Vec3 direction, int explosionPower) {
        super(level, owner, direction, explosionPower);
        setItem(new ItemStack(SulfurDimension.SULFUR_FIREBALL_ITEM));
    }

    /** Replace the vanilla Ghast explosion hit with a direct, fixed-damage impact. */
    @Override
    protected void onHit(HitResult hit) {
        if (hit instanceof EntityHitResult entityHit) {
            onHitEntity(entityHit);
        } else {
            discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        if (!level().isClientSide()) {
            Entity target = hit.getEntity();
            target.hurt(damageSources().fireball(this, getOwner()), 5.0F);
            if (target instanceof Player player) {
                player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 20 * 60, 0), getOwner());
            }
        }
        discard();
    }
}
