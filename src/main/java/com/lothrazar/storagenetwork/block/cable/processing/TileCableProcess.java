package com.lothrazar.storagenetwork.block.cable.processing;

import com.lothrazar.storagenetwork.api.IConnectableItemProcessing;
import com.lothrazar.storagenetwork.block.TileCableWithFacing;
import com.lothrazar.storagenetwork.capability.CapabilityConnectableProcessing;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TileCableProcess extends TileCableWithFacing {

  protected CapabilityConnectableProcessing itemStorage;
  private ProcessRequestModel processModel = new ProcessRequestModel();

  public TileCableProcess(BlockPos pos, BlockState state) {
    super(SsnRegistry.Tiles.PROCESS_KABEL.get(), pos, state);
    this.itemStorage = new CapabilityConnectableProcessing(this);
  }

  public IConnectableItemProcessing getItemStorage() {
    return itemStorage;
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

  public static void clientTick(Level level, BlockPos blockPos, BlockState blockState, TileCableProcess tile) {}

  public static <E extends BlockEntity> void serverTick(Level level, BlockPos blockPos, BlockState blockState, TileCableProcess tile) {
    tile.refreshInventoryDirection();
  }
}
