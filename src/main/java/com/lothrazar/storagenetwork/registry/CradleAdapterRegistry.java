package com.lothrazar.storagenetwork.registry;

import java.util.ArrayList;
import java.util.List;
import com.lothrazar.storagenetwork.api.IConnectableLink;
import com.lothrazar.storagenetwork.block.cradle.TileStorageCradle;
import com.lothrazar.storagenetwork.api.CradleAdapter;
import net.minecraft.world.item.ItemStack;

public final class CradleAdapterRegistry {

  private static final List<CradleAdapter> ADAPTERS = new ArrayList<>();

  private CradleAdapterRegistry() {}

  public static void register(CradleAdapter adapter) {
    ADAPTERS.add(adapter);
  }

  public static boolean accepts(ItemStack heldStack) {
    if (heldStack.isEmpty()) {
      return false;
    }
    for (CradleAdapter a : ADAPTERS) {
      if (a.accepts(heldStack)) {
        return true;
      }
    }
    return false;
  }

  public static IConnectableLink wrap(ItemStack heldStack, TileStorageCradle cradle) {
    if (heldStack.isEmpty()) {
      return null;
    }
    for (CradleAdapter a : ADAPTERS) {
      if (a.accepts(heldStack)) {
        IConnectableLink link = a.wrap(heldStack, cradle);
        if (link != null) {
          return link;
        }
      }
    }
    return null;
  }
}
