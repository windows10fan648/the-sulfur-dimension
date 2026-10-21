package com.sulfur;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class AcidWaterBlock extends Block {
    public AcidWaterBlock(BlockBehaviour.Properties properties) { super(properties); }
    @Override public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, net.minecraft.world.entity.InsideBlockEffectApplier effects, boolean suffocates) {
        if (!level.isClientSide() && entity instanceof Player player && !player.isCreative() && level.getGameTime() % 20L == 0L) {
            player.hurt(level.damageSources().magic(), 2.0f);
            if (!player.getItemBySlot(EquipmentSlot.HEAD).is(SulfurDimension.RESPIRATOR_HELMET)) {
                player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 100, 0));
            }
        }
        super.entityInside(state, level, pos, entity, effects, suffocates);
    }
}
