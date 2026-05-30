package com.lothrazar.storagenetwork.api.capabilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.api.EnumStorageDirection;
import com.lothrazar.storagenetwork.api.network.BlockEntityCradle;
import com.lothrazar.storagenetwork.api.batch.Batch;
import com.lothrazar.storagenetwork.api.batch.StackProvider;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Facade IConnectableLink for a Storage Cradle. Aggregates one wrapped IConnectableLink per held
 * slot (via CradleAdapterRegistry), so vanilla shulkers, AE2 cells, and RS disks all flow through
 * the same path. Empty / incompatible held slots are skipped.
 */
public class CapabilityConnectableCradle implements CapabilityConnectable {
  public static final Logger LOGGER = LogManager.getLogger();

  private final BlockEntityCradle tile;
  private int priority;

  public CapabilityConnectableCradle(BlockEntityCradle tile) {
    this.tile = tile;
  }

  private List<CapabilityConnectable> links() {
    return tile.getHeldLinks();
  }

  @Override
  public DimPos getPos() {
    return new DimPos(tile.getLevel(), tile.getBlockPos());
  }

  @Override
  public List<ItemStack> getStoredStacks(boolean isFiltered) {
    List<CapabilityConnectable> ls = links();
    if (ls.isEmpty()) {
      return Collections.emptyList();
    }
    List<ItemStack> result = new ArrayList<>();
    for (CapabilityConnectable l : ls) {
      result.addAll(l.getStoredStacks(isFiltered));
    }
    return result;
  }

  @Override
  public ItemStack insertStack(ItemStack stack, boolean simulate) {
    if (stack.isEmpty()) {
      return stack;
    }
    ItemStack remaining = stack;
    try {
      for (CapabilityConnectable l : links()) {
        remaining = l.insertStack(remaining, simulate);
        if (remaining.isEmpty()) {
          return ItemStack.EMPTY;
        }
      }
    }
    catch (Exception e) {
      LOGGER.error("Cradle insert error ", e);
      return stack;
    }
    return remaining;
  }

  @Override
  public ItemStack extractStack(ItemStackMatcher matcher, int size, boolean simulate) {
    if (size <= 0) {
      return ItemStack.EMPTY;
    }
    ItemStack first = ItemStack.EMPTY;
    int remaining = size;
    for (CapabilityConnectable l : links()) {
      ItemStack got = l.extractStack(matcher, remaining, simulate);
      if (got.isEmpty()) {
        continue;
      }
      if (first.isEmpty()) {
        first = got;
      }
      else if (ItemStack.isSameItemSameComponents(first, got)) {
        first.grow(got.getCount());
      }
      else {
        // Different item came back from another held storage; ignore and keep first.
        continue;
      }
      remaining = size - first.getCount();
      if (remaining <= 0) {
        break;
      }
    }
    return first;
  }

  @Override
  public int getEmptySlots() {
    int empty = 0;
    for (CapabilityConnectable l : links()) {
      empty += l.getEmptySlots();
    }
    return empty;
  }

  @Override
  public int getFilledSlots() {
    int filled = 0;
    for (CapabilityConnectable l : links()) {
      filled += l.getFilledSlots();
    }
    return filled;
  }

  @Override
  public int getTotalSlots() {
    int total = 0;
    for (CapabilityConnectable l : links()) {
      total += l.getTotalSlots();
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
    // Flat index across all held links' slot spaces.
    int cursor = slot;
    for (CapabilityConnectable l : links()) {
      int n = l.getTotalSlots();
      if (cursor < n) {
        return l.extractFromSlot(cursor, amount, simulate);
      }
      cursor -= n;
    }
    return ItemStack.EMPTY;
  }

  @Override
  public void addToStackProviderBatch(Batch<StackProvider> availableItems) {
    // Each held link contributes its own StackProviders; they're already wired to the right child IConnectableLink.
    for (CapabilityConnectable l : links()) {
      l.addToStackProviderBatch(availableItems);
    }
  }
}
