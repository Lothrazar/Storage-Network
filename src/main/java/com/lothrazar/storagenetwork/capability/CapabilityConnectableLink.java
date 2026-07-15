package com.lothrazar.storagenetwork.capability;

import com.lothrazar.storagenetwork.StorageNetwork;
import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.api.EnumStorageDirection;
import com.lothrazar.storagenetwork.api.IConnectable;
import com.lothrazar.storagenetwork.api.IConnectableLink;
import com.lothrazar.storagenetwork.api.IItemStackMatcher;
import com.lothrazar.storagenetwork.block.exchange.ExchangeItemStackHandler;
import com.lothrazar.storagenetwork.capability.handler.FilterItemStackHandler;
import com.lothrazar.storagenetwork.registry.StorageNetworkCapabilities;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

// TODO: We should add support for CommonCapabilities SlotlessItemHandler for efficiency reasons and compatibility with colossal chests, integrated dynamics etc
public class CapabilityConnectableLink implements IConnectableLink, INBTSerializable<CompoundNBT> {

  public final IConnectable connectable;
  private boolean operationMustBeSmaller = true;
  private ItemStack operationStack = ItemStack.EMPTY;
  private int operationLimit = 0;
  private FilterItemStackHandler filters = new FilterItemStackHandler();
  private EnumStorageDirection filterDirection = EnumStorageDirection.BOTH;
  private Direction inventoryFace;
  private int priority;

  CapabilityConnectableLink() {
    connectable = new CapabilityConnectable();
    filters.setIsAllowlist(false);
  }

  public CapabilityConnectableLink(TileEntity tile) {
    connectable = tile.getCapability(StorageNetworkCapabilities.CONNECTABLE_CAPABILITY, null).orElse(null);
    filters.setIsAllowlist(false);
  }

  public FilterItemStackHandler getFilter() {
    return filters;
  }

  @Override
  public void setFilter(int value, ItemStack stack) {
    filters.setStackInSlot(value, stack);
    filters.getStacks().set(value, stack);
  }

  @Override
  public int getPriority() {
    return priority;
  }

  @Override
  public List<ItemStack> getStoredStacks(boolean isFiltered) {
    if (inventoryFace == null || connectable.getPos() == null) {
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
      if (isFiltered && filters.isStackFiltered(stack)) {
        continue;
      }
      result.add(stack.copy());
    }
    return result;
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
    try {
      // Test whether the connected block has the IItemHandler capability
      IItemHandler itemHandler = inventoryPos.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, inventoryFace.getOpposite());
      if (itemHandler == null) {
        return stack;
      }
      return ItemHandlerHelper.insertItemStacked(itemHandler, stack, simulate);
    }
    catch (Exception e) {
      StorageNetwork.LOGGER.error("Insert stack error from other block ", e);
      return stack;
    }
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
    if (itemHandler instanceof ExchangeItemStackHandler) {
      return ItemStack.EMPTY;
    }
    ItemStack firstMatchedStack = ItemStack.EMPTY;
    int remaining = size;
    for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
      //force simulate: allow them to not let me see the stack, also dont extract since it might steal/dupe
      ItemStack stack = itemHandler.extractItem(slot, remaining, true);
      if (stack == null || stack.isEmpty()) {
        continue;
      }
      // Ignore stacks that are filtered
      if (filters.isStackFiltered(stack)) {
        continue;
      }
      // If its not even the item type we're looking for -> continue
      if (firstMatchedStack.isEmpty()) {
        if (!matcher.match(stack)) {
          continue;
        }
        firstMatchedStack = stack.copy();
      }
      else {
        if (!ItemHandlerHelper.canItemStacksStack(firstMatchedStack, stack)) {
          continue;
        }
      }
      int toExtract = Math.min(stack.getCount(), remaining);
      ItemStack extractedStack = itemHandler.extractItem(slot, toExtract, simulate);
      remaining -= extractedStack.getCount();
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
  public void setPriority(int value) {
    this.priority = value;
  }

  @Override
  public EnumStorageDirection getSupportedTransferDirection() {
    return filterDirection;
  }

  @Override
  public DimPos getTargetPos() {
    if (inventoryFace == null || connectable == null || connectable.getPos() == null) {
      return null;
    }
    return connectable.getPos().offset(inventoryFace);
  }

  public void setInventoryFace(Direction inventoryFace) {
    this.inventoryFace = inventoryFace;
  }

  @Override
  public CompoundNBT serializeNBT() {
    CompoundNBT result = new CompoundNBT();
    result.putInt("prio", priority);
    if (inventoryFace != null) {
      result.putString("inventoryFace", inventoryFace.toString());
    }
    result.putString("way", filterDirection.toString());
    CompoundNBT operation = new CompoundNBT();
    operation.put("stack", operationStack.serializeNBT());
    operation.putBoolean("mustBeSmaller", operationMustBeSmaller);
    operation.putInt("limit", operationLimit);
    result.put("operation", operation);
    CompoundNBT filters = this.filters.serializeNBT();
    result.put("filters", filters);
    return result;
  }

  @Override
  public void deserializeNBT(CompoundNBT nbt) {
    priority = nbt.getInt("prio");
    CompoundNBT filters = nbt.getCompound("filters");
    this.filters.deserializeNBT(filters);
    if (nbt.contains("inventoryFace")) {
      inventoryFace = Direction.byName(nbt.getString("inventoryFace"));
    }
    try {
      filterDirection = EnumStorageDirection.valueOf(nbt.getString("way"));
    }
    catch (Exception e) {
      filterDirection = EnumStorageDirection.BOTH;
    }
    CompoundNBT operation = nbt.getCompound("operation");
    operationStack = ItemStack.EMPTY;
    if (operation != null) {
      operationLimit = operation.getInt("limit");
      operationMustBeSmaller = operation.getBoolean("mustBeSmaller");
      if (operation.contains("stack", Constants.NBT.TAG_COMPOUND)) {
        operationStack = ItemStack.read(operation.getCompound("stack"));
      }
    }
  }

  public static class Factory implements Callable<IConnectableLink> {

    @Override
    public IConnectableLink call() throws Exception {
      return new CapabilityConnectableLink();
    }
  }

  public static class Storage implements Capability.IStorage<IConnectableLink> {

    @Override
    public INBT writeNBT(Capability<IConnectableLink> capability, IConnectableLink rawInstance, Direction side) {
      CapabilityConnectableLink instance = (CapabilityConnectableLink) rawInstance;
      return instance.serializeNBT();
    }

    @Override
    public void readNBT(Capability<IConnectableLink> capability, IConnectableLink rawInstance, Direction side, INBT nbt) {
      CapabilityConnectableLink instance = (CapabilityConnectableLink) rawInstance;
      instance.deserializeNBT((CompoundNBT) nbt);
    }
  }
}
