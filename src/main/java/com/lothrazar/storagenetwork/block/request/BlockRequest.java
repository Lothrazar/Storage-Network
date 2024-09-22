package com.lothrazar.storagenetwork.block.request;

import com.lothrazar.storagenetwork.block.BaseBlock;
import com.lothrazar.storagenetwork.network.SortClientMessage;
import com.lothrazar.storagenetwork.registry.PacketRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkHooks;

public class BlockRequest extends BaseBlock {

  public BlockRequest() {
    super(Block.Properties.of(Material.METAL).strength(0.5F).sound(SoundType.STONE));
  }

  @Override
  public RenderShape getRenderShape(BlockState bs) {
    return RenderShape.MODEL;
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new TileRequest(pos, state);
  }

  @SuppressWarnings("deprecation")
  @Override
  public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
    if (!state.is(newState.getBlock())) {
      BlockEntity blockentity = worldIn.getBlockEntity(pos);
      if (blockentity instanceof Container) {
        Containers.dropContents(worldIn, pos, (Container) blockentity);
        worldIn.updateNeighbourForOutputSignal(pos, this);
      }
      super.onRemove(state, worldIn, pos, newState, isMoving);
    }
  }

  @Override
  public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
    if (!world.isClientSide) {
      TileRequest tile = (TileRequest) world.getBlockEntity(pos);
      if (tile.getMain() == null || tile.getMain().getBlockPos() == null) {
        return InteractionResult.PASS;
      }
      //sync
      ServerPlayer sp = (ServerPlayer) player;
      PacketRegistry.INSTANCE.sendTo(new SortClientMessage(pos, tile.isDownwards(), tile.getSort()), sp.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
      //end sync
      if (tile instanceof MenuProvider) {
        NetworkHooks.openScreen((ServerPlayer) player, (MenuProvider) tile, tile.getBlockPos());
      }
      else {
        throw new IllegalStateException("Our named container provider is missing!");
      }
    }
    return InteractionResult.SUCCESS;
  }
}
