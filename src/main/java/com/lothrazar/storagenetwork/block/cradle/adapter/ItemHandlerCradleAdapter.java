package com.lothrazar.storagenetwork.block.cradle.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.api.EnumStorageDirection;
import com.lothrazar.storagenetwork.api.IConnectableLink;
import com.lothrazar.storagenetwork.api.IItemStackMatcher;
import com.lothrazar.storagenetwork.block.cradle.CradleAdapter;
import com.lothrazar.storagenetwork.block.cradle.TileStorageCradle;
import com.lothrazar.storagenetwork.util.Batch;
import com.lothrazar.storagenetwork.util.StackProvider;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/** Built-in adapter  anything exposing Capabilities.ItemHandler.ITEM (vanilla shulkers, modded inventories). */
public class ItemHandlerCradleAdapter implements CradleAdapter {

  @Override
  public boolean accepts(ItemStack heldStack) {
    return heldStack.getCapability(Capabilities.ItemHandler.ITEM) != null;
  }

  @Override
  public IConnectableLink wrap(ItemStack heldStack, TileStorageCradle cradle) {
    IItemHandler h = heldStack.getCapability(Capabilities.ItemHandler.ITEM);
    if (h == null) {
      return null;
    }
    return new Link(cradle, h);
  }

  private static final class Link implements IConnectableLink {

    private final TileStorageCradle cradle;
    private final IItemHandler handler;

    private Link(TileStorageCradle cradle, IItemHandler handler) {
      this.cradle = cradle;
      this.handler = handler;
    }

    @Override
    public DimPos getPos() {
      return new DimPos(cradle.getLevel(), cradle.getBlockPos());
    }

    @Override
    public List<ItemStack> getStoredStacks(boolean isFiltered) {
      List<ItemStack> result = new ArrayList<>();
      for (int slot = 0; slot < handler.getSlots(); slot++) {
        ItemStack stack = handler.getStackInSlot(slot);
        if (stack != null && !stack.isEmpty()) {
          result.add(stack.copy());
        }
      }
      return result.isEmpty() ? Collections.emptyList() : result;
    }

    @Override
    public ItemStack insertStack(ItemStack stack, boolean simulate) {
      if (stack.isEmpty()) {
        return stack;
      }
      return ItemHandlerHelper.insertItemStacked(handler, stack, simulate);
    }

    @Override
    public ItemStack extractStack(IItemStackMatcher matcher, int size, boolean simulate) {
      if (size <= 0) {
        return ItemStack.EMPTY;
      }
      ItemStack first = ItemStack.EMPTY;
      int remaining = size;
      for (int slot = 0; slot < handler.getSlots(); slot++) {
        ItemStack stack = handler.extractItem(slot, remaining, true);
        if (stack == null || stack.isEmpty()) {
          continue;
        }
        if (first.isEmpty()) {
          if (!matcher.match(stack)) {
            continue;
          }
          first = stack.copy();
        }
        else if (!ItemStack.isSameItemSameComponents(first, stack)) {
          continue;
        }
        int toExtract = Math.min(stack.getCount(), remaining);
        ItemStack extracted = handler.extractItem(slot, toExtract, simulate);
        remaining -= extracted.getCount();
        if (remaining <= 0) {
          break;
        }
      }
      int extractCount = size - remaining;
      if (!first.isEmpty() && extractCount > 0) {
        first.setCount(extractCount);
      }
      return first;
    }

    @Override
    public int getEmptySlots() {
      int empty = 0;
      for (int slot = 0; slot < handler.getSlots(); slot++) {
        if (handler.getStackInSlot(slot).isEmpty()) {
          empty++;
        }
      }
      return empty;
    }

    @Override
    public int getFilledSlots() {
      int filled = 0;
      for (int slot = 0; slot < handler.getSlots(); slot++) {
        if (!handler.getStackInSlot(slot).isEmpty()) {
          filled++;
        }
      }
      return filled;
    }

    @Override
    public int getTotalSlots() {
      return handler.getSlots();
    }

    @Override
    public int getPriority() {
      return 0;
    }

    @Override
    public void setPriority(int value) {}

    @Override
    public EnumStorageDirection getSupportedTransferDirection() {
      return EnumStorageDirection.BOTH;
    }

    @Override
    public void setFilter(int value, ItemStack copy) {}

    @Override
    public ItemStack extractFromSlot(int slot, int amount, boolean simulate) {
      if (slot < 0 || slot >= handler.getSlots()) {
        return ItemStack.EMPTY;
      }
      return handler.extractItem(slot, amount, simulate);
    }

    @Override
    public void addToStackProviderBatch(Batch<StackProvider> availableItems) {
      for (int slot = 0; slot < handler.getSlots(); slot++) {
        ItemStack stack = handler.extractItem(slot, 1, true);
        if (stack != null && !stack.isEmpty()) {
          availableItems.put(stack.getItem(), new StackProvider(this, slot));
        }
      }
    }
  }
}
