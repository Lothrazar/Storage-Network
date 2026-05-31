package com.lothrazar.storagenetwork.api.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;

public class CacheModName {

  private static final Map<Item, String> ITEM_TO_MOD_NAME = new HashMap<>();

  /**
   * Get mod id for item, but use cache to save time just in case it helps
   *
   * @param theitem
   * @return
   */
  public static String getModNameForItem(Item theitem) {
    if (ITEM_TO_MOD_NAME.containsKey(theitem)) {
      return ITEM_TO_MOD_NAME.get(theitem);
    }
    String modId = BuiltInRegistries.ITEM.getKey(theitem).getNamespace();
    String lowercaseModId = modId.toLowerCase(Locale.ENGLISH);
    ITEM_TO_MOD_NAME.put(theitem, lowercaseModId);
    return lowercaseModId;
  }

}
