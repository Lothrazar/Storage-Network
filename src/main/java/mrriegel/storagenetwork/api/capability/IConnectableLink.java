package mrriegel.storagenetwork.api.capability;

import java.util.List;
import mrriegel.storagenetwork.api.data.EnumStorageDirection;
import mrriegel.storagenetwork.api.data.IItemStackMatcher;
import net.minecraft.item.ItemStack;

/**
 * All storage-networks parts that expose IConnectable can also expose this capability to make the part able to interact with nearby inventories.
 *
 * What's being done with the stacks is completely up to your implementation. This can be for example a cable that can connect to a nearby InventoryHandler. Or the cable could store the given
 * itemstacks in a WorldSavedData structure or simply forget about them (which is obviously a bad idea).
 *
 */
public interface IConnectableLink {

  /**
   * Use this method to return all stacks that are stored via this {@link IConnectableLink}. This should be a filtered list, i.e. if you offer some sort of item filter for your storage, apply the
   * filters here or players will see the item in their control panel.
   *
   * @return
   */
  List<ItemStack> getStoredStacks();

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
  ItemStack extractStack(IItemStackMatcher matcher, final int size, boolean simulate);

  /**
   * Storages with a higher priority are preferred to store items. You probably want to add a way to configure the priority of your storage.
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
}
