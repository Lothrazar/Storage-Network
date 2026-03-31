package com.lothrazar.storagenetwork.util;

import com.lothrazar.storagenetwork.api.IConnectableItemAutoIO;
import com.lothrazar.storagenetwork.api.IConnectableLink;
import net.minecraft.world.item.ItemStack;

public class Request {

  private Integer count = 0;
  private final IConnectableItemAutoIO storage;
  /** Stable id per concrete export destination within a batch/tick. */
  private final int targetKey;

  /** Backwards-compatible: falls back to identity hash as destination key. */
  public Request(IConnectableItemAutoIO storage) {
    this(storage, System.identityHashCode(storage));
  }

  /** Preferred: caller provides stable destination key. */
  public Request(IConnectableItemAutoIO storage, int targetKey) {
    this.count = storage.getTransferRate();
    this.storage = storage;
    this.targetKey = targetKey;
  }

  public void setCount(Integer count) {
    this.count = count;
  }

  public Integer getCount() {
    return count;
  }

  public int getPriority() {
    return storage.getPriority();
  }

  public int getTargetKey() {
    return targetKey;
  }

  public Boolean insertStack(IConnectableLink providerStorage, int slot) {
    final int requested = getCount();
    ItemStack simulatedExtractedStack = providerStorage.extractFromSlot(slot, requested, true);

    if (simulatedExtractedStack.isEmpty()) {
      return false;
    }
    int movedItems = 0;
    ItemStack simulatedInsertedStack = storage.insertStack(simulatedExtractedStack, true);
    if (simulatedInsertedStack.isEmpty()) {
      movedItems = simulatedExtractedStack.getCount();
      setCount(Math.max(0, requested - movedItems));
    }
    else {
      movedItems = simulatedExtractedStack.getCount() - simulatedInsertedStack.getCount();
      setCount(Math.max(0, requested - movedItems));
    }
    // real extraction
    ItemStack realExtractedStack = providerStorage.extractFromSlot(slot, movedItems, false);
    storage.insertStack(realExtractedStack, false);
    return getCount() == 0;
  }
}
