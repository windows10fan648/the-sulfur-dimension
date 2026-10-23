package com.sulfur;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

public final class AcidWaterBlock extends LiquidBlock {
    public AcidWaterBlock(FlowingFluid fluid, BlockBehaviour.Properties properties) {
        super(fluid, properties);
    }

    private static boolean hasFullSulfurArmor(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(SulfurDimension.SULFUR_HELMET)
                && player.getItemBySlot(EquipmentSlot.CHEST).is(SulfurDimension.SULFUR_CHESTPLATE)
                && player.getItemBySlot(EquipmentSlot.LEGS).is(SulfurDimension.SULFUR_LEGGINGS)
                && player.getItemBySlot(EquipmentSlot.FEET).is(SulfurDimension.SULFUR_BOOTS);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                             InsideBlockEffectApplier effects, boolean suffocates) {
        if (!level.isClientSide() && entity instanceof Player player && !player.isCreative()
                && level.getGameTime() % 20L == 0L && !hasFullSulfurArmor(player)) {
            player.hurt(level.damageSources().magic(), 2.0f);
            if (!player.getItemBySlot(EquipmentSlot.HEAD).is(SulfurDimension.RESPIRATOR_HELMET)) {
                player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 100, 0));
            }
        }
        super.entityInside(state, level, pos, entity, effects, suffocates);
    }
}
