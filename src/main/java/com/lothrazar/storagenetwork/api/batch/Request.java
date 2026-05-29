package com.lothrazar.storagenetwork.api.batch;

import com.lothrazar.storagenetwork.api.IConnectableItemAutoIO;
import com.lothrazar.storagenetwork.api.IConnectableLink;
import net.minecraft.world.item.ItemStack;

public class Request {

  private Integer count = 0;
  private IConnectableItemAutoIO storage;

  public Request(IConnectableItemAutoIO storage) {
    this.count = storage.getTransferRate();
    this.storage = storage;
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

  public Boolean insertStack(IConnectableLink providerStorage, int slot) {
    ItemStack simulatedExtractedStack = providerStorage.extractFromSlot(slot, getCount(), true);
    if (simulatedExtractedStack.isEmpty()) {
      return false;
    }
    int movedItems = 0;
    ItemStack simulatedInsertedStack = storage.insertStack(simulatedExtractedStack, true);
    if (simulatedInsertedStack.isEmpty()) {
      movedItems = getCount();
      setCount(0);
    }
    else {
      movedItems = simulatedExtractedStack.getCount() - simulatedInsertedStack.getCount();
      setCount(movedItems);
    }
    // real extraction
    ItemStack realExtractedStack = providerStorage.extractFromSlot(slot, movedItems, false);
    storage.insertStack(realExtractedStack, false);
    // Determine the amount of items moved in the stack
    if (getCount() == 0) {
      return true;
    }
    return false;
  }
}
