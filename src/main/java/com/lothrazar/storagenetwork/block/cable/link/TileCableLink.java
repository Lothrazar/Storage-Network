package com.lothrazar.storagenetwork.block.cable.link;

import com.lothrazar.storagenetwork.api.IConnectableLink;
import com.lothrazar.storagenetwork.block.TileCableWithFacing;
import com.lothrazar.storagenetwork.capabilities.CapabilityConnectableLink;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TileCableLink extends TileCableWithFacing {

  protected CapabilityConnectableLink itemStorage;

  public TileCableLink(BlockPos pos, BlockState state) {
    super(SsnRegistry.Tiles.STORAGE_KABEL.get(), pos, state);
    this.itemStorage = new CapabilityConnectableLink(this);
  }

  public IConnectableLink getItemStorage() {
    return itemStorage;
  }

  @Override
  protected void loadAdditional(CompoundTag compound, HolderLookup.Provider registries) {
    super.loadAdditional(compound, registries);
    this.itemStorage.deserializeNBT(registries, compound.getCompound("capability"));
  }

  @Override
  protected void saveAdditional(CompoundTag compound, HolderLookup.Provider registries) {
    super.saveAdditional(compound, registries);
    compound.put("capability", itemStorage.serializeNBT(registries));
  }

  @Override
  public void setDirection(Direction direction) {
    super.setDirection(direction);
    this.itemStorage.setInventoryFace(direction);
  }

  public static void clientTick(Level level, BlockPos blockPos, BlockState blockState, TileCableLink tile) {}

  public static <E extends BlockEntity> void serverTick(Level level, BlockPos blockPos, BlockState blockState, TileCableLink tile) {
    tile.refreshInventoryDirection();
  }
}
