package com.lothrazar.storagenetwork.gui.slot;

import java.util.ArrayList;
import java.util.List;
import com.google.common.collect.Lists;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.api.capabilities.DefaultItemStackMatcher;
import com.lothrazar.storagenetwork.gui.ContainerNetwork;
import com.lothrazar.storagenetwork.network.StackRefreshClientMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class SlotCraftingNetwork extends ResultSlot {

  private TileMain tileMain;
  private final ContainerNetwork parent;

  public SlotCraftingNetwork(ContainerNetwork parent, Player player, CraftingContainer craftingInventory, Container inventoryIn, int slotIndex, int xPosition, int yPosition) {
    super(player, craftingInventory, inventoryIn, slotIndex, xPosition, yPosition);
    this.parent = parent;
  }

  @Override
  public void onTake(Player playerIn, ItemStack stack) {
    if (playerIn.level().isClientSide) {
      return;
    }
    List<ItemStack> lis = Lists.newArrayList();
    for (int i = 0; i < parent.getCraftMatrix().getContainerSize(); i++) {
      lis.add(parent.getCraftMatrix().getItem(i).copy());
    }
    super.onTake(playerIn, stack);
    parent.broadcastChanges();
    for (int i = 0; i < parent.getCraftMatrix().getContainerSize(); i++) {
      if (parent.getCraftMatrix().getItem(i).isEmpty() && getTileMain() != null) {
        ItemStack req = getTileMain().request(
            !lis.get(i).isEmpty() ? new DefaultItemStackMatcher(lis.get(i), false, false) : null, 1, false);
        if (!req.isEmpty()) {
          parent.getCraftMatrix().setItem(i, req);
        }
      }
    }
    parent.broadcastChanges();
    if (playerIn instanceof ServerPlayer sp && getTileMain() != null) {
      List<ItemStack> stacks = getTileMain().getNetwork().getSortedStacks();
      PacketDistributor.sendToPlayer(sp, new StackRefreshClientMessage(stacks, new ArrayList<>()));
    }
    return;
  }

  public TileMain getTileMain() {
    return tileMain;
  }

  public void setTileMain(TileMain in) {
    this.tileMain = in;
  }
}
