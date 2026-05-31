package com.lothrazar.storagenetwork.block.cradle;

import com.lothrazar.storagenetwork.registry.CradleAdapterRegistry;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.SlotItemHandler;

public class ContainerCradle extends AbstractContainerMenu {

  public final TileCradle tile;
  public static final int HELD_GRID_COLS = 3;
  // 3x3 grid centered horizontally in the 176-wide GUI: (176 - 3*18) / 2 = 61
  public static final int HELD_GRID_X = 61;
  public static final int HELD_GRID_Y = 17;

  public ContainerCradle(int windowId, Level world, BlockPos pos, Inventory playerInv, Player player) {
    super(SsnRegistry.Menus.CRADLE.get(), windowId);
    this.tile = (TileCradle) world.getBlockEntity(pos);
    for (int i = 0; i < TileCradle.HOLDER_SIZE; i++) {
      int col = i % HELD_GRID_COLS;
      int row = i / HELD_GRID_COLS;
      this.addSlot(new SlotItemHandler(tile.getHolder(), i, HELD_GRID_X + col * 18, HELD_GRID_Y + row * 18) {

        @Override
        public int getMaxStackSize() {
          return 1;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
          return CradleAdapterRegistry.accepts(stack);
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
    final int holderEnd = TileCradle.HOLDER_SIZE; // exclusive
    final int playerEnd = holderEnd + 36;
    if (slotIndex < holderEnd) {
      // FROM cradle TO inventory
      if (!this.moveItemStackTo(stackInSlot, holderEnd, playerEnd, true)) {
        return ItemStack.EMPTY;
      }
    }
    else {
      // FROM inventory TO cradle (only if compatible)
      if (!CradleAdapterRegistry.accepts(stackInSlot)) {
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
