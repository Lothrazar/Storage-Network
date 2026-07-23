package com.lothrazar.storagenetwork.block.cable.linkfilter;

import com.lothrazar.storagenetwork.block.cable.BlockCable;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class BlockCableFilter extends BlockCable {

  public BlockCableFilter() {
    super();
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new TileCableFilter(pos, state);
  }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
    return createTickerHelper(type, SsnRegistry.Tiles.FILTER_KABEL.get(), world.isClientSide() ? TileCableFilter::clientTick : TileCableFilter::serverTick);
  }

  @Override
  public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player playerIn, BlockHitResult result) {
    if (!world.isClientSide()) {
      BlockEntity tile = world.getBlockEntity(pos);
      if (tile instanceof MenuProvider) {
        ServerPlayer player = (ServerPlayer) playerIn;
        player.connection.send(tile.getUpdatePacket());
        player.openMenu((MenuProvider) tile, buf -> buf.writeBlockPos(tile.getBlockPos()));
      }
      else {
        throw new IllegalStateException("Our named container provider is missing!" + tile);
      }
    }
    return InteractionResult.SUCCESS;
  }
}
