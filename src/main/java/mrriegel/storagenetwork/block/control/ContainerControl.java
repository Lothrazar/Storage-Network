package mrriegel.storagenetwork.block.control;

import mrriegel.storagenetwork.block.master.TileMaster;
import mrriegel.storagenetwork.gui.IStorageContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerControl extends Container implements IStorageContainer {

  protected InventoryPlayer playerInv;
  private TileMaster master;

  public ContainerControl(final TileMaster tile, final InventoryPlayer pi) {
    super();
    master = tile;
    this.playerInv = pi;
    int sq = 18;
    //TODO: base class /shared
    int yoffset = 85;
    //player inventory
    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 9; ++j) {
        this.addSlotToContainer(new Slot(playerInv, j + i * 9 + 9,
            8 + j * sq, yoffset + 55 + 34 + i * sq));
      }
    }
    //player hotbar
    for (int i = 0; i < 9; ++i) {
      this.addSlotToContainer(new Slot(playerInv, i, 8 + i * sq, yoffset + 113 + 34));
    }
  }

  @Override
  public void slotChanged() {
    //parent is abstract
    //seems to not happen from -shiftclick- crafting
    // UtilTileEntity.updateTile(getTileControl().getWorld(), getTileControl().getPos());
  }

  @Override
  public boolean canInteractWith(EntityPlayer playerIn) {
    return getTileMaster() != null;//
    //&& playerIn.getDistanceSq(getTileControl().getPos().getX() + 0.5D, getTileControl().getPos().getY() + 0.5D, getTileControl().getPos().getZ() + 0.5D) <= 64.0D;
  }

  @Override
  public TileMaster getTileMaster() {
    return master;
  }

  @Override
  public boolean isRequest() {
    return true;
  }

  @Override
  public InventoryCrafting getCraftMatrix() {
    return null;
  }

  @Override
  public ItemStack transferStackInSlot(EntityPlayer playerIn, int slotIndex) {
    return ItemStack.EMPTY;
  }
}
