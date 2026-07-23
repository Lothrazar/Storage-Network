package com.lothrazar.storagenetwork.block.cable.input;

import com.lothrazar.storagenetwork.api.EnumStorageDirection;
import com.lothrazar.storagenetwork.api.capabilities.CapabilityImportExport;
import com.lothrazar.storagenetwork.block.TileCableWithFacing;
import com.lothrazar.storagenetwork.api.capabilities.CapabilityImportExportDefault;
import com.lothrazar.storagenetwork.registry.ConfigRegistry;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileCableIO extends TileCableWithFacing {

  protected CapabilityImportExportDefault ioStorage;

  public TileCableIO(BlockPos pos, BlockState state) {
    super(SsnRegistry.Tiles.IMPORT_KABEL.get(), pos, state);
    this.ioStorage = new CapabilityImportExportDefault(this, EnumStorageDirection.IN);
  }

  public CapabilityImportExport getIoStorage() {
    return ioStorage;
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
  }

  @Override
  protected void saveAdditional(ValueOutput output) {
    super.saveAdditional(output);
    this.ioStorage.serialize(output.child("ioStorage"));
  }

  public static void clientTick(Level level, BlockPos blockPos, BlockState blockState, TileCableIO tile) {}

  public static <E extends BlockEntity> void serverTick(Level level, BlockPos blockPos, BlockState blockState, TileCableIO tile) {
    tile.refreshInventoryDirection();
    if (level.getGameTime() % ConfigRegistry.CHUNKLOADER_REFRESH_TICKS.get() == 0L) {
      tile.tickChunkloadFor(tile.ioStorage.upgrades);
    }
  }
}
