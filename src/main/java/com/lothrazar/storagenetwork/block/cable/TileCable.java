package com.lothrazar.storagenetwork.block.cable;

import com.lothrazar.library.core.ITileFacade;
import com.lothrazar.storagenetwork.block.TileConnectable;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

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
  protected void loadAdditional(CompoundTag compound, HolderLookup.Provider registries) {
    super.loadAdditional(compound, registries);
    loadFacade(compound);
  }

  @Override
  protected void saveAdditional(CompoundTag compound, HolderLookup.Provider registries) {
    saveFacade(compound);
    super.saveAdditional(compound, registries);
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
}
