package com.lothrazar.storagenetwork.block.cable.input;

import com.lothrazar.storagenetwork.api.EnumStorageDirection;
import com.lothrazar.storagenetwork.api.IConnectableItemAutoIO;
import com.lothrazar.storagenetwork.block.TileCableWithFacing;
import com.lothrazar.storagenetwork.capability.CapabilityConnectableAutoIO;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TileCableIO extends TileCableWithFacing {

  protected CapabilityConnectableAutoIO ioStorage;

  public TileCableIO(BlockPos pos, BlockState state) {
    super(SsnRegistry.Tiles.IMPORT_KABEL.get(), pos, state);
    this.ioStorage = new CapabilityConnectableAutoIO(this, EnumStorageDirection.IN);
  }

  public IConnectableItemAutoIO getIoStorage() {
    return ioStorage;
  }

  @Override
  public void setDirection(Direction direction) {
    super.setDirection(direction);
    this.ioStorage.setInventoryFace(direction);
  }

  @Override
  protected void loadAdditional(CompoundTag compound, HolderLookup.Provider registries) {
    super.loadAdditional(compound, registries);
    this.ioStorage.deserializeNBT(registries, compound.getCompound("ioStorage"));
  }

  @Override
  protected void saveAdditional(CompoundTag compound, HolderLookup.Provider registries) {
    super.saveAdditional(compound, registries);
    compound.put("ioStorage", this.ioStorage.serializeNBT(registries));
  }

  public static void clientTick(Level level, BlockPos blockPos, BlockState blockState, TileCableIO tile) {}

  public static <E extends BlockEntity> void serverTick(Level level, BlockPos blockPos, BlockState blockState, TileCableIO tile) {
    tile.refreshInventoryDirection();
  }
}
