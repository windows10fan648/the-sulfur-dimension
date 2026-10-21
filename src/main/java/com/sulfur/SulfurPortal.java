package com.sulfur;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.BlockHitResult;

/** Registration and frame construction for the Sulfur Dimension portal. */
public final class SulfurPortal {
    private static final int FRAME_WIDTH = 4;
    private static final int FRAME_HEIGHT = 5;

    private SulfurPortal() {}

    public static void register() {
        UseBlockCallback.EVENT.register(SulfurPortal::onUseBlock);
    }

    private static InteractionResult onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(Items.FLINT_AND_STEEL)) {
            return InteractionResult.PASS;
        }
        Direction face = hit.getDirection();
        BlockPos clicked = hit.getBlockPos();
        Axis widthAxis = face.getAxis() == Axis.Z ? Axis.X : Axis.Z;
        if (face.getAxis() == Axis.Y) {
            widthAxis = Axis.X;
        }
        Axis alternateWidthAxis = widthAxis == Axis.X ? Axis.Z : Axis.X;
        if (level.isClientSide()) {
            return isFrame(level, clicked, widthAxis) || isFrame(level, clicked, alternateWidthAxis)
                    ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        if (!activate(level, clicked, widthAxis) && !activate(level, clicked, alternateWidthAxis)) {
            return InteractionResult.PASS;
        }
        if (!player.isCreative()) {
            stack.hurtAndBreak(1, player, hand);
        }
        return InteractionResult.SUCCESS;
    }

    private static boolean activate(Level level, BlockPos clicked, Axis widthAxis) {
        for (int widthOffset = -3; widthOffset <= 0; widthOffset++) {
            for (int heightOffset = -4; heightOffset <= 0; heightOffset++) {
                BlockPos origin = offset(clicked, widthAxis, widthOffset, heightOffset);
                if (isFrame(level, origin, widthAxis)) {
                    placePortal(level, origin, widthAxis);
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isFrame(Level level, BlockPos originOrClicked, Axis widthAxis) {
        for (int widthOffset = -3; widthOffset <= 0; widthOffset++) {
            for (int heightOffset = -4; heightOffset <= 0; heightOffset++) {
                BlockPos origin = offset(originOrClicked, widthAxis, widthOffset, heightOffset);
                if (isExactFrame(level, origin, widthAxis)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isExactFrame(Level level, BlockPos origin, Axis widthAxis) {
        for (int w = 0; w < FRAME_WIDTH; w++) {
            for (int h = 0; h < FRAME_HEIGHT; h++) {
                BlockPos pos = offset(origin, widthAxis, w, h);
                BlockState state = level.getBlockState(pos);
                boolean frame = w == 0 || w == FRAME_WIDTH - 1 || h == 0 || h == FRAME_HEIGHT - 1;
                if (frame ? !state.is(Blocks.SULFUR) : !state.isAir()) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void placePortal(Level level, BlockPos origin, Axis widthAxis) {
        for (int w = 1; w < FRAME_WIDTH - 1; w++) {
            for (int h = 1; h < FRAME_HEIGHT - 1; h++) {
                Axis portalAxis = widthAxis == Axis.X ? Axis.Z : Axis.X;
                level.setBlockAndUpdate(offset(origin, widthAxis, w, h),
                        SulfurDimension.SULFUR_PORTAL.defaultBlockState().setValue(BlockStateProperties.AXIS, portalAxis));
            }
        }
    }

    private static BlockPos offset(BlockPos origin, Axis widthAxis, int width, int height) {
        int x = widthAxis == Axis.X ? width : 0;
        int z = widthAxis == Axis.Z ? width : 0;
        return origin.offset(x, height, z);
    }
}
