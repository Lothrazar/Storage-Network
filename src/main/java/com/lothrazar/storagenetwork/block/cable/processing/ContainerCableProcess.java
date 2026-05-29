package com.lothrazar.storagenetwork.block.cable.processing;

import com.lothrazar.storagenetwork.block.cable.ContainerCable;
import com.lothrazar.storagenetwork.capability.CapabilityConnectableProcessing;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ContainerCableProcess extends ContainerCable {

  public final TileCableProcess tile;
  public final CapabilityConnectableProcessing cap;

  public ContainerCableProcess(int windowId, Level world, BlockPos pos, Inventory playerInv, Player player) {
    super(SsnRegistry.Menus.PROCESS_KABEL.get(), windowId);
    this.tile = (TileCableProcess) world.getBlockEntity(pos);
    this.cap = tile.getCap();
    this.player = playerInv.player;
    this.world = this.player.level();
    // player inventory (rows) - pushed down to make room for the recipe grids
    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 9; ++j) {
        addSlot(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 118 + i * 18));
      }
    }
    // hotbar
    for (int i = 0; i < 9; ++i) {
      addSlot(new Slot(playerInv, i, 8 + i * 18, 176));
    }
  }

  @Override
  public ItemStack quickMoveStack(Player player, int slotIndex) {
    return ItemStack.EMPTY;
  }

  @Override
  public boolean stillValid(Player playerIn) {
    return true;
  }
}
