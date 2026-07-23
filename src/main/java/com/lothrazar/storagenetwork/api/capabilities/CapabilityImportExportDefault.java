package com.lothrazar.storagenetwork.api.capabilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import com.lothrazar.library.data.OpCompareType;
import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.api.EnumStorageDirection;
import com.lothrazar.storagenetwork.api.UpgradeType;
import com.lothrazar.storagenetwork.api.network.BlockEntityConnectableNode;
import com.lothrazar.storagenetwork.api.network.ConnectableNode;
import com.lothrazar.storagenetwork.api.network.BlockEntityMainNetwork;
import com.lothrazar.storagenetwork.api.batch.Request;
import com.lothrazar.storagenetwork.api.batch.RequestBatch;
import com.lothrazar.storagenetwork.api.network.ConnectableNodeDefault;
import com.lothrazar.storagenetwork.api.util.UtilInventory;
import com.lothrazar.storagenetwork.registry.ConfigRegistry;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CapabilityImportExportDefault implements ValueIOSerializable, CapabilityImportExport {
  public static final Logger LOGGER = LogManager.getLogger();

  public static final int DEFAULT_ITEMS_PER = 4;

  public static class Factory implements Callable<CapabilityImportExport> {

    @Override
    public CapabilityImportExport call() throws Exception {
      return new CapabilityImportExportDefault(EnumStorageDirection.IN);
    }
  }

  public final ConnectableNode connectable;
  public EnumStorageDirection direction;
  public final UpgradesItemStackHandler upgrades = new UpgradesItemStackHandler();
  private final FilterItemStackHandler filters = new FilterItemStackHandler();
  private int priority = 0;
  private Direction inventoryFace;
  public ItemStack operationStack = ItemStack.EMPTY;
  public int operationLimit = 0;
  public int operationType = OpCompareType.LESS.ordinal();

  CapabilityImportExportDefault(EnumStorageDirection direction) {
    connectable = new ConnectableNodeDefault();
    this.direction = direction;
  }

  @Override
  public void toggleNeedsRedstone() {
    if (connectable != null) {
      connectable.toggleNeedsRedstone();
    }
  }

  @Override
  public boolean needsRedstone() {
    return connectable != null && connectable.needsRedstone();
  }

  @Override
  public void needsRedstone(boolean in) {
    if (connectable != null) {
      connectable.needsRedstone(in);
    }
  }

  public FilterItemStackHandler getFilter() {
    return filters;
  }

  //TODO: share with ConnectableLink  @Override
  public List<ItemStack> getStoredStacks(boolean isFiltered) {
    if (inventoryFace == null) {
      return Collections.emptyList();
    }
    DimPos inventoryPos = connectable.getPos().offset(inventoryFace);
    // Test whether the connected block has the IItemHandler capability
    IItemHandler itemHandler = inventoryPos.getItemHandler(inventoryFace.getOpposite());
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

  //TODO: share with ConnectableLink
  public void setPriority(int value) {
    this.priority = value;
  }

  public void setFilter(int value, ItemStack stack) {
    filters.setStackInSlot(value, stack);
    filters.getStacks().set(value, stack);
  }

  public CapabilityImportExportDefault(BlockEntity tile, EnumStorageDirection direction) {
    connectable = (tile instanceof BlockEntityConnectableNode tc)
        ? tc.getConnectableNode() : null;
    this.direction = direction;
    // Set some defaults
    if (direction == EnumStorageDirection.OUT) {
      filters.setIsAllowlist(true);
    }
    else {
      filters.setIsAllowlist(false);
    }
  }

  public void setInventoryFace(Direction inventoryFace) {
    this.inventoryFace = inventoryFace;
  }

  @Override
  public void serialize(ValueOutput output) {
    this.upgrades.serialize(output.child("upgrades"));
    this.filters.serialize(output.child("filters"));
    output.putInt("prio", priority);
    if (inventoryFace != null) {
      output.putString("inventoryFace", inventoryFace.toString());
    }
    ValueOutput operation = output.child("operation");
    if (!operationStack.isEmpty()) {
      operation.store("stack", ItemStack.CODEC, operationStack);
    }
    operation.putInt("operationType", operationType);
    operation.putInt("limit", operationLimit);
  }

  @Override
  public void deserialize(ValueInput input) {
    this.upgrades.deserialize(input.childOrEmpty("upgrades"));
    this.filters.deserialize(input.childOrEmpty("filters"));
    priority = input.getIntOr("prio", 0);
    String faceName = input.getStringOr("inventoryFace", null);
    if (faceName != null) {
      inventoryFace = Direction.byName(faceName);
    }
    if (input.getBooleanOr("needsRedstone", false)) {
      this.needsRedstone(true);
    }
    ValueInput operation = input.childOrEmpty("operation");
    this.operationLimit = operation.getIntOr("limit", 0);
    this.operationType = operation.getIntOr("operationType", OpCompareType.LESS.ordinal());
    this.operationStack = operation.read("stack", ItemStack.CODEC).orElse(ItemStack.EMPTY);
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
    IItemHandler itemHandler = inventoryPos.getItemHandler(inventoryFace.getOpposite());
    if (itemHandler == null) {
      return stack;
    }
    return ItemHandlerHelper.insertItemStacked(itemHandler, stack, simulate);
  }

  public List<ItemStack> getStacksForFilter() {
    if (inventoryFace == null) {
      return Collections.emptyList();
    }
    DimPos inventoryPos = connectable.getPos().offset(inventoryFace);
    // Test whether the connected block has the IItemHandler capability
    IItemHandler itemHandler = inventoryPos.getItemHandler(inventoryFace.getOpposite());
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
      if (filters.exactStackAlreadyInList(stack)) {
        continue;
      }
      result.add(stack.copy());
      // We can abort after we've found FILTER_SIZE stacks; we don't have more filter slots anyway
      if (result.size() >= FilterItemStackHandler.FILTER_SIZE) {
        return result;
      }
    }
    return result;
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
    return inventoryPos.getItemHandler(inventoryFace.getOpposite());
  }

  @Override
  public boolean isStockMode() {
    return getUpgrades().hasUpgradesOfType(UpgradeType.STOCK);
  }

  @Override
  public boolean isOperationMode() {
    return getUpgrades().hasUpgradesOfType(UpgradeType.OP);
  }

  @Override
  public int getTransferRate() {
    if (upgrades.hasUpgradesOfType(UpgradeType.SINGLE)) {
      return 1; //override both others
    }
    return upgrades.hasUpgradesOfType(UpgradeType.STACK) ? 64 : DEFAULT_ITEMS_PER;
  }

  private boolean doesPassOperationFilterLimit(BlockEntityMainNetwork master) {
    if (upgrades.getUpgradesOfType(UpgradeType.OP) < 1) {
      return true;
    }
    if (operationStack == null || operationStack.isEmpty()) {
      return true;
    }
    // TODO: Investigate whether the operation limiter should consider the filter toggles
    int countYourItemInNetwork = master.getNetwork().getAmount(new ItemStackMatcherDefault(operationStack, filters.tags, filters.nbt));
    switch (OpCompareType.get(operationType)) {
      case EQUAL:
        return countYourItemInNetwork == operationLimit;
      case GREATER:
        //true yes allowed to run if SLOT > textbox
        return countYourItemInNetwork > operationLimit;
      case LESS:
        //true yes allowed to run if SLOT < textbox
        return countYourItemInNetwork < operationLimit;
    }
    return false;
  }

  @Override
  public boolean canRunNow(DimPos connectablePos, BlockEntityMainNetwork main) {
    final int speedRatio = getSpeedRatio( );
    final  boolean cooldownOk = (connectablePos.getWorld().getGameTime() % speedRatio == 0);
    if (!cooldownOk) {
      return false;
    }
    //opt: dont check operation count if the cooldown is bad anyway
    boolean operationLimitOk = doesPassOperationFilterLimit(main);
    //    StorageNetwork.log("OP allowed to runNow = " + operationLimitOk);
    return operationLimitOk;
  }

  private int getSpeedRatio() {
    final int speedUpgrades = upgrades.getUpgradesOfType(UpgradeType.SPEED);
    final int slowUpgrades = upgrades.getUpgradesOfType(UpgradeType.SLOW);
    final int baseSpeed = ConfigRegistry.IO_DEFAULT_SPEED.get();
    int speedRatio = baseSpeed; // no upgrades
    if (speedUpgrades > 0) {
      //so 1 speed upgrade is run every 30/2=15t, two is 30/3 ticks etc
      speedRatio = baseSpeed / (speedUpgrades + 1);
    }
    else if (slowUpgrades > 0) {
      //meaning IF one or more speed upgrades are present, then all slowness upgrades are IGNORED
      //so 1 Slow upgrade is run every 30*2=60t, two is 30*3=90 ticks
      speedRatio = baseSpeed * (slowUpgrades + 1);
    }
    if (speedRatio < 1) {
      speedRatio = 1; // 0 wont happen but idk maybe
    }
    return speedRatio;
  }

  @Override
  public List<ItemStackMatcher> getAutoExportList() {
    return filters.getStackMatchers();
  }

  public UpgradesItemStackHandler getUpgrades() {
    return upgrades;
  }

  @Override
  public RequestBatch runExport(BlockEntityMainNetwork main) {
    if (this.ioDirection() != EnumStorageDirection.OUT) { // TODO: redundant?
      return null;
    }
    RequestBatch requestBatch = new RequestBatch();
    for (ItemStackMatcher matcher : this.getAutoExportList()) {
      if (matcher.getStack().isEmpty()) {
        continue;
      }
      Request request = new Request(this);
      // default amt to request. can be overriden by other upgrades
      // check operations upgrade for export
      boolean stockMode = this.isStockMode();
      if (stockMode) {
        // STOCK upgrade means
        try {
          DimPos inventoryPos = connectable.getPos().offset(inventoryFace);
          IItemHandler targetInventory = inventoryPos.getItemHandler(inventoryFace.getOpposite());
          // request with false to see how many even exist in there.
          int stillNeeds = UtilInventory.containsAtLeastHowManyNeeded(targetInventory, matcher.getStack(),
              matcher.getStack().getCount());
          if (stillNeeds == 0) {
            // they dont need any more, they have the stock they need
            continue;
          }
          request.setCount(Math.min(stillNeeds, request.getCount()));
        }
        catch (Throwable e) {
          LOGGER.error("Error thrown from a connected block" + e);
        }
      }
      if (matcher.getStack().isEmpty() || request.getCount() == 0) {
        // either the thing is empty or we are requesting none
        continue;
      }
      requestBatch.put(matcher.getStack().getItem(), request);
    }
    //
    return requestBatch;
  }

  @Override
  public void runImport(BlockEntityMainNetwork main) {
    if (this.ioDirection() != EnumStorageDirection.IN) { // TODO: redundant?
      return;
    }
    IItemHandler itemHandler = this.getItemHandler();
    if (itemHandler == null) {
      return;
    }
    for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
      if (itemHandler.getStackInSlot(slot).isEmpty()) {
        continue;
      }
      ItemStack stackCurrent = itemHandler.getStackInSlot(slot).copy();
      // Ignore stacks that are filtered
      if (this.getFilters() == null || !this.getFilters().isStackFiltered(stackCurrent)) {
        if (this.isStockMode()) {
          int filterSize = this.getFilters().getStackCount(stackCurrent);
          DimPos inventoryPos = connectable.getPos().offset(inventoryFace);
          IItemHandler targetInventory = inventoryPos.getItemHandler(inventoryFace.getOpposite());
          //request with false to see how many even exist in there.
          int chestHowMany = UtilInventory.countHowMany(targetInventory, stackCurrent);
          //so if chest=37 items of that kind
          //and the filter is say filterSize == 20
          //we SHOULD import 37
          //as we want the STOCK of the chest to not go less than the filter number , just down to it
          if (chestHowMany > filterSize) {
            int realSize = Math.min(chestHowMany - filterSize, 64);
            stackCurrent.setCount(realSize);
          }
          else {
            LOGGER.debug(" -> stock mode CANCEL: ITS NOT ENOUGH chestHowMany <= filter size ");
            continue;
          }
        }
        int extractSize = Math.min(this.getTransferRate(), stackCurrent.getCount());
        ItemStack stackToImport = itemHandler.extractItem(slot, extractSize, true); //simulate to grab a reference
        if (stackToImport.isEmpty()) {
          continue; //continue back to itemHandler
        }
        // Then try to insert the stack into this masters network and store the number of remaining items in the stack
        int countUnmoved = main.insertStack(stackToImport, true);
        // Calculate how many items in the stack actually got moved
        int countMoved = stackToImport.getCount() - countUnmoved;
        // Void upgrade: any time the upgrade is installed and the stack passed the filter check above
        // (either allow-list match or not in the deny-list). Installing the upgrade is the opt-in.
        boolean voidEnabled = upgrades.hasUpgradesOfType(UpgradeType.VOID);
        if (countMoved <= 0 && !voidEnabled) {
          continue; //continue back to itemHandler
        }
        // Void mode pulls the full extractSize so the surplus is destroyed; normal mode pulls only what fits.
        int amountToExtract = voidEnabled ? extractSize : countMoved;
        ItemStack actuallyExtracted = itemHandler.extractItem(slot, amountToExtract, false);
        if (!actuallyExtracted.isEmpty()) {
          // Insert what the network accepts; any remainder in actuallyExtracted is silently discarded
          // when the local goes out of scope (this is the void behavior).
          int leftover = main.insertStack(actuallyExtracted, false);
          if (voidEnabled && leftover > 0) {
             LOGGER.debug("Void upgrade destroyed {} x {} at {}",
                leftover, actuallyExtracted.getItem(), connectable.getPos());
          }
        }
        break; // break out of itemHandler loop, done processing this cable, so move to next
      } //end of checking on filter for this stack
    }
  }
}
