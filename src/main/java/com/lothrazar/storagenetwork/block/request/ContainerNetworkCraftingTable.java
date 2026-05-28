package com.lothrazar.storagenetwork.block.request;

import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.gui.ContainerNetwork;
import com.lothrazar.storagenetwork.gui.NetworkCraftingInventory;
import com.lothrazar.storagenetwork.gui.slot.SlotCraftingNetwork;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;

public class ContainerNetworkCraftingTable extends ContainerNetwork {

  private final TileRequest tileRequest;
  private final ContainerLevelAccess access;

  public ContainerNetworkCraftingTable(int windowId, Level world, BlockPos pos, Inventory playerInv, Player player) {
    super(SsnRegistry.Menus.REQUEST.get(), windowId);
    tileRequest = (TileRequest) world.getBlockEntity(pos);
    setCraftMatrix(new NetworkCraftingInventory(this));
    access = ContainerLevelAccess.create(world, pos);
    this.playerInv = playerInv;
    SlotCraftingNetwork slotCraftOutput = new SlotCraftingNetwork(this, playerInv.player, getCraftMatrix(), resultInventory, 0,
        101, yPlayer - 46);
    slotCraftOutput.setTileMain(getTileMain());
    addSlot(slotCraftOutput);
    bindGrid();
    bindPlayerInvo(this.playerInv);
    bindHotbar();
    slotsChanged(getCraftMatrix());
  }

  @Override
  public boolean isCrafting() {
    return true;
  }

  @Override
  public boolean isFullStackCraft() {
    return tileRequest != null && tileRequest.isFullStackCraft();
  }

  @Override
  public void removed(Player player) {
    super.removed(player);
    //the contents of the crafting matrix gets returned to the player
    this.access.execute((level, pos) -> {
      this.clearContainer(player, getCraftMatrix());
    });
  }

  @Override
  public void slotChanged() {
    //parent is abstract
  }

  @Override
  public boolean stillValid(Player playerIn) {
    TileRequest table = getTileRequest();
    BlockPos pos = table.getBlockPos();
    return playerIn.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
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
