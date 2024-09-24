package com.lothrazar.storagenetwork.block;

import com.lothrazar.storagenetwork.api.EnumConnectType;
import com.lothrazar.storagenetwork.block.cable.BlockCable;
import com.lothrazar.storagenetwork.block.cable.TileCable;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.util.UtilConnections;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TileCableWithFacing extends TileCable {

  private Direction direction = null;

  public TileCableWithFacing(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
    super(tileEntityTypeIn, pos, state);
  }

  public BlockPos getFacingPosition() {
    return this.getBlockPos().relative(direction);
  }

  public void setDirection(Direction direction) {
    this.direction = direction;
  }

  public void findNewDirection() {
    for (Direction facing : Direction.values()) {
      BlockPos relative = worldPosition.relative(facing);
      if (UtilConnections.isInventory(facing, level, relative)) {
        setDirection(facing);
        return;
      }
    }
    setDirection(null);
  }

  public void refreshInventoryDirection() {
    if (direction == null) {
      this.findNewDirection();
      if (direction != null) {
        BlockState newState = BlockCable.cleanBlockState(this.getBlockState());
        newState = newState.setValue(BlockCable.FACING_TO_PROPERTY_MAP.get(direction), EnumConnectType.INVENTORY);
        level.setBlockAndUpdate(worldPosition, newState);
      }
    }
  }

  public TileMain getTileMain() {
    if (getMain() == null) {
      return null;
    }
    return getMain().getTileEntity(TileMain.class);
  }

  @Override
  public void load(CompoundTag compound) {
    super.load(compound);
    if (compound.contains("direction")) {
      this.direction = Direction.values()[(compound.getInt("direction"))];
    }
    else {
      this.direction = null;
    }
  }

  @Override
  public void saveAdditional(CompoundTag compound) {
    super.saveAdditional(compound);
    if (direction != null) {
      compound.putInt("direction", this.direction.ordinal());
    }
  }
}
