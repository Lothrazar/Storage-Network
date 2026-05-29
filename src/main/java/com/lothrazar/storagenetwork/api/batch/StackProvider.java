package com.lothrazar.storagenetwork.api.batch;

import com.lothrazar.storagenetwork.api.IConnectableLink;

public class StackProvider {

  IConnectableLink storage;
  int slot;

  public StackProvider(IConnectableLink storage, int slot) {
    this.storage = storage;
    this.slot = slot;
  }

  public IConnectableLink getStorage() {
    return storage;
  }

  public int getSlot() {
    return slot;
  }
}
