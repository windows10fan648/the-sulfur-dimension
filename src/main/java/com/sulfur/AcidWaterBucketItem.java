package com.sulfur;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public final class AcidWaterBucketItem extends Item {
    public AcidWaterBucketItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos target = context.getClickedPos().relative(context.getClickedFace());
        if (!level.isClientSide() && level.getBlockState(target).isAir()) {
            level.setBlockAndUpdate(target, SulfurDimension.ACID_WATER.defaultBlockState());
            if (context.getPlayer() != null && !context.getPlayer().isCreative()) {
                context.getItemInHand().shrink(1);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
