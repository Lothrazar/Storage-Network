package com.lothrazar.storagenetwork.block.cable.inputfilter;

import com.lothrazar.storagenetwork.api.EnumStorageDirection;
import com.lothrazar.storagenetwork.api.IConnectableItemAutoIO;
import com.lothrazar.storagenetwork.block.TileCableWithFacing;
import com.lothrazar.storagenetwork.capability.CapabilityConnectableAutoIO;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TileCableImportFilter extends TileCableWithFacing implements MenuProvider {

  protected CapabilityConnectableAutoIO ioStorage;

  public TileCableImportFilter(BlockPos pos, BlockState state) {
    super(SsnRegistry.Tiles.IMPORT_FILTER_KABEL.get(), pos, state);
    this.ioStorage = new CapabilityConnectableAutoIO(this, EnumStorageDirection.IN);
  }

  public IConnectableItemAutoIO getIoStorage() {
    return ioStorage;
  }

  @Override
  public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
    return new ContainerCableImportFilter(i, level, worldPosition, playerInventory, playerEntity);
  }

  @Override
  public Component getDisplayName() {
    return Component.translatable("block.storagenetwork.import_filter_kabel");
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
    ioStorage.upgrades.deserializeNBT(registries, compound.getCompound("upgrades"));
  }

  @Override
  protected void saveAdditional(CompoundTag compound, HolderLookup.Provider registries) {
    super.saveAdditional(compound, registries);
    compound.put("ioStorage", this.ioStorage.serializeNBT(registries));
    compound.put("upgrades", ioStorage.upgrades.serializeNBT(registries));
  }

  public static void clientTick(Level level, BlockPos blockPos, BlockState blockState, TileCableImportFilter tile) {}

  public static <E extends BlockEntity> void serverTick(Level level, BlockPos blockPos, BlockState blockState, TileCableImportFilter tile) {
    tile.refreshInventoryDirection();
  }
}
