package com.lothrazar.storagenetwork.compat.rs;

import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.registry.CradleAdapterRegistry;

/**
 * Class-loaded only when Refined Storage is present (guarded at the call site in StorageNetworkMod#setup).
 * Direct RS imports in this file are safe because of that guard.
 */
public final class Rs2CradleBootstrap {

  private Rs2CradleBootstrap() {}

  public static void register() {
    CradleAdapterRegistry.register(new Rs2CradleAdapter());
    StorageNetworkMod.LOGGER.info("Storage Cradle: Refined Storage disk adapter registered.");
  }
}
