package com.lothrazar.storagenetwork.api.capabilities;

import java.util.List;

import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.api.EnumStorageDirection;
import com.lothrazar.storagenetwork.api.batch.Batch;
import com.lothrazar.storagenetwork.api.batch.StackProvider;
import com.lothrazar.storagenetwork.api.network.ConnectableNode;
import net.minecraft.world.item.ItemStack;

/**
 * All storage-networks parts that expose IConnectable can also expose this capability to make the part able to interact with nearby inventories.
 *
 * What's being done with the stacks is completely up to your implementation. This can be for example a cable that can connect to a nearby InventoryHandler. Or the cable could store the given
 * itemstacks in a WorldSavedData structure or simply forget about them (which is obviously a bad idea).
 *
 */
public interface CapabilityConnectable {

  /**
   * forwarded to the {@link ConnectableNode}
   * @return
   */
  DimPos getPos();

  /**
   * Use this method to return all stacks that are stored via this {@link CapabilityConnectable}. This should be a filtered list, i.e. if you offer some sort of item filter for your storage, apply the
   * filters here or players will see the item in their control panel.
   * 
   * isFiltered : if true , it does apply the filter. if not it returns all stacks ignoring filter
   *
   * @return
   */
  List<ItemStack> getStoredStacks(boolean isFiltered);

  /**
   * This is called on your capability every time the network tries to insert a stack into your storage.
   *
   * If your getSupportedTransferDirection is set to OUT, this should never get called, unless another malicious mod is doing it.
   *
   * @param stack
   *          The stack being inserted into your storage
   * @param simulate
   *          Whether or not this is just a simulation
   * @return The remainder of the stack if not all of it fit into your storage
   */
  ItemStack insertStack(ItemStack stack, boolean simulate);

  /**
   * This is called whenever a stack is requested from the network. If your storage can not supply it, return an empty itemstack. Otherwise return a matching stack. If its not a simulation actually
   * remove the stack from your storage!
   *
   * Extract at maximum the given size.
   *
   * If your getSupportedTransferDirection is set to IN, this should never get called, unless another malicious mod is doing it.
   *
   * @param matcher
   *          A description of an itemstack that would fulfill the request
   * @param size
   *          The maximum stack size you should extract from your storage
   * @param simulate
   *          Whether or not this is just a simulation
   * @return The stack that has been requested, if you have it
   */
  ItemStack extractStack(ItemStackMatcher matcher, final int size, boolean simulate);

  /**
   * Storages with a higher priority (== lower number) are processed first. You probably want to add a way to configure the priority of your storage.
   *
   * @return Return the priority here
   */
  int getPriority();

  /**
   * If you want to limit this IConnectableLink to only export or only import you can return the appropriate EnumStorageDirection here
   *
   * @return
   */
  EnumStorageDirection getSupportedTransferDirection();

  /**
   * We need to know for various reasons how many more stacks this inventory can hold, i.e. how many empty slots there are.
   *
   * @return
   */
  int getEmptySlots();

  /**
   * Count of slots in the attached inventory that currently hold a non-empty stack.
   * Used to compute the comparator output on the Storage Network Root.
   * Default 0 keeps third-party implementors compiling without contributing to the signal.
   */
  default int getFilledSlots() {
    return 0;
  }

  /**
   * Total slot count in the attached inventory (filled + empty). Used to compute the
   * comparator output on the Storage Network Root. Default 0 keeps third-party
   * implementors compiling without contributing to the signal.
   */
  default int getTotalSlots() {
    return 0;
  }

  void setPriority(int value);

  void setFilter(int value, ItemStack copy);

  public ItemStack extractFromSlot(int slot, int amount, boolean simulate);

  void addToStackProviderBatch(Batch<StackProvider> availableItems);

  /**
   * Position of the inventory this storage is reading from, i.e. the link cable's
   * facing neighbor. Null if not bound to a directional neighbor (e.g. cradle,
   * remote, or storage caps that own their own inventory). Used by the network to
   * detect and refuse duplicate link cables aimed at the same inventory.
   */
  default DimPos getTargetPos() {
    return null;
  }
}
