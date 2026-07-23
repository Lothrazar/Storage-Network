package com.lothrazar.storagenetwork.block.cable.link;

import com.lothrazar.storagenetwork.block.cable.BlockCable;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BlockCableLink extends BlockCable {

  public BlockCableLink() {
    super();
  }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
    return createTickerHelper(type, SsnRegistry.Tiles.STORAGE_KABEL.get(), world.isClientSide() ? TileCableLink::clientTick : TileCableLink::serverTick);
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new TileCableLink(pos, state);
  }
}
