package com.lothrazar.storagenetwork.capability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.api.EnumStorageDirection;
import com.lothrazar.storagenetwork.api.IConnectableLink;
import com.lothrazar.storagenetwork.api.IItemStackMatcher;
import com.lothrazar.storagenetwork.block.cradle.TileStorageCradle;
import com.lothrazar.storagenetwork.util.Batch;
import com.lothrazar.storagenetwork.util.StackProvider;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/**
 * Link capability backed by the union of IItemHandlers from each ItemStack held by the cradle.
 * Each slot's handler is queried independently — empty/incompatible held slots are skipped.
 */
public class CapabilityCradleLink implements IConnectableLink {

  private final TileStorageCradle tile;
  private int priority;

  public CapabilityCradleLink(TileStorageCradle tile) {
    this.tile = tile;
  }

  private List<IItemHandler> handlers() {
    return tile.getHeldHandlers();
  }

  @Override
  public DimPos getPos() {
    return new DimPos(tile.getLevel(), tile.getBlockPos());
  }

  @Override
  public List<ItemStack> getStoredStacks(boolean isFiltered) {
    List<IItemHandler> hs = handlers();
    if (hs.isEmpty()) {
      return Collections.emptyList();
    }
    List<ItemStack> result = new ArrayList<>();
    for (IItemHandler h : hs) {
      for (int slot = 0; slot < h.getSlots(); slot++) {
        ItemStack stack = h.getStackInSlot(slot);
        if (stack == null || stack.isEmpty()) {
          continue;
        }
        result.add(stack.copy());
      }
    }
    return result;
  }

  @Override
  public ItemStack insertStack(ItemStack stack, boolean simulate) {
    List<IItemHandler> hs = handlers();
    if (hs.isEmpty() || stack.isEmpty()) {
      return stack;
    }
    ItemStack remaining = stack;
    try {
      for (IItemHandler h : hs) {
        remaining = ItemHandlerHelper.insertItemStacked(h, remaining, simulate);
        if (remaining.isEmpty()) {
          return ItemStack.EMPTY;
        }
      }
    }
    catch (Exception e) {
      StorageNetworkMod.LOGGER.error("Cradle insert error ", e);
      return stack;
    }
    return remaining;
  }

  @Override
  public ItemStack extractStack(IItemStackMatcher matcher, int size, boolean simulate) {
    if (size <= 0) {
      return ItemStack.EMPTY;
    }
    List<IItemHandler> hs = handlers();
    if (hs.isEmpty()) {
      return ItemStack.EMPTY;
    }
    ItemStack firstMatchedStack = ItemStack.EMPTY;
    int remaining = size;
    for (IItemHandler h : hs) {
      for (int slot = 0; slot < h.getSlots(); slot++) {
        ItemStack stack = h.extractItem(slot, remaining, true);
        if (stack == null || stack.isEmpty()) {
          continue;
        }
        if (firstMatchedStack.isEmpty()) {
          if (!matcher.match(stack)) {
            continue;
          }
          firstMatchedStack = stack.copy();
        }
        else {
          if (!ItemStack.isSameItemSameComponents(firstMatchedStack, stack)) {
            continue;
          }
        }
        int toExtract = Math.min(stack.getCount(), remaining);
        ItemStack extractedStack = h.extractItem(slot, toExtract, simulate);
        remaining -= extractedStack.getCount();
        if (remaining <= 0) {
          break;
        }
      }
      if (remaining <= 0) {
        break;
      }
    }
    int extractCount = size - remaining;
    if (!firstMatchedStack.isEmpty() && extractCount > 0) {
      firstMatchedStack.setCount(extractCount);
    }
    return firstMatchedStack;
  }

  @Override
  public int getEmptySlots() {
    int empty = 0;
    for (IItemHandler h : handlers()) {
      for (int slot = 0; slot < h.getSlots(); slot++) {
        ItemStack stack = h.getStackInSlot(slot);
        if (stack == null || stack.isEmpty()) {
          empty++;
        }
      }
    }
    return empty;
  }

  @Override
  public int getFilledSlots() {
    int filled = 0;
    for (IItemHandler h : handlers()) {
      for (int slot = 0; slot < h.getSlots(); slot++) {
        ItemStack stack = h.getStackInSlot(slot);
        if (stack != null && !stack.isEmpty()) {
          filled++;
        }
      }
    }
    return filled;
  }

  @Override
  public int getTotalSlots() {
    int total = 0;
    for (IItemHandler h : handlers()) {
      total += h.getSlots();
    }
    return total;
  }

  @Override
  public int getPriority() {
    return priority;
  }

  @Override
  public void setPriority(int value) {
    this.priority = value;
  }

  @Override
  public EnumStorageDirection getSupportedTransferDirection() {
    return EnumStorageDirection.BOTH;
  }

  @Override
  public void setFilter(int value, ItemStack copy) {}

  @Override
  public ItemStack extractFromSlot(int slot, int amount, boolean simulate) {
    // slot here is a flat index across all held handlers
    int cursor = slot;
    for (IItemHandler h : handlers()) {
      int n = h.getSlots();
      if (cursor < n) {
        return h.extractItem(cursor, amount, simulate);
      }
      cursor -= n;
    }
    return ItemStack.EMPTY;
  }

  @Override
  public void addToStackProviderBatch(Batch<StackProvider> availableItems) {
    int flatIndex = 0;
    for (IItemHandler h : handlers()) {
      for (int slot = 0; slot < h.getSlots(); slot++) {
        ItemStack stack = h.extractItem(slot, 1, true);
        if (stack != null && !stack.isEmpty()) {
          availableItems.put(stack.getItem(), new StackProvider(this, flatIndex));
        }
        flatIndex++;
      }
    }
  }
}
