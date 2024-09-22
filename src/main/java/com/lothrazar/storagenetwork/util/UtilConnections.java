package com.lothrazar.storagenetwork.util;

import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.registries.ForgeRegistries;

public class UtilConnections {

  public static boolean isTargetAllowed(BlockState state) {
    if (state.getBlock() == Blocks.AIR) {
      return false;
    }
    String blockId = ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString();
    for (String s : StorageNetworkMod.CONFIG.ignorelist()) {
      if (blockId.equals(s)) {
        return false;
      }
    }
    return true;
  }

  public static boolean isCableOverride(BlockState facingState) {
    return facingState.is(SsnRegistry.Blocks.MASTER.get())
        || facingState.is(SsnRegistry.Blocks.EXCHANGE.get())
        || facingState.is(SsnRegistry.Blocks.COLLECTOR.get())
        || facingState.is(SsnRegistry.Blocks.INVENTORY.get())
        || facingState.is(SsnRegistry.Blocks.REQUEST.get())
        || facingState.is(SsnRegistry.Blocks.KABEL.get());
  }

  public static boolean isInventory(Direction facing, LevelAccessor world, BlockPos facingPos) {
    if (facing == null) {
      return false;
    }
    BlockState blockState = world.getBlockState(facingPos);
    if (blockState.is(SsnRegistry.Blocks.EXCHANGE.get()) || blockState.is(SsnRegistry.Blocks.COLLECTOR.get())) {
      return false;
    }
    if (!UtilConnections.isTargetAllowed(blockState)) {
      return false;
    }
    BlockEntity neighbor = world.getBlockEntity(facingPos);
    if (neighbor != null
        && neighbor.getCapability(ForgeCapabilities.ITEM_HANDLER, facing.getOpposite()).orElse(null) != null) {
      return true;
    }
    return false;
  }
}
