package com.lothrazar.storagenetwork.api.capabilities;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Bridges a legacy {@link IItemHandler} to the transactional {@link ResourceHandler} API.
 * NeoForge 26.1 only ships the new-to-old adapter ({@code IItemHandler#of}); this is the
 * old-to-new direction, needed because our block entities' capability providers still hand out
 * legacy {@link IItemHandlerModifiable} handlers under the new {@code Capabilities.Item.BLOCK} key.
 * Rollback on transaction abort is supported only when the wrapped handler is modifiable.
 */
public class ItemHandlerResourceAdapter extends SnapshotJournal<List<ItemStack>> implements ResourceHandler<ItemResource> {

  private final IItemHandler handler;

  public ItemHandlerResourceAdapter(IItemHandler handler) {
    this.handler = handler;
  }

  @Override
  public int size() {
    return handler.getSlots();
  }

  @Override
  public ItemResource getResource(int index) {
    ItemStack stack = handler.getStackInSlot(index);
    return stack.isEmpty() ? ItemResource.EMPTY : ItemResource.of(stack);
  }

  @Override
  public long getAmountAsLong(int index) {
    return handler.getStackInSlot(index).getCount();
  }

  @Override
  public long getCapacityAsLong(int index, ItemResource resource) {
    ItemStack probe = resource.isEmpty() ? handler.getStackInSlot(index) : resource.toStack(1);
    int maxStackSize = probe.isEmpty() ? 64 : probe.getMaxStackSize();
    return Math.min(handler.getSlotLimit(index), maxStackSize);
  }

  @Override
  public boolean isValid(int index, ItemResource resource) {
    return !resource.isEmpty() && handler.isItemValid(index, resource.toStack(1));
  }

  @Override
  public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
    if (resource.isEmpty() || amount <= 0) {
      return 0;
    }
    updateSnapshots(transaction);
    ItemStack remainder = handler.insertItem(index, resource.toStack(amount), false);
    return amount - remainder.getCount();
  }

  @Override
  public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
    if (resource.isEmpty() || amount <= 0) {
      return 0;
    }
    ItemStack current = handler.getStackInSlot(index);
    if (current.isEmpty() || !ItemResource.of(current).equals(resource)) {
      return 0;
    }
    updateSnapshots(transaction);
    return handler.extractItem(index, amount, false).getCount();
  }

  @Override
  protected List<ItemStack> createSnapshot() {
    List<ItemStack> snapshot = new ArrayList<>(handler.getSlots());
    for (int i = 0; i < handler.getSlots(); i++) {
      snapshot.add(handler.getStackInSlot(i).copy());
    }
    return snapshot;
  }

  @Override
  protected void revertToSnapshot(List<ItemStack> snapshot) {
    if (!(handler instanceof IItemHandlerModifiable modifiable)) {
      return;
    }
    for (int i = 0; i < snapshot.size() && i < handler.getSlots(); i++) {
      modifiable.setStackInSlot(i, snapshot.get(i));
    }
  }
}
