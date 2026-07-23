package com.lothrazar.storagenetwork.block.cable.inputfilter;

import com.lothrazar.storagenetwork.api.EnumStorageDirection;
import com.lothrazar.storagenetwork.api.capabilities.CapabilityImportExport;
import com.lothrazar.storagenetwork.block.TileCableWithFacing;
import com.lothrazar.storagenetwork.api.capabilities.CapabilityImportExportDefault;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileCableImportFilter extends TileCableWithFacing implements MenuProvider {

  protected CapabilityImportExportDefault ioStorage;

  public TileCableImportFilter(BlockPos pos, BlockState state) {
    super(SsnRegistry.Tiles.IMPORT_FILTER_KABEL.get(), pos, state);
    this.ioStorage = new CapabilityImportExportDefault(this, EnumStorageDirection.IN);
  }

  public CapabilityImportExport getIoStorage() {
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
  protected void loadAdditional(ValueInput input) {
    super.loadAdditional(input);
    this.ioStorage.deserialize(input.childOrEmpty("ioStorage"));
    ioStorage.upgrades.deserialize(input.childOrEmpty("upgrades"));
  }

  @Override
  protected void saveAdditional(ValueOutput output) {
    super.saveAdditional(output);
    this.ioStorage.serialize(output.child("ioStorage"));
    ioStorage.upgrades.serialize(output.child("upgrades"));
  }

  public static void clientTick(Level level, BlockPos blockPos, BlockState blockState, TileCableImportFilter tile) {}

  public static <E extends BlockEntity> void serverTick(Level level, BlockPos blockPos, BlockState blockState, TileCableImportFilter tile) {
    tile.refreshInventoryDirection();
    if (level.getGameTime() % 20L == 0L) {
      tile.tickChunkloadFor(tile.ioStorage.upgrades);
    }
  }
}
