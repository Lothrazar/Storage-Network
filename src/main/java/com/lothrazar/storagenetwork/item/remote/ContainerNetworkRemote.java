package com.lothrazar.storagenetwork.item.remote;

import org.apache.commons.lang3.tuple.Triple;
import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.gui.ContainerNetwork;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import com.lothrazar.storagenetwork.util.UtilInventory;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ContainerNetworkRemote extends ContainerNetwork {

  private TileMain root;
  private ItemStack remote;

  public ContainerNetworkRemote(int id, Inventory pInv) {
    super(SsnRegistry.Menus.INVENTORY_REMOTE.get(), id);
    this.player = pInv.player;
    Triple<String, Integer, ItemStack> result = UtilInventory.getCurioRemote(pInv.player, SsnRegistry.Items.INVENTORY_REMOTE.get());
    this.remote = result.getRight();
    DimPos dp = DimPos.getPosStored(remote);
    Level level = player.level();
    if (dp == null) {
      StorageNetworkMod.LOGGER.error(level.isClientSide + "=client||Remote opening with null pos Stored {} ", result);
    }
    else {
      this.root = dp.getTileEntity(TileMain.class, level);
    }

    this.playerInv = pInv;
    bindPlayerInvo(this.playerInv);
    bindHotbar();
  }

  @Override
  public boolean stillValid(Player playerIn) {
    //does not store itemstack inventory, and opens from curios so no security here. unless it dissapears
    return !getRemote().isEmpty();
  }

  public ItemStack getRemote() {
    return remote;
  }

  @Override
  public TileMain getTileMain() {
    return root;
  }

  @Override
  public void slotChanged() {}

  @Override
  public boolean isCrafting() {
    return false;
  }
}
