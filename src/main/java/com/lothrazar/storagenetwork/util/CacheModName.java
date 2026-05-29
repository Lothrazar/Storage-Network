package com.lothrazar.storagenetwork.util;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.lothrazar.library.util.ChatUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.registries.BuiltInRegistries;

public class CacheModName {

  private static final Map<Item, String> modNamesForIds = new HashMap<>();

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

  @Deprecated(since="flib-0.2.1")
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


  @Deprecated(since="flib-0.2.1")
  public static void sendStatusMessage(Player player, BlockState bs) {
    ChatUtil.sendStatusMessage(player, Component.translatable(bs.getBlock().getName().getString()));
  }


}
