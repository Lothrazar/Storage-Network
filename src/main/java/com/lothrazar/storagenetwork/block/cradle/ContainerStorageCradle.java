package com.lothrazar.storagenetwork.block.cradle;

import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.SlotItemHandler;

public class ContainerStorageCradle extends AbstractContainerMenu {

  public final TileStorageCradle tile;
  public static final int HELD_ROW_X = 8;
  public static final int HELD_ROW_Y = 34;
  public static final int HELD_ROW_X_COUNT = TileStorageCradle.HOLDER_SIZE;

  public ContainerStorageCradle(int windowId, Level world, BlockPos pos, Inventory playerInv, Player player) {
    super(SsnRegistry.Menus.STORAGE_CRADLE.get(), windowId);
    this.tile = (TileStorageCradle) world.getBlockEntity(pos);
    for (int i = 0; i < TileStorageCradle.HOLDER_SIZE; i++) {
      this.addSlot(new SlotItemHandler(tile.getHolder(), i, HELD_ROW_X + i * 18, HELD_ROW_Y) {

        @Override
        public int getMaxStackSize() {
          return 1;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
          return !stack.isEmpty()
              && stack.getCapability(Capabilities.ItemHandler.ITEM) != null;
        }
      });
    }
    // player inventory
    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 9; ++j) {
        addSlot(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
      }
    }
    // hotbar
    for (int i = 0; i < 9; ++i) {
      addSlot(new Slot(playerInv, i, 8 + i * 18, 142));
    }
  }

  @Override
  public ItemStack quickMoveStack(Player player, int slotIndex) {
    Slot slot = this.slots.get(slotIndex);
    if (slot == null || !slot.hasItem()) {
      return ItemStack.EMPTY;
    }
    ItemStack stackInSlot = slot.getItem();
    ItemStack copy = stackInSlot.copy();
    final int holderEnd = TileStorageCradle.HOLDER_SIZE; // exclusive
    final int playerEnd = holderEnd + 36;
    if (slotIndex < holderEnd) {
      // FROM cradle TO inventory
      if (!this.moveItemStackTo(stackInSlot, holderEnd, playerEnd, true)) {
        return ItemStack.EMPTY;
      }
    }
    else {
      // FROM inventory TO cradle (only if compatible)
      if (stackInSlot.getCapability(Capabilities.ItemHandler.ITEM) == null) {
        return ItemStack.EMPTY;
      }
      if (!this.moveItemStackTo(stackInSlot, 0, holderEnd, false)) {
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
