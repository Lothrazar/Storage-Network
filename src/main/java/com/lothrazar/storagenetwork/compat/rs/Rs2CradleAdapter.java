package com.lothrazar.storagenetwork.compat.rs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.api.EnumStorageDirection;
import com.lothrazar.storagenetwork.api.capabilities.CapabilityConnectable;
import com.lothrazar.storagenetwork.api.capabilities.ItemStackMatcher;
import com.lothrazar.storagenetwork.block.cradle.TileCradle;
import com.lothrazar.storagenetwork.compat.CradleAdapter;
import com.lothrazar.storagenetwork.api.batch.Batch;
import com.lothrazar.storagenetwork.api.batch.StackProvider;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.storage.SerializableStorage;
import com.refinedmods.refinedstorage.common.api.storage.StorageContainerItem;
import com.refinedmods.refinedstorage.common.api.storage.StorageInfo;
import com.refinedmods.refinedstorage.common.api.storage.StorageRepository;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Adapter for Refined Storage 2.0.8 storage disks held inside a Storage Cradle.
 *
 * RS architecture note: disk contents live in a per-Level StorageRepository keyed by a UUID stored
 * on the disk item. The disk item itself is just a pointer. We therefore look up the storage fresh
 * on every operation (lazy & cheap â€” single map lookup).
 */
public class Rs2CradleAdapter implements CradleAdapter {

  @Override
  public boolean accepts(ItemStack heldStack) {
    // Cheap, pure-item check â€” safe to call on client side.
    return heldStack.getItem() instanceof StorageContainerItem;
  }

  @Override
  public CapabilityConnectable wrap(ItemStack heldStack, TileCradle cradle) {
    Level level = cradle.getLevel();
    if (level == null || level.isClientSide()) {
      return null;
    }
    if (!(heldStack.getItem() instanceof StorageContainerItem sci)) {
      return null;
    }
    StorageRepository repo = RefinedStorageApi.INSTANCE.getStorageRepository(level);
    if (repo == null) {
      return null;
    }
    // resolve() returns empty for uninitialized disks (no repository entry yet).
    Optional<SerializableStorage> opt = sci.resolve(repo, heldStack);
    if (opt.isEmpty()) {
      return null;
    }
    return new Link(cradle, heldStack, sci, repo, opt.get());
  }

  private static final class Link implements CapabilityConnectable {

    private final TileCradle cradle;
    private final ItemStack diskStack;
    private final StorageContainerItem diskItem;
    private final StorageRepository repository;
    private final SerializableStorage storage;
    private int priority;

    private Link(TileCradle cradle, ItemStack diskStack, StorageContainerItem diskItem,
        StorageRepository repository, SerializableStorage storage) {
      this.cradle = cradle;
      this.diskStack = diskStack;
      this.diskItem = diskItem;
      this.repository = repository;
      this.storage = storage;
    }

    private static int safeInt(long v) {
      return (int) Math.min(v, Integer.MAX_VALUE);
    }

    @Override
    public DimPos getPos() {
      return new DimPos(cradle.getLevel(), cradle.getBlockPos());
    }

    @Override
    public List<ItemStack> getStoredStacks(boolean isFiltered) {
      Collection<ResourceAmount> all = storage.getAll();
      if (all.isEmpty()) {
        return List.of();
      }
      List<ItemStack> result = new ArrayList<>();
      for (ResourceAmount ra : all) {
        if (!(ra.resource() instanceof ItemResource ir)) {
          continue;
        }
        long amount = ra.amount();
        if (amount <= 0) {
          continue;
        }
        // Disks can hold more than one ItemStack-worth of a single type. Chunk into stacks.
        int per = ir.toItemStack().getMaxStackSize();
        if (per <= 0) {
          per = 64;
        }
        long remaining = amount;
        while (remaining > 0) {
          int n = (int) Math.min(remaining, per);
          result.add(ir.toItemStack(n));
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
      ItemResource res = ItemResource.ofItemStack(stack);
      long inserted = storage.insert(res, stack.getCount(),
          simulate ? Action.SIMULATE : Action.EXECUTE, Actor.EMPTY);
      if (!simulate && inserted > 0) {
        repository.markAsChanged();
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
      for (ResourceAmount ra : storage.getAll()) {
        if (!(ra.resource() instanceof ItemResource ir)) {
          continue;
        }
        ItemStack probe = ir.toItemStack(1);
        if (!matcher.match(probe)) {
          continue;
        }
        long extracted = storage.extract(ir, size,
            simulate ? Action.SIMULATE : Action.EXECUTE, Actor.EMPTY);
        if (extracted <= 0) {
          continue;
        }
        if (!simulate) {
          repository.markAsChanged();
        }
        return ir.toItemStack(safeInt(extracted));
      }
      return ItemStack.EMPTY;
    }

    private StorageInfo infoOrUnknown() {
      Optional<StorageInfo> opt = diskItem.getInfo(repository, diskStack);
      return opt.orElse(StorageInfo.UNKNOWN);
    }

    @Override
    public int getEmptySlots() {
      // RS disks aren't slot-based. Approximate: report "remaining bytes" as remaining slots.
      // Capacity 0 (UNKNOWN) â†’ 0 empty.
      StorageInfo info = infoOrUnknown();
      long free = info.capacity() - info.stored();
      if (free <= 0) {
        return 0;
      }
      // Compress huge numbers into something sensible for the comparator math.
      return safeInt(free);
    }

    @Override
    public int getFilledSlots() {
      return safeInt(infoOrUnknown().stored());
    }

    @Override
    public int getTotalSlots() {
      return safeInt(infoOrUnknown().capacity());
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
      // "Slot" is a flat index into the resource list. Iterate and pick the Nth resource.
      int idx = 0;
      for (ResourceAmount ra : storage.getAll()) {
        if (!(ra.resource() instanceof ItemResource ir)) {
          continue;
        }
        if (idx == slot) {
          long extracted = storage.extract(ir, amount,
              simulate ? Action.SIMULATE : Action.EXECUTE, Actor.EMPTY);
          if (extracted <= 0) {
            return ItemStack.EMPTY;
          }
          if (!simulate) {
            repository.markAsChanged();
          }
          return ir.toItemStack(safeInt(extracted));
        }
        idx++;
      }
      return ItemStack.EMPTY;
    }

    @Override
    public void addToStackProviderBatch(Batch<StackProvider> availableItems) {
      int idx = 0;
      for (ResourceAmount ra : storage.getAll()) {
        ResourceKey key = ra.resource();
        if (!(key instanceof ItemResource ir)) {
          continue;
        }
        if (ra.amount() > 0) {
          availableItems.put(ir.toItemStack(1).getItem(), new StackProvider(this, idx));
        }
        idx++;
      }
    }
  }
}
