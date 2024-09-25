package mrriegel.storagenetwork.capabilities;

import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.Nullable;
import mrriegel.storagenetwork.api.capability.IConnectable;
import mrriegel.storagenetwork.api.capability.IConnectableItemAutoIO;
import mrriegel.storagenetwork.api.data.DimPos;
import mrriegel.storagenetwork.api.data.EnumStorageDirection;
import mrriegel.storagenetwork.api.data.EnumUpgradeType;
import mrriegel.storagenetwork.api.data.IItemStackMatcher;
import mrriegel.storagenetwork.api.network.INetworkMaster;
import mrriegel.storagenetwork.data.ItemStackMatcher;
import mrriegel.storagenetwork.util.inventory.FilterItemStackHandler;
import mrriegel.storagenetwork.util.inventory.UpgradesItemStackHandler;
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

public class CapabilityConnectableAutoIO implements INBTSerializable<NBTTagCompound>, IConnectableItemAutoIO {

  public IConnectable connectable;
  public EnumStorageDirection direction;
  public UpgradesItemStackHandler upgrades = new UpgradesItemStackHandler();
  public FilterItemStackHandler filters = new FilterItemStackHandler();
  public ItemStack operationStack = ItemStack.EMPTY;
  public int operationLimit = 0;
  public boolean operationMustBeSmaller = true;
  public int priority = 0;
  protected EnumFacing inventoryFace;
  public static final int IO_DEFAULT_SPEED = 30;
  public static final int DEFAULT_ITEMS_PER = 4;

  public CapabilityConnectableAutoIO(EnumStorageDirection direction) {
    this.connectable = new CapabilityConnectable();
    this.direction = direction;
  }

  public CapabilityConnectableAutoIO(TileEntity tile, EnumStorageDirection direction) {
    this.connectable = tile.getCapability(StorageNetworkCapabilities.CONNECTABLE_CAPABILITY, null);
    this.direction = direction;
    // Set some defaults
    if (direction == EnumStorageDirection.OUT) {
      filters.setIsWhitelist(true);
    }
  }

  public void setInventoryFace(EnumFacing inventoryFace) {
    this.inventoryFace = inventoryFace;
  }

  @Override
  public NBTTagCompound serializeNBT() {
    NBTTagCompound result = new NBTTagCompound();
    NBTTagCompound upgrades = this.upgrades.serializeNBT();
    result.setTag("upgrades", upgrades);
    NBTTagCompound filters = this.filters.serializeNBT();
    result.setTag("filters", filters);
    NBTTagCompound operation = new NBTTagCompound();
    operation.setTag("stack", operationStack.serializeNBT());
    operation.setBoolean("mustBeSmaller", operationMustBeSmaller);
    operation.setInteger("limit", operationLimit);
    result.setTag("operation", operation);
    result.setInteger("prio", priority);
    if (inventoryFace != null) {
      result.setString("inventoryFace", inventoryFace.toString());
    }
    return result;
  }

  @Override
  public void deserializeNBT(NBTTagCompound nbt) {
    NBTTagCompound upgrades = nbt.getCompoundTag("upgrades");
    this.upgrades.deserializeNBT(upgrades);
    NBTTagCompound filters = nbt.getCompoundTag("filters");
    this.filters.deserializeNBT(filters);
    NBTTagCompound operation = nbt.getCompoundTag("operation");
    this.operationLimit = operation.getInteger("limit");
    this.operationMustBeSmaller = operation.getBoolean("mustBeSmaller");
    if (operation.hasKey("stack", Constants.NBT.TAG_COMPOUND)) {
      this.operationStack = new ItemStack(operation.getCompoundTag("stack"));
    }
    else {
      this.operationStack = ItemStack.EMPTY;
    }
    this.priority = nbt.getInteger("prio");
    if (nbt.hasKey("inventoryFace")) {
      this.inventoryFace = EnumFacing.byName(nbt.getString("inventoryFace"));
    }
  }

  @Override
  public EnumStorageDirection ioDirection() {
    return direction;
  }

  @Override
  public int getPriority() {
    return priority;
  }

  @Override
  public int getTransferRate() {
    if (upgrades.getUpgradesOfType(EnumUpgradeType.SINGLE) > 0) {
      return 1; //override others
    }
    return upgrades.getUpgradesOfType(EnumUpgradeType.STACK) > 0 ? 64 : DEFAULT_ITEMS_PER;
  }

  @Override
  public ItemStack insertStack(ItemStack stack, boolean simulate) {
    // If this storage is configured to only import into the network, do not
    // insert into the storage, but abort immediately.
    if (direction == EnumStorageDirection.IN) {
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
  public FilterItemStackHandler getFilters() {
    return filters;
  }

  @Override
  public IItemHandler getItemHandler() {
    if (inventoryFace == null || direction == EnumStorageDirection.OUT) {
      return null;
    }
    DimPos inventoryPos = connectable.getPos().offset(inventoryFace);
    return inventoryPos.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, inventoryFace.getOpposite());
  }

  @Deprecated
  @Override
  public ItemStack extractNextStack(int size, boolean simulate) {
    return ItemStack.EMPTY;
  }

  private boolean doesPassOperationFilterLimit(INetworkMaster master) {
    if (upgrades.getUpgradesOfType(EnumUpgradeType.OPERATION) < 1) {
      return true;
    }
    if (operationStack == null || operationStack.isEmpty()) {
      return true;
    }
    // TODO: Investigate whether the operation limiter should consider the filter toggles
    ItemStack availableStack = master.getAmount(new ItemStackMatcher(operationStack, filters.meta, filters.ores, filters.nbt));
    if (operationMustBeSmaller) {
      return operationLimit >= availableStack.getCount();
    }
    else {
      return operationLimit < availableStack.getCount();
    }
  }

  @Override
  public boolean runNow(DimPos connectablePos, INetworkMaster master) {
    int speedRatio = upgrades.getSpeedRatio();
    //  
    boolean cooldownOk = (connectablePos.getWorld().getTotalWorldTime() % speedRatio == 0);
    boolean operationLimitOk = doesPassOperationFilterLimit(master);
    return cooldownOk && operationLimitOk;
  }

  @Override
  public List<IItemStackMatcher> getAutoExportList() {
    return filters.getStackMatchers();
  }

  public static class Factory implements Callable<IConnectableItemAutoIO> {

    @Override
    public IConnectableItemAutoIO call() throws Exception {
      return new CapabilityConnectableAutoIO(EnumStorageDirection.IN);
    }
  }

  public static class Storage implements Capability.IStorage<IConnectableItemAutoIO> {

    @Nullable
    @Override
    public NBTBase writeNBT(Capability<IConnectableItemAutoIO> capability, IConnectableItemAutoIO rawInstance, EnumFacing side) {
      CapabilityConnectableAutoIO instance = (CapabilityConnectableAutoIO) rawInstance;
      return instance.serializeNBT();
    }

    @Override
    public void readNBT(Capability<IConnectableItemAutoIO> capability, IConnectableItemAutoIO rawInstance, EnumFacing side, NBTBase nbt) {
      CapabilityConnectableAutoIO instance = (CapabilityConnectableAutoIO) rawInstance;
      instance.deserializeNBT((NBTTagCompound) nbt);
    }
  }

  @Override
  public boolean isStockMode() {
    return upgrades.getUpgradesOfType(EnumUpgradeType.STOCK) > 0;
  }

  @Override
  public EnumFacing facingInventory() {
    return inventoryFace;
  }

  @Override
  public DimPos getPos() {
    return this.connectable.getPos();
  }
}
