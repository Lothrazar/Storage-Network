package com.lothrazar.storagenetwork.compat.ae2;

import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.registry.CradleAdapterRegistry;

/**
 * Class-loaded only when AE2 is present (guarded at the call site in StorageNetworkMod#setup).
 * Direct AE2 imports in this file are safe because of that guard.
 */
public final class Ae2CradleBootstrap {

  private Ae2CradleBootstrap() {}

  public static void register() {
    CradleAdapterRegistry.register(new Ae2CradleAdapter());
    StorageNetworkMod.LOGGER.info("Storage Cradle: AE2 storage cell adapter registered.");
  }
}
