package com.sulfur.mixin;

import com.sulfur.SulfurDimension;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keeps the vanilla Potent Sulfur gas behavior, while making the mod respirator protective. */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void sulfur_dimension$respiratorProtectsFromSulfurGas(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof Player player) || player.level().isClientSide()) {
            return;
        }
        if (player.level().dimension().equals(SulfurDimension.SULFUR_WORLD)
                && player.getItemBySlot(EquipmentSlot.HEAD).is(SulfurDimension.RESPIRATOR_HELMET)) {
            // Vanilla PotentSulfurBlockEntity applies Nausea for 80 ticks near reachable gas.
            // Remove that toxic-gas effect immediately for a player wearing the respirator.
            player.removeEffect(MobEffects.NAUSEA);
        }
    }
}
