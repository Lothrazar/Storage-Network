package com.lothrazar.storagenetwork.network;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.gui.ContainerNetwork;
import com.lothrazar.storagenetwork.registry.PacketRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class ClearRecipeMessage {

  public static void handle(ClearRecipeMessage message, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer player = ctx.get().getSender();
      ClearRecipeMessage.clearContainerRecipe(player, true);
    });
    ctx.get().setPacketHandled(true);
  }

  public static ClearRecipeMessage decode(FriendlyByteBuf buf) {
    return new ClearRecipeMessage();
  }

  public static void encode(ClearRecipeMessage msg, FriendlyByteBuf buf) {}

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
  static void clearContainerRecipe(ServerPlayer player, boolean doRefresh) {
    if (player.containerMenu instanceof ContainerNetwork) {
      ContainerNetwork container = (ContainerNetwork) player.containerMenu;
      CraftingContainer craftMatrix = container.getCraftMatrix();
      if (container.isCrafting() && craftMatrix != null) {
        TileMain root = container.getTileMain();
        for (int i = 0; i < 9; i++) {
          if (root == null) {
            break;
          }
          ItemStack stackInSlot = craftMatrix.getItem(i);
          if (stackInSlot.isEmpty()) {
            continue;
          }
          int numBeforeInsert = stackInSlot.getCount();
          int remainingAfter = root.insertStack(stackInSlot.copy(), false);
          if (numBeforeInsert == remainingAfter) {
            continue;
          }
          if (remainingAfter == 0) {
            craftMatrix.setItem(i, ItemStack.EMPTY);
          }
          else {
            craftMatrix.setItem(i, ItemHandlerHelper.copyStackWithSize(stackInSlot, remainingAfter));
          }
        }
        if (doRefresh) {
          final int THRESH = 256;
          List<ItemStack> list = root.getNetwork().getSortedStacksUpTo(THRESH);
          PacketRegistry.INSTANCE.sendTo(new StackRefreshClientMessage(list, new ArrayList<>()),
              player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
          container.broadcastChanges();
        }
      }
    }
  }
}
