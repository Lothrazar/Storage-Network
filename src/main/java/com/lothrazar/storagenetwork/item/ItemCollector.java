package com.lothrazar.storagenetwork.item;

import java.util.List;

import com.lothrazar.library.util.ChatUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.component.CustomData;
import org.apache.commons.lang3.tuple.Triple;
import com.lothrazar.library.item.ItemFlib;
import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.api.util.UtilInventory;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

public class ItemCollector extends ItemFlib {
  private static final String NBT_ENABLED = "Enabled";

  public ItemCollector(Properties properties) {
    super(properties.stacksTo(1));
  }

  protected ItemStack findAmmo(Player player, Item item) {
    //is curios installed? doesnt matter this is safe
    Triple<String, Integer, ItemStack> remote = UtilInventory.getCurioRemote(player, item);
    return remote.getRight();
  }

  public void toggleEnabled(ItemStack stack, Player player) {
    boolean newEnabled = !isEnabled(stack);
    stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data.update(tag -> tag.putBoolean(NBT_ENABLED, newEnabled)));
    player.displayClientMessage(makeDisabledTooltip(newEnabled), true);
  }

  // not subscribe, called from SsnEvents.java
  public void onEntityItemPickupEvent(ItemEntityPickupEvent.Pre event) {
    if (event.getPlayer() instanceof Player &&
        event.getItemEntity() != null &&
        !event.getItemEntity().getItem().isEmpty()) {
      Player player = event.getPlayer();

      // find the collector that the player has with them (main hand, offhand, curios...)
      ItemStack collectorStack = this.findAmmo(player, this);
      if (collectorStack.isEmpty()) {
        return;
      }

      // check if it is turned on
      if (!isEnabled(collectorStack)) {
        return;
      }

      ItemStack item = event.getItemEntity().getItem();
      Level world = player.level();
      DimPos dp = DimPos.getPosStored(collectorStack);
      if (dp != null && !world.isClientSide) {
        ServerLevel serverTargetWorld = DimPos.stringDimensionLookup(dp.getDimension(), world.getServer());
        if (serverTargetWorld == null) {
          StorageNetworkMod.LOGGER.error("Missing dimension key " + dp.getDimension());
          return;
        }
        BlockEntity tile = serverTargetWorld.getBlockEntity(dp.getBlockPos());
        if (tile instanceof TileMain network) {
          int countUnmoved = network.insertStack(item.copy(), false);
          item.setCount(countUnmoved);
          if (countUnmoved == 0) {
            playSoundFromServer((ServerPlayer) player, SoundEvents.ITEM_PICKUP, 0.2F);
          }
        }
        // else { StorageNetworkMod.LOGGER.error("item.remote.notfound"); }
      }
    }
  }


  public static void playSoundFromServer(ServerPlayer entityIn, SoundEvent soundIn, float volume) {
    if (soundIn == null || entityIn == null) {
      return;
    }
    entityIn.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(soundIn), SoundSource.PLAYERS, entityIn.xOld, entityIn.yOld, entityIn.zOld, volume, 1.0F, 0)); // pitch=1; seed=0
  }
  private static boolean isEnabled(ItemStack collectorStack) {
    CustomData customData = collectorStack.get(DataComponents.CUSTOM_DATA);
    // Default to enabled for a fresh stack (no CUSTOM_DATA / no NBT_ENABLED entry yet).
    return customData == null || !customData.copyTag().contains(NBT_ENABLED) || customData.copyTag().getBoolean(NBT_ENABLED);
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
      ChatUtil.sendStatusMessage(player, "item.remote.connected");
      return InteractionResult.SUCCESS;
    }
    return InteractionResult.PASS;
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
      }
      tooltip.add(makeDisabledTooltip(isEnabled(stack)).withStyle(ChatFormatting.DARK_GRAY));
    }
  }

  // used for title on keybind as well as item tooltip
  private MutableComponent makeDisabledTooltip(boolean enabled) {
    return Component.translatable(getDescriptionId() + "." + (enabled ? "enabled" : "disabled"));
  }
}
