package com.lothrazar.storagenetwork.block;

import com.lothrazar.storagenetwork.api.EnumConnectType;
import com.lothrazar.storagenetwork.block.cable.BlockCable;
import com.lothrazar.storagenetwork.block.cable.TileCable;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.block.cable.CableHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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
      if (CableHelpers.isInventory(facing, level, relative)) {
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
  protected void loadAdditional(ValueInput input) {
    super.loadAdditional(input);
    Integer dirOrdinal = input.getInt("direction").orElse(null);
    this.direction = dirOrdinal != null ? Direction.values()[dirOrdinal] : null;
  }

  @Override
  protected void saveAdditional(ValueOutput output) {
    super.saveAdditional(output);
    if (direction != null) {
      output.putInt("direction", this.direction.ordinal());
    }
  }
}
