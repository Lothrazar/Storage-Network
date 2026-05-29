package com.lothrazar.storagenetwork.api.batch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.world.item.Item;

public class Batch<K> extends HashMap<Item, List<K>> {

  private static final long serialVersionUID = -7856766754136175749L;

  public List<K> put(Item item, K object) {
    if (containsKey(item)) {
      List<K> matchingList = super.get(item);
      matchingList.add(object);
      return matchingList;
    }
    List<K> newList = new ArrayList<K>();
    newList.add(object);
    return super.put(item, newList);
  }
}
