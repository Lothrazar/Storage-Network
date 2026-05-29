package com.lothrazar.storagenetwork.block.exchange;

import com.lothrazar.library.cap.ItemStackHandlerEx;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.api.capabilities.ItemStackMatcherDefault;
import com.lothrazar.storagenetwork.registry.ConfigRegistry;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author ajsnarr98 Created MasterItemStackHandler
 * 
 * @info
 * 
 *       ItemStackHandler used for interfacing with TileMain like a chest.
 * 
 *       used by TileExchange
 * 
 *       use restricted in CapabilityConnectableLink::extractStack
 * 
 *       https://github.com/ajsnarr98/Storage-Network/tree/ajsnarr98-inventory
 * 
 */
public class ExchangeItemStackHandler extends ItemStackHandlerEx {

  public static final Logger LOGGER = LogManager.getLogger();
  TileMain tileMain;

  public ExchangeItemStackHandler() {
    super(Math.min(5000, ConfigRegistry.EXCHANGEBUFFER.get()));
    update();
  }

  public void setMain(TileMain main) {
    this.tileMain = main;
    update();
  }

  /**
   * Updates items in the handler based on outside storage.
   */
  public void update() {
    if (tileMain == null || tileMain.getLevel() == null) {
      return;
    }
    try {
      //      StorageNetwork.log("exchange update started");
      this.stacks.clear();
      int i = 0;
      for (ItemStack stack : tileMain.getNetwork().getStacks()) {
        if (i >= this.stacks.size()) {
          break;
        }
        this.stacks.set(i, stack);
        i++;
      }
      //      StorageNetwork.log("exchange updated " + i);
    }
    catch (Exception e) {
      LOGGER.error("Exchange update error ", e);
    }
  }

  /**
   * @param slot
   *          may not end up in the exact slot specified
   * 
   * @return The remaining ItemStack that was not inserted (if the entire stack is accepted, then return an empty ItemStack). May be the same as the input ItemStack if unchanged, otherwise a new
   *         ItemStack. The returned ItemStack can be safely modified after.
   */
  @Override
  public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
    if (stack.isEmpty() || tileMain == null || !isItemValid(slot, stack)) {
      return stack;
    }
    validateSlotIndex(slot);
    try {
      int remaining = tileMain.insertStack(stack, simulate);
      //  StorageNetwork.log("exchange: insertItem " + stack + " remain " + remaining);
      if (remaining > 0) {
        // if failed, refresh whole list
        update();
        return stack.copyWithCount(remaining);
      }
    }
    catch (Exception e) {
      LOGGER.error("insertStack error ", e);
    }
    update();
    return ItemStack.EMPTY;
  }

  @Override
  public ItemStack extractItem(int slot, int amount, boolean simulate) {
    if (tileMain == null) {
      //            super.extractItem(slot, amount, simulate);
      return ItemStack.EMPTY;
    }
    ItemStackMatcherDefault matcher = new ItemStackMatcherDefault(getStackInSlot(slot));
    //    StorageNetwork.log("extractItem " + matcher.getStack());
    ItemStack stack = tileMain.request(matcher, amount, simulate); // Stackoverflow?
    update();
    // StorageNetwork.log("exchange: extractItem; after " + stack);
    return stack;
  }

  @Override
  protected void onLoad() {
    update();
  }
}
