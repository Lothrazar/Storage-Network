package com.lothrazar.storagenetwork.api.network;

import com.lothrazar.storagenetwork.api.capabilities.ItemStackMatcherDefault;
import net.minecraft.world.item.ItemStack;

public interface BlockEntityMainNetwork {

  NetworkModule getNetwork();

  /**
   * Request to extract from my network
   */
  ItemStack request(ItemStackMatcherDefault matcher, int count, boolean b);

  /**
   * insert into my network
   */
  int insertStack(ItemStack stack, boolean simulate);
}
