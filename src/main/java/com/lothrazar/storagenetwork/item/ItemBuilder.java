package com.lothrazar.storagenetwork.item;

import java.util.List;
import com.lothrazar.library.item.ItemFlib;
import com.lothrazar.library.util.ChatUtil;
import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.api.capabilities.ItemStackMatcherDefault;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;

public class ItemBuilder extends ItemFlib {

  public static final String NBTBLOCKSTATE = "blockstate";

  public ItemBuilder(Properties properties) {
    super(properties.stacksTo(1));
  }

  public static void setBlockState(ItemStack wand, BlockState target) {
    CompoundTag encoded = NbtUtils.writeBlockState(target);
    wand.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data.update(tag -> tag.put(NBTBLOCKSTATE, encoded)));
  }

  public static BlockState getBlockState(Level level, ItemStack wand) {
    CustomData customData = wand.get(DataComponents.CUSTOM_DATA);
    if (customData == null) {
      return null;
    }
    CompoundTag tag = customData.copyTag();
    if (!tag.contains(NBTBLOCKSTATE)) {
      return null;
    }
    return NbtUtils.readBlockState(level.registryAccess().lookupOrThrow(Registries.BLOCK), tag.getCompound(NBTBLOCKSTATE));
  }

  @SuppressWarnings("deprecation")
  @Override
  public InteractionResult useOn(UseOnContext context) {
    InteractionHand hand = context.getHand();
    Level world = context.getLevel();
    BlockPos pos = context.getClickedPos();
    Player player = context.getPlayer();
    BlockPos buildAt = pos.relative(context.getClickedFace());
    if (world.getBlockEntity(pos) instanceof TileMain) {
      ItemStack stack = player.getItemInHand(hand);
      DimPos.putPos(stack, pos, world);
      ChatUtil.sendStatusMessage(player, "item.remote.connected");
      return InteractionResult.SUCCESS;
    }
    else if (world.isEmptyBlock(buildAt) || world.getBlockState(buildAt).liquid()) {
      player.swing(hand);
      ItemStack stack = player.getItemInHand(hand);
      //succeed or fail
      DimPos dp = DimPos.getPosStored(stack);
      if (dp != null && hand == InteractionHand.MAIN_HAND && !world.isClientSide) {
        ServerLevel serverTargetWorld = DimPos.stringDimensionLookup(dp.getDimension(), world.getServer());
        if (serverTargetWorld == null) {
          StorageNetworkMod.LOGGER.error("Missing dimension key " + dp.getDimension());
          return InteractionResult.PASS;
        }
        BlockEntity tile = serverTargetWorld.getBlockEntity(dp.getBlockPos());
        BlockState targetState = ItemBuilder.getBlockState(world, stack);
        if (tile instanceof TileMain && targetState != null) {
          TileMain network = (TileMain) tile;
          ItemStackMatcherDefault matcher = new ItemStackMatcherDefault(new ItemStack(targetState.getBlock()), false, false);
          ItemStack found = network.request(matcher, 1, true);
          //SIMULATED, see if materials are available
          if (!found.isEmpty()) {
            // yes materials are available
            boolean success = placeStateSafe(world, player, buildAt, targetState);
            if (success) {
              network.request(matcher, 1, false);
              //NOT SIMULATED, extract item from network
            }
          }
          else {
            player.displayClientMessage(Component.translatable("item.remote.notfound.item"), true);
          }
        }
        else {
          player.displayClientMessage(Component.translatable("item.remote.notfound"), true);
        }
      }
    }
    //else something non-air and non-liquid is in the way, flower etc
    return InteractionResult.PASS;
  }

  @SuppressWarnings("deprecation")
  private boolean placeStateSafe(Level world, Player player, BlockPos placePos, BlockState placeState) {
    BlockState stateHere = world.getBlockState(placePos);
    if (stateHere.getBlock() == Blocks.AIR || stateHere.liquid()) {
      return world.setBlock(placePos, placeState, 3);
    }
    return false;
  }

  @Override
  @OnlyIn(Dist.CLIENT)
  public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
    MutableComponent t = Component.translatable(getDescriptionId() + ".tooltip");
    t.withStyle(ChatFormatting.GRAY);
    tooltip.add(t);
    if (stack.has(DataComponents.CUSTOM_DATA)) {
      DimPos dp = DimPos.getPosStored(stack);
      if (dp != null) {
        tooltip.add(dp.makeTooltip());
      } // block state?
      BlockState target = null;
      if (context.level() != null) {
        target = ItemBuilder.getBlockState(context.level(), stack);
      }
      if (target != null) {
        String block = target.getBlock().getDescriptionId();
        t = Component.translatable(block);
        t.withStyle(ChatFormatting.AQUA);
        tooltip.add(t);
      }
      else {
        //if it has a network connection but no blockstate saved, then
        t = Component.translatable(getDescriptionId() + ".blockstate");
        t.withStyle(ChatFormatting.AQUA);
        tooltip.add(t);
      }
    }
  }

  public static void onLeftClickBlock(LeftClickBlock event) {
    Player player = event.getEntity();
    ItemStack held = player.getItemInHand(event.getHand());
    if (held.getItem() == SsnRegistry.Items.BUILDER_REMOTE.get()) {
      Level world = player.getCommandSenderWorld();
      BlockState target = world.getBlockState(event.getPos());
      ItemBuilder.setBlockState(held, target);
      ChatUtil.statusMessage(player, target);
      event.setCanceled(true);
    }
  }
}
