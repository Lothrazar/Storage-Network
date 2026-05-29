package com.lothrazar.storagenetwork.block.cable.export;

import com.lothrazar.storagenetwork.api.EnumStorageDirection;
import com.lothrazar.storagenetwork.api.capabilities.CapabilityImportExport;
import com.lothrazar.storagenetwork.block.TileCableWithFacing;
import com.lothrazar.storagenetwork.api.capabilities.CapabilityImportExportDefault;
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

public class TileCableExport extends TileCableWithFacing implements MenuProvider {

  protected CapabilityImportExportDefault ioStorage;

  public TileCableExport(BlockPos pos, BlockState state) {
    super(SsnRegistry.Tiles.EXPORT_KABEL.get(), pos, state);
    this.ioStorage = new CapabilityImportExportDefault(this, EnumStorageDirection.OUT);
    this.ioStorage.getFilter().isAllowList = true;
  }

  public CapabilityImportExport getIoStorage() {
    return ioStorage;
  }

  @Override
  public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
    return new ContainerCableExportFilter(i, level, worldPosition, playerInventory, playerEntity);
  }

  @Override
  public Component getDisplayName() {
    return Component.translatable("block.storagenetwork.export_kabel");
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
    this.ioStorage.getFilter().isAllowList = true;
  }

  @Override
  protected void saveAdditional(CompoundTag compound, HolderLookup.Provider registries) {
    super.saveAdditional(compound, registries);
    compound.put("ioStorage", this.ioStorage.serializeNBT(registries));
    compound.put("upgrades", ioStorage.upgrades.serializeNBT(registries));
  }

  public static void clientTick(Level level, BlockPos blockPos, BlockState blockState, TileCableExport tile) {}

  public static <E extends BlockEntity> void serverTick(Level level, BlockPos blockPos, BlockState blockState, TileCableExport tile) {
    tile.refreshInventoryDirection();
  }
}
