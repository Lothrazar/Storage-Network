package com.lothrazar.storagenetwork.network;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.gui.ContainerNetwork;
import com.lothrazar.storagenetwork.registry.PacketRegistry;
import com.lothrazar.storagenetwork.util.UtilTileEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class InsertMessage {

  private int dim, mouseButton;
  private String query = "";

  public InsertMessage(int dim, int buttonID) {
    this.dim = dim;
    this.mouseButton = buttonID;
  }

  private InsertMessage() {}

  public InsertMessage withQuery(String q) {
    this.query = (q == null) ? "" : q;
    return this;
  }

  public static void handle(InsertMessage message, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer player = ctx.get().getSender();
      TileMain root = null;
      if (player.containerMenu instanceof ContainerNetwork) {
        root = ((ContainerNetwork) player.containerMenu).getTileMain();
      }
      if (root == null) {
        player.containerMenu.broadcastChanges();
        ctx.get().setPacketHandled(true);
        return;
      }
      final String q = message.query == null ? "" : message.query.trim();
      final int THRESH = 256;
      int rest;
      ItemStack send = ItemStack.EMPTY;
      ItemStack stack = player.containerMenu.getCarried();
      if (stack.isEmpty()) {
        // nothing to insert → just refresh view with current query (respect cap)
        final List<ItemStack> view = q.isEmpty()
            ? root.getNetwork().getSortedStacksUpTo(THRESH)
            : root.getNetwork().getSortedStacksFiltered(q, 4096);
        PacketRegistry.INSTANCE.sendTo(
            new StackRefreshClientMessage(view, new ArrayList<>()),
            player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        player.containerMenu.broadcastChanges();
        return;
      }
      if (message.mouseButton == UtilTileEntity.MOUSE_BTN_LEFT) {
        rest = root.insertStack(stack, false);
        if (rest != 0) {
          send = ItemHandlerHelper.copyStackWithSize(stack, rest);
        }
      }
      else if (message.mouseButton == UtilTileEntity.MOUSE_BTN_RIGHT) {
        ItemStack stack1 = stack.copy();
        stack1.setCount(1);
        stack.shrink(1);
        rest = root.insertStack(stack1, false) + stack.getCount();
        if (rest != 0) {
          send = ItemHandlerHelper.copyStackWithSize(stack, rest);
        }
      }
      player.containerMenu.setCarried(send);
      PacketRegistry.INSTANCE.sendTo(new StackResponseClientMessage(send),
          player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
      final List<ItemStack> list = q.isEmpty()
          ? root.getNetwork().getSortedStacksUpTo(THRESH)
          : root.getNetwork().getSortedStacksFiltered(q, 4096);
      PacketRegistry.INSTANCE.sendTo(
          new StackRefreshClientMessage(list, new ArrayList<>()),
          player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
      player.containerMenu.broadcastChanges();
    });
    ctx.get().setPacketHandled(true);
  }

  public static InsertMessage decode(FriendlyByteBuf buf) {
    InsertMessage message = new InsertMessage();
    message.dim = buf.readInt();
    message.mouseButton = buf.readInt();
    message.query = buf.readUtf(32767);
    return message;
  }

  public static void encode(InsertMessage msg, FriendlyByteBuf buf) {
    buf.writeInt(msg.dim);
    buf.writeInt(msg.mouseButton);
    buf.writeUtf(msg.query == null ? "" : msg.query);
  }
}
