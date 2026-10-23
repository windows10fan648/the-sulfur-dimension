package com.sulfur.mixin;

import com.sulfur.SulfurFireball;
import com.sulfur.SulfurizedGhast;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Replaces only modded Ghast shots; vanilla Ghasts continue to use vanilla fireballs. */
@Mixin(targets = "net.minecraft.world.entity.monster.Ghast$GhastShootFireballGoal")
public abstract class GhastFireballMixin {
    @Redirect(
            method = "tick",
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/world/entity/projectile/hurtingprojectile/LargeFireball"
            )
    )
    private LargeFireball sulfur_dimension$useSulfurFireball(Level level, LivingEntity shooter, Vec3 direction, int explosionPower) {
        if (shooter instanceof SulfurizedGhast) {
            return new SulfurFireball(level, shooter, direction, explosionPower);
        }
        return new LargeFireball(level, shooter, direction, explosionPower);
    }
}
