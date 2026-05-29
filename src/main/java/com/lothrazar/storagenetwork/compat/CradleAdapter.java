package com.lothrazar.storagenetwork.compat;

import com.lothrazar.storagenetwork.api.capabilities.CapabilityConnectable;
import com.lothrazar.storagenetwork.block.cradle.TileStorageCradle;
import net.minecraft.world.item.ItemStack;

/**
 * Strategy for treating an arbitrary ItemStack as network storage when held by a Storage Cradle.
 * Built-in: IItemHandler-backed items (vanilla shulker boxes, mod inventories that expose the cap).
 * Compat: AE2 cells, Refined Storage disks, etc. - registered from gated bootstrap classes.
 */
public interface CradleAdapter {

  /** Cheap predicate. Called from slot validation, container shift-clicks, GUI mayPlace. */
  boolean accepts(ItemStack heldStack);

  /**
   * Wrap the held stack as an IConnectableLink. Called per-operation; implementations should be cheap.
   * Returning null is treated the same as accepts() returning false.
   */
  CapabilityConnectable wrap(ItemStack heldStack, TileStorageCradle cradle);
}
