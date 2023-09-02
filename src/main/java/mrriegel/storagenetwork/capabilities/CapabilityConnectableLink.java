package mrriegel.storagenetwork.capabilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.Nullable;
import mrriegel.storagenetwork.api.capability.IConnectable;
import mrriegel.storagenetwork.api.capability.IConnectableLink;
import mrriegel.storagenetwork.api.data.DimPos;
import mrriegel.storagenetwork.api.data.EnumStorageDirection;
import mrriegel.storagenetwork.api.data.IItemStackMatcher;
import mrriegel.storagenetwork.util.inventory.FilterItemStackHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

// TODO: We should add support for CommonCapabilities SlotlessItemHandler for efficiency reasons and compatibility with colossal chests, integrated dynamics etc
public class CapabilityConnectableLink implements IConnectableLink, INBTSerializable<NBTTagCompound> {

  public IConnectable connectable;
  private boolean operationMustBeSmaller = true;
  private ItemStack operationStack = ItemStack.EMPTY;
  private int operationLimit = 0;
  public FilterItemStackHandler filters = new FilterItemStackHandler();
  public EnumStorageDirection filterDirection = EnumStorageDirection.BOTH;
  protected EnumFacing inventoryFace;
  public int priority;

  public CapabilityConnectableLink() {
    this.connectable = new CapabilityConnectable();
    this.filters.setIsWhitelist(false);
  }

  public CapabilityConnectableLink(TileEntity tile) {
    this.connectable = tile.getCapability(StorageNetworkCapabilities.CONNECTABLE_CAPABILITY, null);
    this.filters.setIsWhitelist(false);
  }

  @Override
  public int getPriority() {
    return priority;
  }

  @Override
  public List<ItemStack> getStoredStacks() {
    if (inventoryFace == null) {
      return Collections.emptyList();
    }
    DimPos inventoryPos = connectable.getPos().offset(inventoryFace);
    // Test whether the connected block has the IItemHandler capability
    IItemHandler itemHandler = inventoryPos.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, inventoryFace.getOpposite());
    if (itemHandler == null) {
      return Collections.emptyList();
    }
    // If it does, iterate its stacks, filter them and add them to the result list
    List<ItemStack> result = new ArrayList<>();
    for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
      ItemStack stack = itemHandler.getStackInSlot(slot);
      if (stack == null || stack.isEmpty()) {
        continue;
      }
      if (this.filters.isStackFiltered(stack)) {
        continue;
      }
      result.add(stack.copy());
    }
    return result;
  }

  public void importFilterStacks() {
    if (inventoryFace == null) {
      return;
    }
    DimPos inventoryPos = connectable.getPos().offset(inventoryFace);
    // Test whether the connected block has the IItemHandler capability
    IItemHandler itemHandler = inventoryPos.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, inventoryFace.getOpposite());
    if (itemHandler == null) {
      return;
    }
    // clear the filter
    filters.clear();
    // If it does, iterate its stacks and add them to the result list
    int targetSlot = 0;
    for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
      ItemStack stack = itemHandler.getStackInSlot(slot);
      if (stack == null || stack.isEmpty()) {
        continue;
      }
      if (filters.exactStackAlreadyInList(stack)) {
        continue;
      }
      filters.setStackInSlot(targetSlot, stack.copy());
      targetSlot++;
      if (targetSlot >= FilterItemStackHandler.FILTER_SIZE) {
        return;
      }
    }
  }

  @Override
  public ItemStack insertStack(ItemStack stack, boolean simulate) {
    // If this storage is configured to only import into the network, do not
    // insert into the storage, but abort immediately.
    if (filterDirection == EnumStorageDirection.IN) {
      return stack;
    }
    if (filters.isStackFiltered(stack)) {
      return stack;
    }
    if (inventoryFace == null) {
      return stack;
    }
    DimPos inventoryPos = connectable.getPos().offset(inventoryFace);
    // Test whether the connected block has the IItemHandler capability
    IItemHandler itemHandler = inventoryPos.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, inventoryFace.getOpposite());
    if (itemHandler == null) {
      return stack;
    }
    return ItemHandlerHelper.insertItemStacked(itemHandler, stack, simulate);
  }

  @Override
  public ItemStack extractStack(IItemStackMatcher matcher, int size, boolean simulate) {
    // If nothing is actually being requested, abort immediately
    if (size <= 0) {
      return ItemStack.EMPTY;
    }
    // If this storage is configured to only export from the network, do not
    // extract from the storage, but abort immediately.
    if (filterDirection == EnumStorageDirection.IN) {
      return ItemStack.EMPTY;
    }
    if (inventoryFace == null) {
      return ItemStack.EMPTY;
    }
    DimPos inventoryPos = connectable.getPos().offset(inventoryFace);
    // Test whether the connected block has the IItemHandler capability
    IItemHandler itemHandler = inventoryPos.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, inventoryFace.getOpposite());
    if (itemHandler == null) {
      return ItemStack.EMPTY;
    }
    ItemStack firstMatchedStack = ItemStack.EMPTY;
    int remaining = size;
    for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
      // first check if we should bother trying to extract the item
      ItemStack check = itemHandler.getStackInSlot(slot);
      if (check == null || check.isEmpty() || filters.isStackFiltered(check) || !matcher.match(check)) {
        continue;
      }
      // now try for real (or for simulate)
      ItemStack extracted = itemHandler.extractItem(slot, remaining, simulate);
      if (extracted == null || extracted.isEmpty() || filters.isStackFiltered(extracted) || !matcher.match(extracted)) {
        continue;
      }
      // If its not even the item type we're looking for -> continue
      if (firstMatchedStack.isEmpty()) {
        firstMatchedStack = extracted;
      }
      else {
        if (!ItemHandlerHelper.canItemStacksStack(firstMatchedStack, extracted)) {
          continue;
        }
      }
      remaining -= extracted.getCount();
      if (remaining <= 0) {
        break;
      }
    }
    int extractCount = size - remaining;
    if (!firstMatchedStack.isEmpty() && extractCount > 0) {
      firstMatchedStack.setCount(extractCount);
    }
    return firstMatchedStack;
  }

  @Override
  public int getEmptySlots() {
    // If this storage is configured to only import into the network, do not
    // insert into the storage, but abort immediately.
    if (filterDirection == EnumStorageDirection.IN) {
      return 0;
    }
    if (inventoryFace == null) {
      return 0;
    }
    DimPos inventoryPos = connectable.getPos().offset(inventoryFace);
    // Test whether the connected block has the IItemHandler capability
    IItemHandler itemHandler = inventoryPos.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, inventoryFace.getOpposite());
    if (itemHandler == null) {
      return 0;
    }
    int emptySlots = 0;
    for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
      ItemStack stack = itemHandler.getStackInSlot(slot);
      if (stack != null && !stack.isEmpty()) {
        continue;
      }
      emptySlots++;
    }
    return emptySlots;
  }

  @Override
  public EnumStorageDirection getSupportedTransferDirection() {
    return filterDirection;
  }

  public void setInventoryFace(EnumFacing inventoryFace) {
    this.inventoryFace = inventoryFace;
  }

  @Override
  public NBTTagCompound serializeNBT() {
    NBTTagCompound result = new NBTTagCompound();
    NBTTagCompound filters = this.filters.serializeNBT();
    result.setTag("filters", filters);
    result.setInteger("prio", priority);
    if (inventoryFace != null) {
      result.setString("inventoryFace", inventoryFace.toString());
    }
    result.setString("way", filterDirection.toString());
    NBTTagCompound operation = new NBTTagCompound();
    operation.setTag("stack", operationStack.serializeNBT());
    operation.setBoolean("mustBeSmaller", operationMustBeSmaller);
    operation.setInteger("limit", operationLimit);
    result.setTag("operation", operation);
    return result;
  }

  @Override
  public void deserializeNBT(NBTTagCompound nbt) {
    NBTTagCompound filters = nbt.getCompoundTag("filters");
    this.filters.deserializeNBT(filters);
    this.priority = nbt.getInteger("prio");
    if (nbt.hasKey("inventoryFace")) {
      this.inventoryFace = EnumFacing.byName(nbt.getString("inventoryFace"));
    }
    try {
      this.filterDirection = EnumStorageDirection.valueOf(nbt.getString("way"));
    }
    catch (Exception e) {
      this.filterDirection = EnumStorageDirection.BOTH;
    }
    NBTTagCompound operation = nbt.getCompoundTag("operation");
    this.operationLimit = operation.getInteger("limit");
    this.operationMustBeSmaller = operation.getBoolean("mustBeSmaller");
    if (operation.hasKey("stack", Constants.NBT.TAG_COMPOUND)) {
      this.operationStack = new ItemStack(operation.getCompoundTag("stack"));
    }
    else {
      this.operationStack = ItemStack.EMPTY;
    }
  }

  public static class Factory implements Callable<IConnectableLink> {

    @Override
    public IConnectableLink call() throws Exception {
      return new CapabilityConnectableLink();
    }
  }

  public static class Storage implements Capability.IStorage<IConnectableLink> {

    @Nullable
    @Override
    public NBTBase writeNBT(Capability<IConnectableLink> capability, IConnectableLink rawInstance, EnumFacing side) {
      CapabilityConnectableLink instance = (CapabilityConnectableLink) rawInstance;
      return instance.serializeNBT();
    }

    @Override
    public void readNBT(Capability<IConnectableLink> capability, IConnectableLink rawInstance, EnumFacing side, NBTBase nbt) {
      CapabilityConnectableLink instance = (CapabilityConnectableLink) rawInstance;
      instance.deserializeNBT((NBTTagCompound) nbt);
    }
  }
}
