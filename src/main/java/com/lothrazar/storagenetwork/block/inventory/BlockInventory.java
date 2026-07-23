package com.lothrazar.storagenetwork.block.inventory;

import com.lothrazar.storagenetwork.block.EntityBlockConnectable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockInventory extends EntityBlockConnectable {

  public BlockInventory(Identifier id) {
    super(id);
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new TileInventory(pos, state);
  }
}
