package com.lothrazar.storagenetwork.block.collection;

import com.lothrazar.storagenetwork.api.IConnectable;
import com.lothrazar.storagenetwork.block.cable.ContainerCable;
import com.lothrazar.storagenetwork.capability.CapabilityConnectable;
import com.lothrazar.storagenetwork.item.ItemUpgrade;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ContainerCollectionFilter extends ContainerCable {

  public final TileCollection tile;
  public CapabilityConnectable cap;

  public ContainerCollectionFilter(int windowId, Level world, BlockPos pos, Inventory playerInv, Player player) {
    super(SsnRegistry.Menus.COLLECTOR.get(), windowId);
    tile = (TileCollection) world.getBlockEntity(pos);
    IConnectable connectable = tile.getConnectable();
    if (connectable instanceof CapabilityConnectable pc) {
      this.cap = pc;
    }
    this.bindPlayerInvo(playerInv);
  }

  @Override
  public ItemStack quickMoveStack(Player player, int slotIndex) {
    Slot slot = this.slots.get(slotIndex);
    //in range [4,39] means its coming FROM inventory
    // [0,3] is the filter list
    if (slot != null && slot.hasItem()) {
      ItemStack stackInSlot = slot.getItem();
      if (stackInSlot.getItem() instanceof ItemUpgrade) {
        if (4 <= slotIndex && slotIndex <= 39) {
          //FROM inventory to upgrade slots
          if (!this.moveItemStackTo(stackInSlot, 0, 4, true)) {
            return ItemStack.EMPTY;
          }
        }
        else if (0 <= slotIndex && slotIndex <= 3) {
          //FROM upgrade slots TO inventory
          if (!this.moveItemStackTo(stackInSlot, 0, 35, true)) {
            return ItemStack.EMPTY;
          }
        }
      }
    }
    return ItemStack.EMPTY;
  }

  @Override
  public boolean stillValid(Player playerIn) {
    return true;
  }
}
