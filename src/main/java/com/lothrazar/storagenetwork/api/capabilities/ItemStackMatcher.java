package com.lothrazar.storagenetwork.api.capabilities;

import net.minecraft.world.item.ItemStack;

/**
 * Do not implement this interface yourself, but get instances from the {@link } you get passed on your Plugin initialization.
 *
 * You also should usually not need to call any of the given methods, since you are usually just passing instances of this to other api methods.
 */
public interface ItemStackMatcher {

  /**
   * Returns the stack stored in the matcher.
   *
   * @return
   */
  ItemStack getStack();

  /**
   * Matches the given rules against the given stack and returns whether it matches.
   *
   * @param stack
   * @return
   */
  boolean match(ItemStack stack);

  boolean match(ItemStackMatcher matcher);
}
