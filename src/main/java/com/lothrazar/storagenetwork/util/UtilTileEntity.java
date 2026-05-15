package com.lothrazar.storagenetwork.util;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.lothrazar.storagenetwork.api.IConnectable;
import com.lothrazar.storagenetwork.block.main.TileMain;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.registries.BuiltInRegistries;

public class UtilTileEntity {

  private static final Map<Item, String> modNamesForIds = new HashMap<>();
  public static final int MOUSE_BTN_LEFT = 0;
  public static final int MOUSE_BTN_RIGHT = 1;
  public static final int MOUSE_BTN_MIDDLE_CLICK = 2;

  public static void addOrMergeIntoList(List<ItemStack> list, ItemStack stackToAdd) {
    boolean added = false;
    for (ItemStack stack : list) {
      if (ItemStack.isSameItemSameComponents(stackToAdd, stack)) {
        stack.setCount(stack.getCount() + stackToAdd.getCount());
        added = true;
        break;
      }
    }
    if (!added) {
      list.add(stackToAdd);
    }
  }

  public static void playSoundFromServer(ServerPlayer entityIn, SoundEvent soundIn, float volume) {
    if (soundIn == null || entityIn == null) {
      return;
    }
    entityIn.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(soundIn), SoundSource.PLAYERS, entityIn.xOld, entityIn.yOld, entityIn.zOld, volume, 1.0F, 0)); // pitch=1; seed=0
  }

  public static void chatMessage(Player player, String message) {
    if (player.level().isClientSide) {
      player.sendSystemMessage(Component.translatable(message));
    }
  }

  public static void statusMessage(Player player, BlockState bs) {
    if (player.level().isClientSide) {
      player.displayClientMessage(Component.translatable(bs.getBlock().getName().getString()), true);
    }
  }

  public static void statusMessage(Player player, String message) {
    if (player.level().isClientSide) {
      player.displayClientMessage(Component.translatable(message), true);
    }
  }

  public static String lang(String message) {
    return Component.translatable(message).getString();
  }

  /**
   * This can only be called on the server side! It returns the Main tile entity for the given connectable.
   *
   * @param connectable
   * @return
   */
  public static TileMain getTileMainForConnectable(IConnectable connectable) {
    if (connectable == null || connectable.getMainPos() == null) {
      return null;
    }
    return connectable.getMainPos().getTileEntity(TileMain.class);
  }

  /**
   * Get mod id for item, but use cache to save time just in case it helps
   * 
   * @param theitem
   * @return
   */
  public static String getModNameForItem(Item theitem) {
    if (modNamesForIds.containsKey(theitem)) {
      return modNamesForIds.get(theitem);
    }
    String modId = BuiltInRegistries.ITEM.getKey(theitem).getNamespace();
    String lowercaseModId = modId.toLowerCase(Locale.ENGLISH);
    modNamesForIds.put(theitem, lowercaseModId);
    return lowercaseModId;
  }
}
