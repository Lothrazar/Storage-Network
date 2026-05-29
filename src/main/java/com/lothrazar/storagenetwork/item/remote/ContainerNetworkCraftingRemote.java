package com.lothrazar.storagenetwork.item.remote;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.tuple.Triple;
import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.gui.ContainerNetwork;
import com.lothrazar.storagenetwork.gui.NetworkCraftingInventory;
import com.lothrazar.storagenetwork.gui.slot.SlotCraftingNetwork;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import com.lothrazar.storagenetwork.api.util.UtilInventory;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ContainerNetworkCraftingRemote extends ContainerNetwork {

  Map<Integer, ItemStack> matrixStacks = new HashMap<>();
  private TileMain root;
  private ItemStack remote;

  public ContainerNetworkCraftingRemote(int id, Inventory pInv) {
    super(SsnRegistry.Menus.CRAFTING_REMOTE.get(), id);
    this.player = pInv.player;
    this.remote = pInv.player.getMainHandItem();
    if (this.remote.getItem() != SsnRegistry.Items.CRAFTING_REMOTE.get()) {
      Triple<String, Integer, ItemStack> result = UtilInventory.getCurioRemote(player, SsnRegistry.Items.CRAFTING_REMOTE.get());
      this.remote = result.getRight();
    }
    DimPos dp = DimPos.getPosStored(remote);
    if (dp == null) {
      StorageNetworkMod.LOGGER.error("Remote opening with null pos Stored {} ", remote);
    }
    else {
      this.root = dp.getTileEntity(TileMain.class, player.level());
    }
    setCraftMatrix(new NetworkCraftingInventory(this, matrixStacks));
    this.playerInv = pInv;
    SlotCraftingNetwork slotCraftOutput = new SlotCraftingNetwork(this, playerInv.player, getCraftMatrix(), resultInventory, 0, 101, 128);
    slotCraftOutput.setTileMain(getTileMain());
    addSlot(slotCraftOutput);
    bindGrid();
    bindPlayerInvo(this.playerInv);
    bindHotbar();
    slotsChanged(getCraftMatrix());
  }

  @Override
  public boolean stillValid(Player playerIn) {
    return !remote.isEmpty();
  }

  @Override
  public TileMain getTileMain() {
    if (root == null) {
      DimPos dp = DimPos.getPosStored(remote);
      if (dp != null) {
        root = dp.getTileEntity(TileMain.class, player.level());
      }
    }
    return root;
  }

  @Override
  public void slotsChanged(Container inventoryIn) {
    if (recipeLocked) {
      //      StorageNetwork.log("recipe locked so onCraftMatrixChanged cancelled");
      return;
    }
    //    findMatchingRecipe(matrix);
    super.slotsChanged(inventoryIn);
  }

  @Override
  public void removed(Player playerIn) {
    super.removed(playerIn);
    for (int i = 0; i < getCraftMatrix().getContainerSize(); i++) {
      UtilInventory.dropItem(player.level(), playerIn.blockPosition(), getCraftMatrix().getItem(i));
    }
  }

  @Override
  public void slotChanged() {}

  @Override
  public boolean isCrafting() {
    return true;
  }

  @Override
  public boolean isFullStackCraft() {
    return remote != null && ItemRemote.isFullStackCraft(remote);
  }

  public ItemStack getRemote() {
    return remote;
  }
}
