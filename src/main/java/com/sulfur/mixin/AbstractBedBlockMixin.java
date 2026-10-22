package com.sulfur.mixin;

import com.sulfur.SulfurDimension;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractBedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBedBlock.class)
public abstract class AbstractBedBlockMixin {
    @Inject(method = "useWithoutItem", at = @At("TAIL"))
    private void sulfur_dimension$killAfterBedExplosion(BlockState state, Level level, BlockPos pos,
                                                        Player player, BlockHitResult hitResult,
                                                        CallbackInfoReturnable<InteractionResult> cir) {
        if (level.isClientSide() || !level.dimension().equals(SulfurDimension.SULFUR_WORLD)
                || !player.isAlive()) {
            return;
        }

        // Vanilla's bed blast already uses this special damage type; the extra hit makes it fatal
        // even if armor or the normal blast's distance-based damage would otherwise let them live.
        float lethalDamage = Math.max(1000.0F, player.getMaxHealth() * 100.0F + 100.0F);
        player.hurt(level.damageSources().badRespawnPointExplosion(Vec3.atCenterOf(pos)), lethalDamage);
    }
}
