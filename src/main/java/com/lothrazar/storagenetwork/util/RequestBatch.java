package com.lothrazar.storagenetwork.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import net.minecraft.world.item.Item;

public class RequestBatch {

  private final Map<Item, LinkedHashMap<Integer, Request>> map = new HashMap<>();

  public void put(Item item, Request req) {
    if (item == null || req == null) return;
    LinkedHashMap<Integer, Request> byDest = map.computeIfAbsent(item, k -> new LinkedHashMap<>());
    int key = req.getTargetKey();
    Request existing = byDest.get(key);
    if (existing == null) {
      byDest.put(key, req);
    } else {
      existing.setCount(existing.getCount() + req.getCount());
    }
  }

  public boolean isEmpty() {
    if (map.isEmpty()) return true;
    for (Map<Integer, Request> m : map.values()) {
      if (!m.isEmpty()) return false;
    }
    return true;
  }

  public void forEach(BiConsumer<Item, Request> consumer) {
    for (Map.Entry<Item, LinkedHashMap<Integer, Request>> e : map.entrySet()) {
      Item item = e.getKey();
      for (Request r : e.getValue().values()) {
        consumer.accept(item, r);
      }
    }
  }

  // Backward-compatibility
  // remove once all call sites use batch.forEach(...).
  // mirrors old API: try to satisfy requests for this item from a provider slot
  public void extractStacks(com.lothrazar.storagenetwork.api.IConnectableLink providerStorage,
                            Integer slot,
                            net.minecraft.world.item.Item item) {
    java.util.LinkedHashMap<Integer, Request> byDest = map.get(item);
    if (byDest == null || byDest.isEmpty()) return;
    java.util.Iterator<Request> it = byDest.values().iterator();
    while (it.hasNext()) {
      Request r = it.next();
      if (!r.insertStack(providerStorage, slot)) {
        // keep for next pass
      }
      net.minecraft.world.item.ItemStack peek = providerStorage.extractFromSlot(slot, 1, true);
      if (peek.isEmpty()) {
        return;
      }
    }
  }

  public void sort() {
    for (Map.Entry<Item, LinkedHashMap<Integer, Request>> e : map.entrySet()) {
      LinkedHashMap<Integer, Request> byDest = e.getValue();
      java.util.List<Map.Entry<Integer, Request>> entries =
          new java.util.ArrayList<>(byDest.entrySet());
      entries.sort(java.util.Comparator.comparingInt(a -> a.getValue().getPriority()));
      LinkedHashMap<Integer, Request> reordered = new LinkedHashMap<>();
      for (Map.Entry<Integer, Request> en : entries) {
        reordered.put(en.getKey(), en.getValue());
      }
      e.setValue(reordered);
    }
  }
  /** Old: batch.get(item) returned a List<Request>. */
  public java.util.List<Request> get(Item item) {
    LinkedHashMap<Integer, Request> byDest = map.get(item);
    if (byDest == null || byDest.isEmpty()) return java.util.Collections.emptyList();
    return new java.util.ArrayList<>(byDest.values());
  }

  /** Old: batch.put(item, List<Request>). */
  public void put(Item item, java.util.List<Request> requests) {
    if (requests == null) return;
    for (Request r : requests) {
      put(item, r);
    }
  }
  public java.util.Set<Item> keySet() { return map.keySet(); }
}