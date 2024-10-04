package com.lothrazar.storagenetwork.block.cable;

import com.lothrazar.storagenetwork.block.TileConnectable;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Base class for TileCable
 *
 */
public class TileCable extends TileConnectable {

  private static final String NBT_FACADE = "facade";
  private CompoundTag facadeState = null;

  public TileCable(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
    super(tileEntityTypeIn, pos, state);
  }

  public TileCable(BlockPos pos, BlockState state) {
    super(SsnRegistry.Tiles.KABEL.get(), pos, state);
  }

  @Override
  public void load(CompoundTag compound) {
    super.load(compound);
    if (compound.contains(NBT_FACADE)) {
      setFacadeState(compound.getCompound(NBT_FACADE));
    }
    else {
      setFacadeState(null);
    }
  }

  @Override
  public void saveAdditional(CompoundTag compound) {
    super.saveAdditional(compound);
    if (facadeState != null) {
      compound.put(NBT_FACADE, facadeState);
    }
  }

  public static TileCable getTileCable(BlockGetter world, BlockPos pos) {
    BlockEntity tile = world.getBlockEntity(pos);
    if (tile instanceof TileCable te) {
      return te;
    }
    return null;
  }

  public BlockState getFacadeState() {
    if (level == null || facadeState == null || facadeState.isEmpty()) {
      return null; // level is null on world load 
    }
    BlockState stateFound = NbtUtils.readBlockState(level.holderLookup(Registries.BLOCK), facadeState);
    return stateFound;
  }

  public void setFacadeState(CompoundTag facadeState) {
    this.facadeState = facadeState;
  }
}
