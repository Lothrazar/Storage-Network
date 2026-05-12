package com.lothrazar.storagenetwork.item;

import java.util.List;
import com.lothrazar.library.item.ItemFlib;
import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.capability.handler.ItemStackMatcher;
import com.lothrazar.storagenetwork.util.UtilTileEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ItemPicker extends ItemFlib {

  public static final String NBT_BOUND = "bound";

  public ItemPicker(Properties properties) {
    super(properties.stacksTo(1));
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    InteractionHand hand = context.getHand();
    Level world = context.getLevel();
    BlockPos pos = context.getClickedPos();
    Player player = context.getPlayer();
    if (world.getBlockEntity(pos) instanceof TileMain) {
      ItemStack stack = player.getItemInHand(hand);
      DimPos.putPos(stack, pos, world);
      UtilTileEntity.statusMessage(player, "item.remote.connected");
      return InteractionResult.SUCCESS;
    }
    else {
      ItemStack stack = player.getItemInHand(hand);
      DimPos dp = DimPos.getPosStored(stack);
      if (dp != null && hand == InteractionHand.MAIN_HAND && !world.isClientSide) {
        ServerLevel serverTargetWorld = DimPos.stringDimensionLookup(dp.getDimension(), world.getServer());
        if (serverTargetWorld == null) {
          StorageNetworkMod.LOGGER.error("Missing dimension key " + dp.getDimension());
          return InteractionResult.PASS;
        }
        BlockEntity tile = serverTargetWorld.getBlockEntity(dp.getBlockPos());
        if (tile instanceof TileMain) {
          TileMain network = (TileMain) tile;
          BlockState bs = world.getBlockState(pos);
          ItemStackMatcher matcher = new ItemStackMatcher(new ItemStack(bs.getBlock()), false, false);
          int size = player.isCrouching() ? 1 : 64;
          ItemStack found = network.request(matcher, size, false);
          if (!found.isEmpty()) {
            player.displayClientMessage(Component.translatable("item.remote.found"), true);
            //using add will bypass the collector so try if possible
            if (!player.addItem(found)) {
              player.spawnAtLocation(found);
            }
          }
          else {
            player.displayClientMessage(Component.translatable("item.remote.notfound.item"), true);
          }
        }
        else {
          //no main
          player.displayClientMessage(Component.translatable("item.remote.notfound"), true);
        }
      }
    }
    return InteractionResult.PASS;
  }

  @Override
  @OnlyIn(Dist.CLIENT)
  public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
    MutableComponent t = Component.translatable(getDescriptionId() + ".tooltip");
    t.withStyle(ChatFormatting.GRAY);
    tooltip.add(t);
    if (stack.has(DataComponents.CUSTOM_DATA)) {
      DimPos dp = DimPos.getPosStored(stack);
      if (dp != null) {
        tooltip.add(dp.makeTooltip());
      }
    }
  }
}
