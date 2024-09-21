package com.lothrazar.storagenetwork.block.request;

import com.lothrazar.storagenetwork.block.BaseBlock;
import com.lothrazar.storagenetwork.network.SortClientMessage;
import com.lothrazar.storagenetwork.registry.PacketRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkHooks;

public class BlockRequest extends BaseBlock {

  public BlockRequest() {
    super(Material.IRON, "request");
  }

  @Override
  public boolean hasTileEntity(BlockState state) {
    return true;
  }

  @Override
  public TileEntity createTileEntity(BlockState state, IBlockReader world) {
    return new TileRequest();
  }

  @SuppressWarnings("deprecation")
  @Override
  public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
    if (state.getBlock() != newState.getBlock()) {
      //      TileEntity tileentity = worldIn.getTileEntity(pos);
      //      Containers.dropContents(worldIn, pos, tileentity);
      worldIn.updateComparatorOutputLevel(pos, getSelf());
      super.onReplaced(state, worldIn, pos, newState, isMoving);
    }
  }

  @Override
  public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
    if (!world.isRemote) {
      TileRequest tile = (TileRequest) world.getTileEntity(pos);
      if (tile.getMain() == null || tile.getMain().getBlockPos() == null) {
        return ActionResultType.PASS;
      }
      //sync
      ServerPlayerEntity sp = (ServerPlayerEntity) player;
      PacketRegistry.INSTANCE.sendTo(new SortClientMessage(pos, tile.isDownwards(), tile.getSort()),
          sp.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
      //end sync
      if (tile instanceof INamedContainerProvider) {
        NetworkHooks.openGui((ServerPlayerEntity) player, (INamedContainerProvider) tile, tile.getPos());
      }
      else {
        throw new IllegalStateException("Our named container provider is missing!");
      }
    }
    return ActionResultType.SUCCESS;
  }
}
