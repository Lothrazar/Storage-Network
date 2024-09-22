package com.lothrazar.storagenetwork.network;

import com.lothrazar.storagenetwork.StorageNetwork;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.gui.ContainerNetwork;
import com.lothrazar.storagenetwork.registry.PacketRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.items.ItemHandlerHelper;

public class ClearRecipeMessage {

  public static void handle(ClearRecipeMessage message, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayerEntity player = ctx.get().getSender();
      ClearRecipeMessage.clearContainerRecipe(player, true);
    });
    ctx.get().setPacketHandled(true);
  }

  public static ClearRecipeMessage decode(PacketBuffer buf) {
    return new ClearRecipeMessage();
  }

  public static void encode(ClearRecipeMessage msg, PacketBuffer buf) {}

  /**
   * Should be in a public util.
   * 
   * Clears recipe and puts ingredients back in the network. If possible.
   * 
   * May stop partway and leave items in if network is disconnected.
   * 
   * @param player
   * @param doRefresh
   */
  static void clearContainerRecipe(ServerPlayerEntity player, boolean doRefresh) {
    if (player.openContainer instanceof ContainerNetwork) {
      ContainerNetwork container = (ContainerNetwork) player.openContainer;
      CraftingInventory craftMatrix = container.getCraftMatrix();
      if (craftMatrix == null) {
        // NullPointer fixed in NetworkInvenotry, second safety valve here
        StorageNetwork.log("error: no crafting grid here");
        return;
      }
      TileMain root = container.getTileMain();
      for (int i = 0; i < 9; i++) {
        if (root == null) {
          break;
        }
        ItemStack stackInSlot = craftMatrix.getStackInSlot(i);
        if (stackInSlot.isEmpty()) {
          continue;
        }
        int numBeforeInsert = stackInSlot.getCount();
        int remainingAfter = root.insertStack(stackInSlot.copy(), false);
        if (numBeforeInsert == remainingAfter) {
          continue;
        }
        if (remainingAfter == 0) {
          craftMatrix.setInventorySlotContents(i, ItemStack.EMPTY);
        }
        else {
          craftMatrix.setInventorySlotContents(i, ItemHandlerHelper.copyStackWithSize(stackInSlot, remainingAfter));
        }
      }
      if (doRefresh) {
        List<ItemStack> list = root.getStacks();
        PacketRegistry.INSTANCE.sendTo(new StackRefreshClientMessage(list, new ArrayList<>()),
            player.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
        container.detectAndSendChanges();
      }
    }
  }
}
