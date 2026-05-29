package com.lothrazar.storagenetwork.api;

import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

public enum UpgradeType {

  STACK, SPEED, SLOW, STOCK, OP, SINGLE, VOID;




  static Map<Item, UpgradeType> REGISTRY =  new HashMap<Item, UpgradeType>();

  //TODO: register item STACK_UPGRADE from registry as STACK type, and so on for all the otehrs
  public static void register(Item item, UpgradeType type) {

    REGISTRY.put(item,type);
  }

  public static boolean isUpgrade(Item item) {
    return REGISTRY.containsKey(item);
  }
  public static boolean isUpgradeOfType(Item item, UpgradeType type) {
    return isUpgrade(item) && type == REGISTRY.get(item);
  }
}
