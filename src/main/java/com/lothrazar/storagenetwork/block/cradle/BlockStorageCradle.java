package com.lothrazar.storagenetwork.block.cradle;

import java.util.List;
import com.lothrazar.library.block.EntityBlockFlib;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class BlockStorageCradle extends EntityBlockFlib {

  public BlockStorageCradle() {
    super(Block.Properties.of().strength(0.5F).sound(SoundType.STONE));
  }

  @Override
  public RenderShape getRenderShape(BlockState bs) {
    return RenderShape.MODEL;
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new TileStorageCradle(pos, state);
  }

  @Override
  public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    super.appendHoverText(stack, context, tooltip, flag);
    tooltip.add(Component.translatable("block.storagenetwork.storage_cradle.tooltip").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
  }

  @Override
  public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player playerIn, BlockHitResult result) {
    if (!world.isClientSide) {
      BlockEntity tile = world.getBlockEntity(pos);
      if (tile instanceof MenuProvider) {
        ServerPlayer player = (ServerPlayer) playerIn;
        player.connection.send(tile.getUpdatePacket());
        player.openMenu((MenuProvider) tile, buf -> buf.writeBlockPos(tile.getBlockPos()));
      }
      else {
        throw new IllegalStateException("Our named container provider is missing!" + tile);
      }
    }
    return InteractionResult.SUCCESS;
  }

  @Override
  public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
    if (!level.isClientSide && level instanceof ServerLevel) {
      BlockEntity be = level.getBlockEntity(pos);
      if (be instanceof TileStorageCradle cradle) {
        for (int i = 0; i < TileStorageCradle.HOLDER_SIZE; i++) {
          ItemStack held = cradle.getHeldStack(i);
          if (!held.isEmpty() && !player.isCreative()) {
            ItemEntity drop = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, held.copy());
            drop.setDefaultPickUpDelay();
            level.addFreshEntity(drop);
          }
          cradle.getHolder().setStackInSlot(i, ItemStack.EMPTY);
        }
      }
    }
    return super.playerWillDestroy(level, pos, state, player);
  }
}
