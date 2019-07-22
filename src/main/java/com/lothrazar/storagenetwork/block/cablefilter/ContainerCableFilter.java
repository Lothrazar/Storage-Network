package com.lothrazar.storagenetwork.block.cablefilter;
import com.lothrazar.storagenetwork.StorageNetwork;
import com.lothrazar.storagenetwork.api.capability.IConnectableItemAutoIO;
import com.lothrazar.storagenetwork.api.capability.IConnectableLink;
import com.lothrazar.storagenetwork.block.cableinfilter.TileCableImportFilter;
import com.lothrazar.storagenetwork.capabilities.CapabilityConnectableAutoIO;
import com.lothrazar.storagenetwork.capabilities.CapabilityConnectableLink;
import com.lothrazar.storagenetwork.capabilities.StorageNetworkCapabilities;
import com.lothrazar.storagenetwork.gui.ContainerCable;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ContainerCableFilter extends ContainerCable {

  public final TileCableFilter tile;
  @Nullable
  public CapabilityConnectableLink link;


  public ContainerCableFilter(int windowId, World world, BlockPos pos, PlayerInventory playerInv, PlayerEntity player) {
    super(SsnRegistry.filterContainer, windowId);
    tile = (TileCableFilter) world.getTileEntity(pos);

    IConnectableLink rawLink = tile.getCapability(StorageNetworkCapabilities.CONNECTABLE_ITEM_STORAGE_CAPABILITY, null).orElse(null);
    if (!(rawLink instanceof CapabilityConnectableLink)) {
      return;
    }
    this.link = (CapabilityConnectableLink) rawLink;

    this.bindPlayerInvo(playerInv);

  }

  @Override
  public ItemStack transferStackInSlot(PlayerEntity player, int slotIndex) {
    return ItemStack.EMPTY;
  }

  @Override public boolean canInteractWith(PlayerEntity playerIn) {
    return true;
  }
}