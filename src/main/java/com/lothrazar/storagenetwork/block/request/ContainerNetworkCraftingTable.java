package com.lothrazar.storagenetwork.block.request;

import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.gui.ContainerNetwork;
import com.lothrazar.storagenetwork.gui.NetworkCraftingInventory;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import javax.annotation.Nonnull;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ContainerNetworkCraftingTable extends ContainerNetwork {

  private final TileRequest tileRequest;

  public ContainerNetworkCraftingTable(int windowId, World world, BlockPos pos, PlayerInventory playerInv, PlayerEntity player) {
    super(SsnRegistry.REQUESTCONTAINER, windowId);
    tileRequest = (TileRequest) world.getTileEntity(pos);
    matrix = new NetworkCraftingInventory(this);
    this.playerInv = playerInv;
    SlotCraftingNetwork slotCraftOutput = new SlotCraftingNetwork(this, playerInv.player, matrix, resultInventory, 0, 101, 128);
    slotCraftOutput.setTileMain(getTileMain());
    addSlot(slotCraftOutput);
    bindGrid();
    bindPlayerInvo(this.playerInv);
    bindHotbar();
    onCraftMatrixChanged(matrix);
    this.removeListener(null);
  }

  // in 1.18.2 this is just removed(player) 
  @Override
  public void onContainerClosed(@Nonnull PlayerEntity playerIn) {
    //the contents of the crafting matrix gets returned to the player when cleaning up
    if (!playerIn.world.isRemote) {
      this.clearContainer(playerIn, playerIn.world, this.matrix);
    }
    super.onContainerClosed(playerIn);
  }

  @Override
  public boolean isCrafting() {
    return true;
  }

  @Override
  public void slotChanged() {
    //parent is abstract
  }

  @Override
  public boolean canInteractWith(PlayerEntity playerIn) {
    TileRequest table = getTileRequest();
    BlockPos pos = table.getPos();
    return playerIn.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
  }

  @Override
  public TileMain getTileMain() {
    if (getTileRequest() == null || getTileRequest().getMain() == null) {
      return null;
    }
    return getTileRequest().getMain().getTileEntity(TileMain.class);
  }

  public TileRequest getTileRequest() {
    return tileRequest;
  }
}
