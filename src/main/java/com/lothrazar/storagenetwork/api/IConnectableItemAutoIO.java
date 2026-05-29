package com.lothrazar.storagenetwork.api;

import java.util.Collections;
import java.util.List;

import com.lothrazar.storagenetwork.api.batch.RequestBatch;
import com.lothrazar.storagenetwork.api.capabilities.ItemStackMatcher;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.api.capabilities.FilterItemStackHandler;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Only expose this capability if you want your cable/block to auto-export and import blocks controlled by the networks main. You could quite as well just expose {@link IConnectable} and do the
 * exporting/importing in your own update() method.
 * <p>
 * If you indeed want to add another exporting/importing cable in the style of the integrated ones, this might be for you. In all other cases, this is probably not what you want.
 */
public interface IConnectableItemAutoIO {

  void toggleNeedsRedstone();

  boolean needsRedstone();

  void needsRedstone(boolean in);

  /**
   * Return either IN or OUT here, but not BOTH. If you return BOTH expect weird things to happen.
   * <p>
   * View the EnumStorageDirection from the networks perspective, i.e.: - OUT means you are storing items in your storage based on the given auto export list. - IN means you are extracting items from
   * your storage in regular intervals.
   *
   * @return
   */
  EnumStorageDirection ioDirection();

  /**
   * This is called on your capability every time the network tries to insert a stack into your storage.
   * <p>
   * If your ioDirection is set to OUT, this should never get called, unless another malicious mod is doing it.
   *
   * @param stack
   *          The stack being inserted into your storage
   * @param simulate
   *          Whether or not this is just a simulation
   * @return The remainder of the stack if not all of it fit into your storage
   */
  ItemStack insertStack(ItemStack stack, boolean simulate);

  default IItemHandler getItemHandler() {
    return null;
  }

  default FilterItemStackHandler getFilters() {
    return null;
  }

  /**
   * Optional 'Stock Upgrade' mode. Meaning the export only exports if the target has less than this number, and only exports enough to meet it.
   * <p>
   * only for exports, currently.
   *
   * @return
   */
  default boolean isStockMode() {
    return false;
  }

  default boolean isOperationMode() {
    return false;
  }

  /**
   * Get transfer rate from 0-64. This is literally the amount of items that can be transferred per operation.
   *
   * @return max stacksize to transfer per operation
   */
  int getTransferRate();

  Direction facingInventory();

  /**
   * Storages with a higher priority (== lower number) are processed first. You probably want to add a way to configure the priority of your storage.
   *
   * @return Return the priority here
   */
  int getPriority();

  /**
   * Called every tick to see if an operation should be processed now, i.e. this can be used to add cooldown times or disable operations via redstone signal.
   *
   * @param connectablePos
   *          The position of your block, including the world
   * @param main
   *          The network main. Use this to e.g. query amount of items.
   * @return Whether or not this IConnectableLink should be processed this tick.
   */
  boolean canRunNow(DimPos connectablePos, TileMain main);

  RequestBatch runExport(TileMain main);

  void runImport(TileMain main);

  /**
   * If this block is used with an ioDirection of OUT and has its getSupportedTransferDirection set to OUT, then this list will be consolidated by the main and available items in the network matching
   * the {@link ItemStackMatcher}s in the list will be exported via the canTransfer() and transfer() methods above.
   * <p>
   * In other words: - Only implement this if you are making a main-controlled export cable (you shouldnt)
   *
   * @return
   */
  default List<ItemStackMatcher> getAutoExportList() {
    return Collections.emptyList();
  }
}
