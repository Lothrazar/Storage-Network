package mrriegel.storagenetwork.cable;
import java.util.Arrays;
import mrriegel.storagenetwork.ModItems;
import mrriegel.storagenetwork.helper.StackWrapper;
import mrriegel.storagenetwork.tile.AbstractFilterTile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerCable extends Container {
  InventoryPlayer playerInv;
  public AbstractFilterTile tile;
  IInventory upgrades;
  public ContainerCable(AbstractFilterTile tile, InventoryPlayer playerInv) {
    this.playerInv = playerInv;
    this.tile = tile;
    upgrades = new InventoryBasic("upgrades", false, 4) {
      @Override
      public int getInventoryStackLimit() {
        return 4;
      }
    };
    if (tile instanceof TileCable && ((TileCable) tile).isUpgradeable()) {
      for (int i = 0; i < ((TileCable) tile).getUpgrades().size(); i++) {
        upgrades.setInventorySlotContents(i, ((TileCable) tile).getUpgrades().get(i));
      }
      for (int ii = 0; ii < 4; ii++) {
        this.addSlotToContainer(new Slot(upgrades, ii, 98 + ii * 18, 6) {
          @Override
          public boolean isItemValid(ItemStack stack) {
            return stack.getItem() == ModItems.upgrade && ((getStack() != null && getStack().getItemDamage() == stack.getItemDamage()) || !in(stack.getItemDamage()));
          }
          @Override
          public void onSlotChanged() {
            slotChanged();
            super.onSlotChanged();
          }
          private boolean in(int meta) {
            for (int i = 0; i < upgrades.getSizeInventory(); i++) {
              if (upgrades.getStackInSlot(i) != null && upgrades.getStackInSlot(i).getItemDamage() == meta)
                return true;
            }
            return false;
          }
        });
      }
    }
    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 9; ++j) {
        this.addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 55 + 34 + i * 18));
      }
    }
    for (int i = 0; i < 9; ++i) {
      this.addSlotToContainer(new Slot(playerInv, i, 8 + i * 18, 113 + 34));
    }
  }
  @Override
  public boolean canInteractWith(EntityPlayer playerIn) {
    return playerIn.getDistanceSq(tile.getPos().getX() + 0.5D, tile.getPos().getY() + 0.5D, tile.getPos().getZ() + 0.5D) <= 64.0D;
  }
  public void slotChanged() {
    if (tile instanceof TileCable) {
      ((TileCable) tile).setUpgrades(Arrays.<ItemStack> asList(null, null, null, null));
      for (int i = 0; i < upgrades.getSizeInventory(); i++) {
        ((TileCable) tile).getUpgrades().set(i, upgrades.getStackInSlot(i));
      }
    }
  }
  @Override
  public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
    Slot slot = this.inventorySlots.get(slotIndex);
    if (slot != null && slot.getHasStack()) {
      ItemStack itemstack1 = slot.getStack();
      if (itemstack1.isEmpty())
        return ItemStack.EMPTY;
      for (int i = 0; i < 18; i++) {
        if (tile.getFilter().get(i) == null && !in(new StackWrapper(itemstack1, 1))) {
          tile.getFilter().put(i, new StackWrapper(itemstack1.copy(), itemstack1.getCount()));
          tile.getOres().put(i, false);
          break;
        }
      }
    }
    return ItemStack.EMPTY;
  }
  public boolean in(StackWrapper stack) {
    for (int i = 0; i < 18; i++) {
      if (tile.getFilter().get(i) != null && tile.getFilter().get(i).getStack().isItemEqual(stack.getStack()))
        return true;
    }
    return false;
  }
}
