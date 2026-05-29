package com.lothrazar.storagenetwork.block.cable.linkfilter;

import com.lothrazar.storagenetwork.api.capabilities.CapabilityConnectable;
import com.lothrazar.storagenetwork.api.capabilities.CapabilityConnectableDefault;
import com.lothrazar.storagenetwork.block.cable.ContainerCable;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ContainerCableFilter extends ContainerCable {

  public final TileCableFilter tile;
  public CapabilityConnectableDefault cap;

  public ContainerCableFilter(int windowId, Level world, BlockPos pos, Inventory playerInv, Player player) {
    super(SsnRegistry.Menus.FILTER_KABEL.get(), windowId);
    tile = (TileCableFilter) world.getBlockEntity(pos);
    CapabilityConnectable rawLink = tile.getCapabilityLink();
    if (!(rawLink instanceof CapabilityConnectableDefault)) {
      return;
    }
    this.cap = (CapabilityConnectableDefault) rawLink;
    this.bindPlayerInvo(playerInv);
  }

  @Override
  public ItemStack quickMoveStack(Player player, int slotIndex) {
    return ItemStack.EMPTY;
  }

  @Override
  public boolean stillValid(Player playerIn) {
    return true;
  }
}
