package com.lothrazar.storagenetwork.block.cable.processing;

import com.lothrazar.storagenetwork.block.cable.BlockCable;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class BlockCableProcess extends BlockCable {

  public BlockCableProcess(Identifier id) {
    super(id);
  }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
    return createTickerHelper(type, SsnRegistry.Tiles.PROCESS_KABEL.get(), world.isClientSide() ? TileCableProcess::clientTick : TileCableProcess::serverTick);
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new TileCableProcess(pos, state);
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
    }
    return InteractionResult.SUCCESS;
  }
}
