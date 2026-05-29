package com.lothrazar.storagenetwork.block.cable.processing;

import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.api.IConnectableItemProcessing;
import com.lothrazar.storagenetwork.api.capabilities.FilterItemStackHandler;
import com.lothrazar.storagenetwork.block.TileCableWithFacing;
import com.lothrazar.storagenetwork.capabilities.CapabilityConnectableProcessing;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

public class TileCableProcess extends TileCableWithFacing implements MenuProvider {

  protected CapabilityConnectableProcessing itemStorage;
  private ProcessRequestModel processModel = new ProcessRequestModel();

  public TileCableProcess(BlockPos pos, BlockState state) {
    super(SsnRegistry.Tiles.PROCESS_KABEL.get(), pos, state);
    this.itemStorage = new CapabilityConnectableProcessing(this);
  }

  public IConnectableItemProcessing getItemStorage() {
    return itemStorage;
  }

  public CapabilityConnectableProcessing getCap() {
    return itemStorage;
  }

  public ProcessRequestModel getProcessModel() {
    return processModel;
  }

  @Override
  public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
    return new ContainerCableProcess(i, level, worldPosition, playerInventory, playerEntity);
  }

  @Override
  public Component getDisplayName() {
    return Component.translatable("block.storagenetwork.process_kabel");
  }

  @Override
  protected void loadAdditional(CompoundTag compound, HolderLookup.Provider registries) {
    super.loadAdditional(compound, registries);
    this.itemStorage.deserializeNBT(registries, compound.getCompound("capability"));
    this.processModel.readFromNBT(compound);
  }

  @Override
  protected void saveAdditional(CompoundTag compound, HolderLookup.Provider registries) {
    super.saveAdditional(compound, registries);
    compound.put("capability", itemStorage.serializeNBT(registries));
    this.processModel.writeToNBT(compound);
  }

  @Override
  public void setDirection(Direction direction) {
    super.setDirection(direction);
    this.itemStorage.setInventoryFace(direction);
  }

  /**
   * Snapshot the recipe by reading the adjacent block's input/output faces.
   * Clears existing filters first.
   */
  public void importFilters() {
    BlockPos facing = getFacingPosition();
    if (facing == null || level == null) {
      return;
    }
    DimPos targetPos = new DimPos(level, facing);
    IItemHandler input = targetPos.getItemHandler(processModel.getInputFace());
    IItemHandler output = targetPos.getItemHandler(processModel.getOutputFace());
    if (input == null && output == null) {
      return;
    }
    clearFilters(itemStorage.getFilters());
    clearFilters(itemStorage.getFiltersOut());
    if (input != null) {
      int target = 0;
      for (int i = 0; i < input.getSlots() && target < itemStorage.getFilters().getSlots(); i++) {
        ItemStack s = input.getStackInSlot(i);
        if (!s.isEmpty()) {
          ItemStack ghost = s.copy();
          ghost.setCount(1);
          itemStorage.getFilters().setStackInSlot(target++, ghost);
        }
      }
    }
    if (output != null) {
      int target = 0;
      for (int i = 0; i < output.getSlots() && target < itemStorage.getFiltersOut().getSlots(); i++) {
        ItemStack s = output.getStackInSlot(i);
        if (!s.isEmpty()) {
          ItemStack ghost = s.copy();
          ghost.setCount(1);
          itemStorage.getFiltersOut().setStackInSlot(target++, ghost);
        }
      }
    }
    this.setChanged();
  }

  private static void clearFilters(FilterItemStackHandler handler) {
    for (int i = 0; i < handler.getSlots(); i++) {
      handler.setStackInSlot(i, ItemStack.EMPTY);
    }
  }

  public static void clientTick(Level level, BlockPos blockPos, BlockState blockState, TileCableProcess tile) {}

  public static <E extends BlockEntity> void serverTick(Level level, BlockPos blockPos, BlockState blockState, TileCableProcess tile) {
    tile.refreshInventoryDirection();
  }
}
