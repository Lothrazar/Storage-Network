package com.lothrazar.storagenetwork.block.cable;

import com.lothrazar.storagenetwork.api.network.ConnectableNode;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.registry.ConfigRegistry;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;

public class CableHelpers {
  /**
   * This can only be called on the server side! It returns the Main tile entity for the given connectable.
   *
   * @param connectable
   * @return
   */
  public static TileMain getTileMainForConnectable(ConnectableNode connectable) {
    if (connectable == null || connectable.getMainPos() == null) {
      return null;
    }
    return connectable.getMainPos().getTileEntity(TileMain.class);
  }

  // TODO: block tag? 
  public static boolean isCableOverride(BlockState facingState) {
    return facingState.is(SsnRegistry.Blocks.MASTER.get())
        || facingState.is(SsnRegistry.Blocks.EXCHANGE.get())
        || facingState.is(SsnRegistry.Blocks.COLLECTOR.get())
        || facingState.is(SsnRegistry.Blocks.INVENTORY.get())
        || facingState.is(SsnRegistry.Blocks.REQUEST.get())
        || facingState.is(SsnRegistry.Blocks.REQUEST_EXPANDED.get())
        || facingState.is(SsnRegistry.Blocks.KABEL.get())
        || facingState.is(SsnRegistry.Blocks.STORAGE_CRADLE.get())
        || facingState.is(SsnRegistry.Blocks.RECEIVER.get());
  }

  public static boolean isInventory(Direction facing, LevelAccessor world, BlockPos facingPos) {
    if (facing == null) {
      return false;
    }
    BlockState blockState = world.getBlockState(facingPos);
    if (blockState.is(SsnRegistry.Blocks.EXCHANGE.get()) || blockState.is(SsnRegistry.Blocks.COLLECTOR.get())) {
      return false;
    }
    if (!ConfigRegistry.isTargetAllowed(blockState)) {
      return false;
    }
    BlockEntity neighbor = world.getBlockEntity(facingPos);
    if (neighbor == null) {
      return false;
    }
    if (world instanceof Level level) {
      return level.getCapability(Capabilities.ItemHandler.BLOCK, facingPos, facing.getOpposite()) != null;
    }
    return true;
  }
}
