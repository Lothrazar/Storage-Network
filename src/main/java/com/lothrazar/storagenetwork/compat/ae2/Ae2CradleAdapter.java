package com.lothrazar.storagenetwork.compat.ae2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.api.EnumStorageDirection;
import com.lothrazar.storagenetwork.api.capabilities.CapabilityConnectable;
import com.lothrazar.storagenetwork.api.capabilities.ItemStackMatcher;
import com.lothrazar.storagenetwork.block.cradle.TileStorageCradle;
import com.lothrazar.storagenetwork.compat.CradleAdapter;
import com.lothrazar.storagenetwork.api.batch.Batch;
import com.lothrazar.storagenetwork.api.batch.StackProvider;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.world.item.ItemStack;

/** Adapter for AE2 storage cells held inside a Storage Cradle. */
public class Ae2CradleAdapter implements CradleAdapter {

  @Override
  public boolean accepts(ItemStack heldStack) {
    return StorageCells.isCellHandled(heldStack);
  }

  @Override
  public CapabilityConnectable wrap(ItemStack heldStack, TileStorageCradle cradle) {
    // ISaveProvider gets called by AE2 whenever the cell wants its NBT flushed.
    // Marking the cradle dirty here ensures the held ItemStack's component changes are saved.
    ISaveProvider saver = cradle::setChanged;
    StorageCell cell = StorageCells.getCellInventory(heldStack, saver);
    if (cell == null) {
      return null;
    }
    return new Link(cradle, cell);
  }

  private static final class Link implements CapabilityConnectable {

    private final TileStorageCradle cradle;
    private final StorageCell cell;
    private int priority;

    private Link(TileStorageCradle cradle, StorageCell cell) {
      this.cradle = cradle;
      this.cell = cell;
    }

    private static long clampToInt(long v) {
      return Math.min(v, Integer.MAX_VALUE);
    }

    private static int safeInt(long v) {
      return (int) clampToInt(v);
    }

    @Override
    public DimPos getPos() {
      return new DimPos(cradle.getLevel(), cradle.getBlockPos());
    }

    @Override
    public List<ItemStack> getStoredStacks(boolean isFiltered) {
      KeyCounter counter = new KeyCounter();
      cell.getAvailableStacks(counter);
      if (counter.isEmpty()) {
        return Collections.emptyList();
      }
      List<ItemStack> result = new ArrayList<>();
      for (Object2LongMap.Entry<AEKey> e : counter) {
        AEKey k = e.getKey();
        if (!(k instanceof AEItemKey ik)) {
          continue;
        }
        long amount = e.getLongValue();
        if (amount <= 0) {
          continue;
        }
        // AE2 amounts can exceed int; chunk into ItemStack-sized representations.
        long remaining = amount;
        int per = ik.getMaxStackSize();
        if (per <= 0) {
          per = 64;
        }
        while (remaining > 0) {
          int n = (int) Math.min(remaining, per);
          result.add(ik.toStack(n));
          remaining -= n;
        }
      }
      return result;
    }

    @Override
    public ItemStack insertStack(ItemStack stack, boolean simulate) {
      if (stack.isEmpty()) {
        return stack;
      }
      AEItemKey key = AEItemKey.of(stack);
      if (key == null) {
        return stack;
      }
      long inserted = cell.insert(key, stack.getCount(),
          simulate ? Actionable.SIMULATE : Actionable.MODULATE, IActionSource.empty());
      if (!simulate && inserted > 0) {
        cell.persist();
      }
      if (inserted >= stack.getCount()) {
        return ItemStack.EMPTY;
      }
      ItemStack remainder = stack.copy();
      remainder.shrink((int) inserted);
      return remainder;
    }

    @Override
    public ItemStack extractStack(ItemStackMatcher matcher, int size, boolean simulate) {
      if (size <= 0) {
        return ItemStack.EMPTY;
      }
      KeyCounter counter = new KeyCounter();
      cell.getAvailableStacks(counter);
      for (Object2LongMap.Entry<AEKey> e : counter) {
        AEKey k = e.getKey();
        if (!(k instanceof AEItemKey ik)) {
          continue;
        }
        ItemStack probe = ik.toStack(1);
        if (!matcher.match(probe)) {
          continue;
        }
        long extracted = cell.extract(ik, size,
            simulate ? Actionable.SIMULATE : Actionable.MODULATE, IActionSource.empty());
        if (extracted <= 0) {
          continue;
        }
        if (!simulate) {
          cell.persist();
        }
        return ik.toStack(safeInt(extracted));
      }
      return ItemStack.EMPTY;
    }

    @Override
    public int getEmptySlots() {
      // AE2 cells don't have slots; report capacity in "types" terms so the comparator math makes sense.
      // Approximation: total types - filled types, never negative.
      int total = getTotalSlots();
      int filled = getFilledSlots();
      return Math.max(0, total - filled);
    }

    @Override
    public int getFilledSlots() {
      KeyCounter counter = new KeyCounter();
      cell.getAvailableStacks(counter);
      return counter.size();
    }

    @Override
    public int getTotalSlots() {
      // No direct "type cap" accessor on MEStorage; report a fixed reasonable value so comparators
      // produce non-zero output. Real cells are usually 63 types.
      return 63;
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
      // "Slot" is a flat index into the type list. Iterate and pick the Nth type.
      KeyCounter counter = new KeyCounter();
      cell.getAvailableStacks(counter);
      int idx = 0;
      for (Object2LongMap.Entry<AEKey> e : counter) {
        AEKey k = e.getKey();
        if (!(k instanceof AEItemKey ik)) {
          continue;
        }
        if (idx == slot) {
          long extracted = cell.extract(ik, amount,
              simulate ? Actionable.SIMULATE : Actionable.MODULATE, IActionSource.empty());
          if (extracted <= 0) {
            return ItemStack.EMPTY;
          }
          if (!simulate) {
            cell.persist();
          }
          return ik.toStack(safeInt(extracted));
        }
        idx++;
      }
      return ItemStack.EMPTY;
    }

    @Override
    public void addToStackProviderBatch(Batch<StackProvider> availableItems) {
      KeyCounter counter = new KeyCounter();
      cell.getAvailableStacks(counter);
      int idx = 0;
      for (Object2LongMap.Entry<AEKey> e : counter) {
        AEKey k = e.getKey();
        if (!(k instanceof AEItemKey ik)) {
          continue;
        }
        if (e.getLongValue() > 0) {
          availableItems.put(ik.getItem(), new StackProvider(this, idx));
        }
        idx++;
      }
    }
  }
}
