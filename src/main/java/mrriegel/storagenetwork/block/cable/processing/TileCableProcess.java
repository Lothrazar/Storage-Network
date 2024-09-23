package mrriegel.storagenetwork.block.cable.processing;

import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.block.cable.TileCableWithFacing;
import mrriegel.storagenetwork.block.master.TileMaster;
import mrriegel.storagenetwork.data.ItemStackMatcher;
import mrriegel.storagenetwork.util.UtilInventory;
import mrriegel.storagenetwork.util.inventory.ProcessingItemStackHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class TileCableProcess extends TileCableWithFacing {

  private ProcessRequestModel processModel = new ProcessRequestModel();
  public EnumFacing processingTop = EnumFacing.UP;
  public EnumFacing processingBottom = EnumFacing.DOWN;
  public ProcessingItemStackHandler filters = new ProcessingItemStackHandler();

  @Override
  public void readFromNBT(NBTTagCompound compound) {
    processingTop = EnumFacing.values()[compound.getInteger("processingTop")];
    processingBottom = EnumFacing.values()[compound.getInteger("processingBottom")];
    ProcessRequestModel pm = new ProcessRequestModel();
    pm.readFromNBT(compound);
    this.setProcessModel(pm);
    NBTTagCompound filters = compound.getCompoundTag("filters");
    this.filters.deserializeNBT(filters);
    super.readFromNBT(compound);
  }

  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    super.writeToNBT(compound);
    this.processModel.writeToNBT(compound);
    compound.setInteger("processingBottom", processingBottom.ordinal());
    compound.setInteger("processingTop", processingTop.ordinal());
    NBTTagCompound filters = this.filters.serializeNBT();
    compound.setTag("filters", filters);
    return compound;
  }

  public void run() {
    ProcessRequestModel processRequest = getRequest();
    if (processRequest == null) {
      return;
    }
    if (processRequest.isAlwaysActive() == false && processRequest.getCount() <= 0) {
      return; // no more left to do
    }
    if (getMaster() == null || getMaster().getTileEntity(TileMaster.class) == null) {
      return;
    }
    if (!hasDirection()) {
      return;
    }
    // now check item filter for input/output
    List<ItemStack> ingredients = getProcessIngredients();
    // well should this only be a single output?
    List<ItemStack> outputs = getProcessOutputs();
    if (ingredients.size() == 0 || outputs.size() == 0) {
      return;
    }
    TileMaster master = getMaster().getTileEntity(TileMaster.class);
    // EXAMPLE REQUEST:
    // automate a furnace:
    // ingredient is one cobblestone (network provides-exports this)
    // output is one smoothstone (network gets-imports this)
    if (processRequest.getStatus() == ProcessRequestModel.ProcessStatus.EXPORTING) {
      // insert ingredients one by one instead of all at once to be more effecient
      // with item requests
      // keep track of inserted ingredients with stackIndex and make sure to start at
      // the right one
      boolean exportedAll = true;
      for (ItemStack ingred : ingredients.subList(processRequest.getStackIndex(), ingredients.size())) {
        ItemStackMatcher matcher = new ItemStackMatcher(ingred.copy(), this.filters.meta, this.filters.ores,
            this.filters.nbt);
        ItemStack requestedFromNetwork = master.request(matcher, ingred.getCount(), true);
        int found = requestedFromNetwork.getCount();
        // StorageNetwork.log("ingr size " + ingred.getSize() + " found +" + found + "
        // of " + ingred.getStack().getDisplayName());
        IItemHandler inventoryLinked = UtilInventory.getItemHandler(world.getTileEntity(this.getFacingPosition()),
            this.getFacingTopRow());
        ItemStack remain = ItemHandlerHelper.insertItemStacked(inventoryLinked, requestedFromNetwork, true);
        if (remain.isEmpty() && found >= ingred.getCount()) {
          // then do it for real
          requestedFromNetwork = master.request(matcher, ingred.getCount(), false);
          remain = ItemHandlerHelper.insertItemStacked(inventoryLinked, requestedFromNetwork, false);
          if (!remain.isEmpty()) {
            StorageNetwork.log("ingr" + ingred.getDisplayName() + " had "
                + remain.getCount() + " left after insertion");
          }
          // done inserting item, increment it in the request
          processRequest.increaseStackIndex();
        }
        else {
          // couldn't insert item, wait to insert more or change mode until we succeed
          exportedAll = false;
          break;
        }
      } // end loop on ingredients
      if (exportedAll) {
        processRequest.setStatus(ProcessRequestModel.ProcessStatus.IMPORTING);
      }
    }
    else if (processRequest.getStatus() == ProcessRequestModel.ProcessStatus.IMPORTING) {
      // try to find/get from the blocks outputs into network
      // look for "output" items that can be from target
      // do one by one here as well because we can
      boolean importedAll = true;
      for (ItemStack out : outputs.subList(processRequest.getStackIndex(), outputs.size())) {
        // pull this many from target
        IItemHandler inventoryLinked = UtilInventory.getItemHandler(world.getTileEntity(this.getFacingPosition()),
            this.getFacingBottomRow());
        // make sure the target has all the items we want to import
        int targetStillNeeds = UtilInventory.containsAtLeastHowManyNeeded(inventoryLinked, out, out.getCount());
        if (targetStillNeeds > 0) {
          importedAll = false;
          break;
        }
        // make sure we are able to import all the items
        int countNotInserted = master.insertStack(out, true);
        if (countNotInserted > 0) {
          importedAll = false;
          break;
        }
        // StorageNetwork.log("IMPORTING: " + out.toString());
        // new extract item using capabilities
        ItemStack extracted = UtilInventory.extractItem(inventoryLinked, new ItemStackMatcher(out), out.getCount(), false);
        master.insertStack(extracted, false);
        // done importing item, increment it in the request
        processRequest.increaseStackIndex();
      }
      if (importedAll) {
        // change mode back to exporting
        // StorageNetwork.log("IMPORTING: TO STATUS EXPORTING ");
        processRequest.setStatus(ProcessRequestModel.ProcessStatus.EXPORTING);
        if (processRequest.isAlwaysActive() == false) {
          processRequest.reduceCount();
        }
      }
    }
    this.markDirty();
  }

  public List<ItemStack> getProcessIngredients() {
    return filters.getInputs().stream().map(stack -> stack.copy()).collect(Collectors.toList());
  }

  public List<ItemStack> getProcessOutputs() {
    return filters.getOutputs().stream().map(stack -> stack.copy()).collect(Collectors.toList());
  }

  @Nonnull
  public ItemStack getFirstRecipeOut() {
    if (filters.isOutputEmpty()) {
      return ItemStack.EMPTY;
    }
    return filters.getOutputs().get(0);
  }

  public EnumFacing getFacingBottomRow() {
    return this.processingBottom;
  }

  public EnumFacing getFacingTopRow() {
    return this.processingTop;
  }

  public ProcessRequestModel getProcessModel() {
    return processModel;
  }

  public void setProcessModel(ProcessRequestModel processModel) {
    this.processModel = processModel;
  }

  // TODO: also list of requests ordered . and nbt saved
  // where a process terminal lists some nodes and I "turn node on for 6 cycles"
  // and it keeps track, maybe stuck after 2.
  public ProcessRequestModel getRequest() {
    return getProcessModel();
  }

  public void setRequest(ProcessRequestModel request) {
    this.setProcessModel(request);
  }
}
