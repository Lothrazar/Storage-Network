package com.lothrazar.storagenetwork.block.receiver;

import com.lothrazar.storagenetwork.api.UpgradeType;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.SlotItemHandler;

public class ContainerNetworkReceiver extends AbstractContainerMenu {

  public final TileNetworkReceiver tile;
  public static final int UPGRADE_X = 80;
  public static final int UPGRADE_Y = 35;
  private int activeSync;

  public ContainerNetworkReceiver(int windowId, Level world, BlockPos pos, Inventory playerInv, Player player) {
    super(SsnRegistry.Menus.RECEIVER.get(), windowId);
    this.tile = (TileNetworkReceiver) world.getBlockEntity(pos);
    this.addSlot(new SlotItemHandler(tile.getUpgrades(), 0, UPGRADE_X, UPGRADE_Y) {

      @Override
      public boolean mayPlace(ItemStack stack) {
        return UpgradeType.isUpgradeOfType(stack.getItem(), UpgradeType.CHUNKLOAD);
      }
    });
    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 9; ++j) {
        addSlot(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
      }
    }
    for (int i = 0; i < 9; ++i) {
      addSlot(new Slot(playerInv, i, 8 + i * 18, 142));
    }
    this.addDataSlot(new DataSlot() {

      @Override
      public int get() {
        return tile != null && tile.isActive() ? 1 : 0;
      }

      @Override
      public void set(int v) {
        activeSync = v;
      }
    });
  }

  public boolean isActiveSynced() {
    return activeSync != 0;
  }

  @Override
  public ItemStack quickMoveStack(Player player, int slotIndex) {
    Slot slot = this.slots.get(slotIndex);
    if (slot == null || !slot.hasItem()) {
      return ItemStack.EMPTY;
    }
    ItemStack stackInSlot = slot.getItem();
    ItemStack copy = stackInSlot.copy();
    final int upgradeEnd = 1;
    final int playerEnd = upgradeEnd + 36;
    if (slotIndex < upgradeEnd) {
      if (!this.moveItemStackTo(stackInSlot, upgradeEnd, playerEnd, true)) {
        return ItemStack.EMPTY;
      }
    }
    else {
      if (!UpgradeType.isUpgradeOfType(stackInSlot.getItem(), UpgradeType.CHUNKLOAD)) {
        return ItemStack.EMPTY;
      }
      if (!this.moveItemStackTo(stackInSlot, 0, upgradeEnd, false)) {
        return ItemStack.EMPTY;
      }
    }
    if (stackInSlot.isEmpty()) {
      slot.set(ItemStack.EMPTY);
    }
    else {
      slot.setChanged();
    }
    return copy;
  }

  @Override
  public boolean stillValid(Player playerIn) {
    return true;
  }
}
