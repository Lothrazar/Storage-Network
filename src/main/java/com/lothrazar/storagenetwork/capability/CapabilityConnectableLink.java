package com.lothrazar.storagenetwork.capability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.api.EnumStorageDirection;
import com.lothrazar.storagenetwork.api.IConnectable;
import com.lothrazar.storagenetwork.api.IConnectableLink;
import com.lothrazar.storagenetwork.api.IItemStackMatcher;
import com.lothrazar.storagenetwork.capability.handler.FilterItemStackHandler;
import com.lothrazar.storagenetwork.registry.StorageNetworkCapabilities;
import com.lothrazar.storagenetwork.util.Batch;
import com.lothrazar.storagenetwork.util.StackProvider;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class CapabilityConnectableLink implements IConnectableLink, INBTSerializable<CompoundTag> {

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

  public CapabilityConnectableLink(BlockEntity tile) {
    connectable = tile.getCapability(StorageNetworkCapabilities.CONNECTABLE_CAPABILITY, null).orElse(null);
    filters.setIsAllowlist(false);
  }

  @Override
  public DimPos getPos() {
    return connectable == null ? null : connectable.getPos();
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
    IItemHandler itemHandler = inventoryPos.getCapability(ForgeCapabilities.ITEM_HANDLER, inventoryFace.getOpposite());
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
      IItemHandler itemHandler = inventoryPos.getCapability(ForgeCapabilities.ITEM_HANDLER, inventoryFace.getOpposite());
      if (itemHandler == null) {
        return stack;
      }
      //      if (itemHandler instanceof ExchangeItemStackHandler) {
      //        StorageNetwork.log("cannot loop back a network insert into ExchangeItemStackHandler");
      //        return stack;
      //      }
      return ItemHandlerHelper.insertItemStacked(itemHandler, stack, simulate);
    }
    catch (Exception e) {
      StorageNetworkMod.LOGGER.error("Insert stack error from other block ", e);
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
    IItemHandler itemHandler = inventoryPos.getCapability(ForgeCapabilities.ITEM_HANDLER, inventoryFace.getOpposite());
    if (itemHandler == null) {
      return ItemStack.EMPTY;
    }
    //    if (itemHandler instanceof ExchangeItemStackHandler) {
    //      StorageNetwork.log("cannot loop back a network extract into ExchangeItemStackHandler");
    //      return ItemStack.EMPTY;
    //    }
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
    IItemHandler itemHandler = inventoryPos.getCapability(ForgeCapabilities.ITEM_HANDLER, inventoryFace.getOpposite());
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

  public void setInventoryFace(Direction inventoryFace) {
    this.inventoryFace = inventoryFace;
  }

  @Override
  public CompoundTag serializeNBT() {
    CompoundTag result = new CompoundTag();
    result.putInt("prio", priority);
    if (inventoryFace != null) {
      result.putString("inventoryFace", inventoryFace.toString());
    }
    result.putString("way", filterDirection.toString());
    CompoundTag operation = new CompoundTag();
    operation.put("stack", operationStack.serializeNBT());
    operation.putBoolean("mustBeSmaller", operationMustBeSmaller);
    operation.putInt("limit", operationLimit);
    result.put("operation", operation);
    CompoundTag filters = this.filters.serializeNBT();
    result.put("filters", filters);
    return result;
  }

  @Override
  public void deserializeNBT(CompoundTag nbt) {
    priority = nbt.getInt("prio");
    CompoundTag filters = nbt.getCompound("filters");
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
    CompoundTag operation = nbt.getCompound("operation");
    operationStack = ItemStack.EMPTY;
    if (operation != null) {
      operationLimit = operation.getInt("limit");
      operationMustBeSmaller = operation.getBoolean("mustBeSmaller");
      if (operation.contains("stack")) {
        operationStack = ItemStack.of(operation.getCompound("stack"));
      }
    }
  }

  @Override
  public ItemStack extractFromSlot(int slot, int amount, boolean simulate) {
    DimPos inventoryPos = connectable.getPos().offset(inventoryFace);
    // Test whether the connected block has the IItemHandler capability
    IItemHandler itemHandler = inventoryPos.getCapability(ForgeCapabilities.ITEM_HANDLER,
        inventoryFace.getOpposite());
    if (itemHandler == null) {
      return ItemStack.EMPTY;
    }
    return itemHandler.extractItem(slot, amount, simulate);
  }

  @Override
  public void addToStackProviderBatch(Batch<StackProvider> availableItems) {
    // If this storage is configured to only export from the network, do not
    // extract from the storage, but abort immediately.
    if (filterDirection == EnumStorageDirection.IN) {
      return;
    }
    if (inventoryFace == null) {
      return;
    }
    DimPos connectablePos = connectable.getPos();
    if (connectablePos == null) {
      return;
    }
    DimPos inventoryPos = connectablePos.offset(inventoryFace);
    // Test whether the connected block has the IItemHandler capability
    IItemHandler itemHandler = inventoryPos.getCapability(ForgeCapabilities.ITEM_HANDLER,
        inventoryFace.getOpposite());
    if (itemHandler == null) {
      return;
    }
    // if (itemHandler instanceof ExchangeItemStackHandler) {
    // StorageNetwork.log("cannot loop back a network extract into
    // ExchangeItemStackHandler");
    // return ItemStack.EMPTY;
    // }
    for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
      // force simulate: allow them to not let me see the stack, also dont extract
      // since it might steal/dupe
      ItemStack stack = itemHandler.extractItem(slot, 1, true);
      if (stack == null || stack.isEmpty()) {
        continue;
      }
      // Ignore stacks that are filtered
      if (filters.isStackFiltered(stack)) {
        continue;
      }
      StackProvider provider = new StackProvider(this, slot);
      availableItems.put(stack.getItem(), provider);
    }
  }
}
