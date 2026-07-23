package com.lothrazar.storagenetwork.block.cable;

import com.lothrazar.library.core.ITileFacade;
import com.lothrazar.storagenetwork.api.capabilities.CapabilityImportExport;
import com.lothrazar.storagenetwork.api.capabilities.CapabilityImportExportDefault;
import com.lothrazar.storagenetwork.block.TileConnectable;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import com.lothrazar.storagenetwork.registry.StorageNetworkCapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Base class for TileCable
 *
 */
public class TileCable extends TileConnectable implements ITileFacade {

  private CompoundTag facade = null;

  public TileCable(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
    super(tileEntityTypeIn, pos, state);
  }

  public TileCable(BlockPos pos, BlockState state) {
    super(SsnRegistry.Tiles.KABEL.get(), pos, state);
  }

  @Override
  protected void loadAdditional(ValueInput input) {
    super.loadAdditional(input);
    loadFacade(input.read("facadeData", CompoundTag.CODEC).orElse(new CompoundTag()));
  }

  @Override
  protected void saveAdditional(ValueOutput output) {
    CompoundTag facadeTag = new CompoundTag();
    saveFacade(facadeTag);
    if (!facadeTag.isEmpty()) {
      output.store("facadeData", CompoundTag.CODEC, facadeTag);
    }
    super.saveAdditional(output);
  }

  public static TileCable getTileCable(BlockGetter world, BlockPos pos) {
    BlockEntity tile = world.getBlockEntity(pos);
    if (tile instanceof TileCable te) {
      return te;
    }
    return null;
  }

  public BlockState getFacadeState() {
    return getFacadeState(level); // level is null on world load, ITileFacade default handles that
  }

  @Override
  public CompoundTag getFacade() {
    return facade;
  }

  @Override
  public void setFacade(CompoundTag facade) {
    this.facade = facade;
  }

  // Block#onRemove is gone in 26.1; this cleanup (used to live in BlockCable#onRemove) now
  // runs here, called by the game at the equivalent point in the block-removal sequence
  // (only when the block type is actually changing, same as the old "state != newState" guard).
  @Override
  public void preRemoveSideEffects(BlockPos pos, BlockState state) {
    super.preRemoveSideEffects(pos, state);
    if (level == null) {
      return;
    }
    var resourceHandler = level.getCapability(Capabilities.Item.BLOCK, pos, null);
    IItemHandler items = resourceHandler == null ? null : IItemHandler.of(resourceHandler);
    if (items != null) {
      for (int i = 0; i < items.getSlots(); ++i) {
        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), items.getStackInSlot(i));
      }
      level.updateNeighbourForOutputSignal(pos, state.getBlock());
    }
    CapabilityImportExport connectable = level.getCapability(StorageNetworkCapabilities.CONNECTABLE_AUTO_IO, pos, null);
    if (connectable instanceof CapabilityImportExportDefault filterCable) {
      for (int i = 0; i < filterCable.upgrades.getSlots(); ++i) {
        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), filterCable.upgrades.getStackInSlot(i));
      }
      level.updateNeighbourForOutputSignal(pos, state.getBlock());
    }
    // Release any chunkload ticket this cable was holding.
    releaseChunkTicket();
  }
}
