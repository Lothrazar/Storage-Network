package com.lothrazar.storagenetwork.api.batch;

import com.lothrazar.storagenetwork.api.capabilities.CapabilityConnectable;

public class StackProvider {

  CapabilityConnectable storage;
  int slot;

  public StackProvider(CapabilityConnectable storage, int slot) {
    this.storage = storage;
    this.slot = slot;
  }

  public CapabilityConnectable getStorage() {
    return storage;
  }

  public int getSlot() {
    return slot;
  }
}
