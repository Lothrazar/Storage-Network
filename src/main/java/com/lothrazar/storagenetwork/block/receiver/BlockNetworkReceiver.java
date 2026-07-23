package com.lothrazar.storagenetwork.block.receiver;

import java.util.List;
import com.lothrazar.library.block.EntityBlockFlib;
import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.items.ItemStackHandler;

public class BlockNetworkReceiver extends EntityBlockFlib {

  public BlockNetworkReceiver() {
    super(Block.Properties.of().strength(0.5F).sound(SoundType.STONE));
  }

  @Override
  public RenderShape getRenderShape(BlockState bs) {
    return RenderShape.MODEL;
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new TileNetworkReceiver(pos, state);
  }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
    return createTickerHelper(type, SsnRegistry.Tiles.RECEIVER.get(), world.isClientSide() ? TileNetworkReceiver::clientTick : TileNetworkReceiver::serverTick);
  }

  @Override
  public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
    super.setPlacedBy(world, pos, state, placer, stack);
    if (world.isClientSide()) {
      return;
    }
    BlockEntity be = world.getBlockEntity(pos);
    if (be instanceof TileNetworkReceiver recv) {
      DimPos stored = DimPos.getPosStored(stack);
      if (stored != null) {
        recv.setBoundMaster(stored);
      }
    }
  }

  @Override
  public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player playerIn, BlockHitResult result) {
    if (!world.isClientSide()) {
      BlockEntity tile = world.getBlockEntity(pos);
      if (tile instanceof MenuProvider mp && playerIn instanceof ServerPlayer sp) {
        sp.connection.send(tile.getUpdatePacket());
        sp.openMenu(mp, buf -> buf.writeBlockPos(pos));
      }
    }
    return InteractionResult.SUCCESS;
  }

  @Override
  public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
    if (!level.isClientSide()) {
      BlockEntity be = level.getBlockEntity(pos);
      if (be instanceof TileNetworkReceiver recv && !player.isCreative()) {
        // Drop the upgrade contents so they aren't lost on break.
        ItemStackHandler up = recv.getUpgrades();
        for (int i = 0; i < up.getSlots(); i++) {
          ItemStack s = up.getStackInSlot(i);
          if (!s.isEmpty()) {
            Block.popResource(level, pos, s);
            up.setStackInSlot(i, ItemStack.EMPTY);
          }
        }
        // Silk touch preserves the master binding by dropping a stamped BlockItem
        // and suppressing the normal loot-table drop (which would be unbound).
        if (recv.getBoundMaster() != null && hasSilkTouch(level, player.getMainHandItem())) {
          DimPos bm = recv.getBoundMaster();
          Level masterLevel = bm.resolveLevel();
          if (masterLevel != null) {
            ItemStack stamped = new ItemStack(SsnRegistry.Items.RECEIVER.get());
            DimPos.putPos(stamped, bm.getBlockPos(), masterLevel);
            Block.popResource(level, pos, stamped);
            // Replacing with air now means the loot table sees a non-matching
            // block and yields nothing - so we don't get both a stamped item
            // AND an unbound one.
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 35);
            return state;
          }
        }
      }
    }
    return super.playerWillDestroy(level, pos, state, player);
  }

  private static boolean hasSilkTouch(Level level, ItemStack tool) {
    if (tool.isEmpty()) {
      return false;
    }
    Holder<Enchantment> silk = level.registryAccess()
        .lookupOrThrow(Registries.ENCHANTMENT)
        .getOrThrow(Enchantments.SILK_TOUCH);
    return EnchantmentHelper.getItemEnchantmentLevel(silk, tool) > 0;
  }

  // Block#onRemove is gone in 26.1; this cleanup now lives in
  // TileNetworkReceiver#preRemoveSideEffects, which the game calls at the equivalent point
  // in the block-removal sequence (only when the block type is actually changing).

  @Override
  public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    super.appendHoverText(stack, context, tooltip, flag);

    DimPos stored = DimPos.getPosStored(stack);
    if (stored != null) {
      tooltip.add(Component.translatable("block.storagenetwork.receiver.bound").withStyle(ChatFormatting.AQUA));
      tooltip.add(stored.makeTooltip());
    }
    else {
      tooltip.add(Component.translatable("block.storagenetwork.receiver.unbound").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
  }
}
