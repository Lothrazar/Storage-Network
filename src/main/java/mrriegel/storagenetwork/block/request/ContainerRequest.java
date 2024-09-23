package mrriegel.storagenetwork.block.request;

import java.util.ArrayList;
import java.util.List;
import mrriegel.storagenetwork.block.master.TileMaster;
import mrriegel.storagenetwork.gui.ContainerNetworkBase;
import mrriegel.storagenetwork.gui.InventoryCraftingNetwork;
import mrriegel.storagenetwork.network.StackRefreshClientMessage;
import mrriegel.storagenetwork.registry.PacketRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class ContainerRequest extends ContainerNetworkBase {

  private TileRequest tileRequest;

  public ContainerRequest(final TileRequest tile, final InventoryPlayer playerInv) {
    matrix = new InventoryCraftingNetwork(this, tile.matrix);
    this.setTileRequest(tile);
    this.playerInv = playerInv;
    result = new InventoryCraftResult();
    SlotCraftingNetwork slotCraftOutput = new SlotCraftingNetwork(playerInv.player, matrix, result, 0, 101, 128);
    slotCraftOutput.setTileMaster(this.getTileMaster());
    this.addSlotToContainer(slotCraftOutput);
    bindGrid();
    bindPlayerInvo(playerInv);
    bindHotbar();
    this.onCraftMatrixChanged(this.matrix);
  }

  @Override
  public void onContainerClosed(EntityPlayer playerIn) {
    if (!playerIn.world.isRemote) {
      this.clearContainer(playerIn, playerIn.world, matrix);
    }
    super.onContainerClosed(playerIn);
  }

  @Override
  public void bindHotbar() {
    //player hotbar
    for (int i = 0; i < 9; ++i) {
      this.addSlotToContainer(new Slot(playerInv, i, 8 + i * 18, 232));
    }
  }

  @Override
  public void onCraftMatrixChanged(IInventory inventoryIn) {
    if (this.recipeLocked) {
      //StorageNetwork.log("recipe locked so onCraftMatrixChanged cancelled");
      return;
    }
    findMatchingRecipe(matrix);
  }

  @Override
  public void slotChanged() {
    //parent is abstract
    //seems to not happen from -shiftclick- crafting
    //    for (int i = 0; i < 9; i++) {
    //      getTileRequest().matrix.put(i, matrix.getStackInSlot(i));
    //    }
    //    UtilTileEntity.updateTile(getTileRequest().getWorld(), getTileRequest().getPos());
  }

  @Override
  public boolean canInteractWith(EntityPlayer playerIn) {
    TileMaster tileMaster = this.getTileMaster();
    TileRequest table = getTileRequest();
    if (tileMaster != null &&
        !table.getWorld().isRemote && table.getWorld().getTotalWorldTime() % 40 == 0) {
      List<ItemStack> list = tileMaster.getStacks();
      PacketRegistry.INSTANCE.sendTo(new StackRefreshClientMessage(list, new ArrayList<>()), (EntityPlayerMP) playerIn);
    }
    BlockPos pos = table.getPos();
    return playerIn.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
  }

  @Override
  public boolean canMergeSlot(ItemStack stack, Slot slot) {
    return slot.inventory != this.result && super.canMergeSlot(stack, slot);
  }

  @Override
  public TileMaster getTileMaster() {
    if (getTileRequest() == null || getTileRequest().getMaster() == null) {
      return null;
    }
    return getTileRequest().getMaster().getTileEntity(TileMaster.class);
  }

  public TileRequest getTileRequest() {
    return tileRequest;
  }

  public void setTileRequest(TileRequest tileRequest) {
    this.tileRequest = tileRequest;
  }

  @Override
  public boolean isRequest() {
    return true;
  }
}
