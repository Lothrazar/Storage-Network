package com.lothrazar.storagenetwork.block.main;

import java.util.HashMap;
import java.util.Map;
import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.api.util.UtilInventory;
import net.minecraft.world.item.ItemStack;

/**
 * Remember previous input locations for items coming in and re use them. Saves efficiency and also keeps similar items in the same place. So when the first of a new material goes in, the spot is
 * remembered for the next one coming in. Originally added in 1.12.2 for import cables along with testing with BDS&M storage crates and barrels so credit to funwayguy for the original idea. Rebuilt
 * int mc1.18.2 and also linked to user/GUI screen inserts for Sapphy and others.
 * 
 * @author lothr
 *
 */
public class NetworkCache {

  Map<String, DimPos> importCache = new HashMap<>();

  boolean hasCachedSlot(ItemStack stack) {
    return importCache.containsKey(UtilInventory.getStackKey(stack));
  }

  int size() {
    return this.importCache.size();
  }

  DimPos getCachedSlot(ItemStack stack) {
    return importCache.get(UtilInventory.getStackKey(stack));
  }

  void clearCache() {
    importCache = new HashMap<>();
  }

  public void remove(String key) {
    this.importCache.remove(key);
  }

  public void put(String key, DimPos pos) {
    this.importCache.put(key, pos);
  }
}
