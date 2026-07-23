package com.lothrazar.storagenetwork.block.cable.processing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.api.network.BlockEntityMainNetwork;
import com.lothrazar.storagenetwork.api.network.ConnectableNode;
import com.lothrazar.storagenetwork.api.capabilities.CapabilityProcessing;
import com.lothrazar.storagenetwork.api.network.ConnectableNodeDefault;
import com.lothrazar.storagenetwork.block.cable.processing.ProcessRequestModel.ProcessStatus;
import com.lothrazar.storagenetwork.api.capabilities.FilterItemStackHandler;
import com.lothrazar.storagenetwork.api.capabilities.ItemStackMatcherDefault;
import com.lothrazar.storagenetwork.api.capabilities.UpgradesItemStackHandler;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public class CapabilityProcessingDefault implements ValueIOSerializable, CapabilityProcessing {

  public static class Factory implements Callable<CapabilityProcessing> {

    @Override
    public CapabilityProcessing call() throws Exception {
      return new CapabilityProcessingDefault();
    }
  }

  private Direction inventoryFace;
  public final ConnectableNode connectable;
  private final TileCableProcess tile;
  public final UpgradesItemStackHandler upgrades = new UpgradesItemStackHandler();
  private final FilterItemStackHandler filters = new FilterItemStackHandler(9);
  private final FilterItemStackHandler filtersOut = new FilterItemStackHandler(9);
  private int priority;

  CapabilityProcessingDefault() {
    connectable = new ConnectableNodeDefault();
    tile = null;
  }

  public CapabilityProcessingDefault(TileCableProcess tile) {
    this.tile = tile;
    this.connectable = tile != null ? tile.getConnectableNode() : null;
  }

  public void setInventoryFace(Direction inventoryFace) {
    this.inventoryFace = inventoryFace;
  }

  public FilterItemStackHandler getFilters() {
    return filters;
  }

  public FilterItemStackHandler getFiltersOut() {
    return filtersOut;
  }

  public boolean needsRedstone() {
    return connectable != null && connectable.needsRedstone();
  }

  public void toggleNeedsRedstone() {
    if (connectable != null) {
      connectable.toggleNeedsRedstone();
    }
  }

  @Override
  public void serialize(ValueOutput output) {
    output.putInt("prio", priority);
    this.upgrades.serialize(output.child("upgrades"));
    this.filters.serialize(output.child("filtersIn"));
    this.filtersOut.serialize(output.child("filtersOut"));
    if (inventoryFace != null) {
      output.putString("inventoryFace", inventoryFace.toString());
    }
  }

  @Override
  public void deserialize(ValueInput input) {
    priority = input.getIntOr("prio", 0);
    this.upgrades.deserialize(input.childOrEmpty("upgrades"));
    this.filters.deserialize(input.childOrEmpty("filtersIn"));
    this.filtersOut.deserialize(input.childOrEmpty("filtersOut"));
    String faceName = input.getStringOr("inventoryFace", null);
    if (faceName != null) {
      inventoryFace = Direction.byName(faceName);
    }
  }

  @Override
  public int getPriority() {
    return priority;
  }

  @Override
  public void setPriority(int value) {
    this.priority = value;
  }

//  @Override
//  public Direction facingInventory() {
//    return this.inventoryFace;
//  }

  private List<ItemStack> nonEmpty(FilterItemStackHandler handler) {
    List<ItemStack> out = new ArrayList<>();
    for (int i = 0; i < handler.getSlots(); i++) {
      ItemStack s = handler.getStackInSlot(i);
      if (!s.isEmpty()) {
        out.add(s.copy());
      }
    }
    return out;
  }

  @Override
  public void execute(BlockEntityMainNetwork main) {
    if (tile == null || connectable == null || connectable.getPos() == null) {
      return;
    }
    ProcessRequestModel model = tile.getProcessModel();
    if (!model.isAlwaysActive() && model.getCount() <= 0) {
      return;
    }
    if (inventoryFace == null) {
      // not connected to anything yet
      return;
    }
    List<ItemStack> ingredients = nonEmpty(filters);
    List<ItemStack> outputs = nonEmpty(filtersOut);
    if (ingredients.isEmpty() || outputs.isEmpty()) {
      return;
    }
    DimPos targetPos = connectable.getPos().offset(inventoryFace);
    if (targetPos == null) {
      return;
    }
    if (model.getStatus() == ProcessStatus.EXPORTING) {
      runExport(main, model, ingredients, targetPos);
    }
    else if (model.getStatus() == ProcessStatus.IMPORTING) {
      runImport(main, model, outputs, targetPos);
    }
    tile.setChanged();
  }

  private void runExport(BlockEntityMainNetwork main, ProcessRequestModel model, List<ItemStack> ingredients, DimPos targetPos) {
    IItemHandler targetIn = targetPos.getItemHandler(model.getInputFace());
    if (targetIn == null) {
      return;
    }
    int start = Math.min(model.getStackIndex(), ingredients.size());
    boolean exportedAll = true;
    for (int idx = start; idx < ingredients.size(); idx++) {
      ItemStack ingred = ingredients.get(idx);
      ItemStackMatcherDefault matcher = new ItemStackMatcherDefault(ingred.copy(), filters.tags, filters.nbt);
      // simulate pull from network
      ItemStack simulated = main.request(matcher, ingred.getCount(), true);
      if (simulated.getCount() < ingred.getCount()) {
        exportedAll = false;
        break;
      }
      // simulate insert into target
      ItemStack remain = ItemHandlerHelper.insertItemStacked(targetIn, simulated.copy(), true);
      if (!remain.isEmpty()) {
        exportedAll = false;
        break;
      }
      // commit: pull for real and insert for real
      ItemStack real = main.request(matcher, ingred.getCount(), false);
      ItemHandlerHelper.insertItemStacked(targetIn, real, false);
      model.increaseStackIndex();
    }
    if (exportedAll) {
      model.setStatus(ProcessStatus.IMPORTING);
    }
  }

  private void runImport(BlockEntityMainNetwork main, ProcessRequestModel model, List<ItemStack> outputs, DimPos targetPos) {
    IItemHandler targetOut = targetPos.getItemHandler(model.getOutputFace());
    if (targetOut == null) {
      return;
    }
    int start = Math.min(model.getStackIndex(), outputs.size());
    boolean importedAll = true;
    for (int idx = start; idx < outputs.size(); idx++) {
      ItemStack out = outputs.get(idx);
      // does the target have enough of this output ready? (count matches on item only)
      int available = countAvailable(targetOut, out);
      if (available < out.getCount()) {
        importedAll = false;
        break;
      }
      // does the network have capacity?
      int leftover = main.insertStack(out.copy(), true);
      if (leftover > 0) {
        importedAll = false;
        break;
      }
      ItemStack extracted = extractFromTarget(targetOut, out, out.getCount());
      if (extracted.isEmpty()) {
        importedAll = false;
        break;
      }
      main.insertStack(extracted, false);
      model.increaseStackIndex();
    }
    if (importedAll) {
      model.setStatus(ProcessStatus.EXPORTING);
      if (!model.isAlwaysActive()) {
        model.reduceCount();
      }
    }
  }

  private static int countAvailable(IItemHandler inv, ItemStack target) {
    int found = 0;
    for (int slot = 0; slot < inv.getSlots(); slot++) {
      ItemStack s = inv.getStackInSlot(slot);
      if (!s.isEmpty() && s.getItem() == target.getItem()) {
        found += s.getCount();
      }
    }
    return found;
  }

  /**
   * Robust extract: walks every slot, accumulates what comes out of extractItem().
   * Does NOT rely on the post-extract value of getStackInSlot - vanilla containers
   * (e.g. furnace) mutate the stack reference in-place, so reading the slot after
   * the extract is unreliable.
   */
  private static ItemStack extractFromTarget(IItemHandler inv, ItemStack target, int amount) {
    ItemStack collected = ItemStack.EMPTY;
    int remaining = amount;
    for (int slot = 0; slot < inv.getSlots() && remaining > 0; slot++) {
      ItemStack s = inv.getStackInSlot(slot);
      if (s.isEmpty() || s.getItem() != target.getItem()) {
        continue;
      }
      ItemStack pulled = inv.extractItem(slot, remaining, false);
      if (pulled.isEmpty()) {
        continue;
      }
      if (collected.isEmpty()) {
        collected = pulled.copy();
      }
      else {
        collected.grow(pulled.getCount());
      }
      remaining -= pulled.getCount();
    }
    return collected;
  }
}
