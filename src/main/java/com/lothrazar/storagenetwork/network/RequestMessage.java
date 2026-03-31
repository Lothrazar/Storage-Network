package com.lothrazar.storagenetwork.network;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.capability.handler.ItemStackMatcher;
import com.lothrazar.storagenetwork.gui.ContainerNetwork;
import com.lothrazar.storagenetwork.registry.PacketRegistry;
import com.lothrazar.storagenetwork.util.UtilTileEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class RequestMessage {

  private int mouseButton = 0;
  private ItemStack stack = ItemStack.EMPTY;
  private boolean shift;
  private boolean ctrl;
  private String query = "";

  @Override
  public String toString() {
    return "RequestMessage [mouseButton=" + mouseButton + ", shift=" + shift + ", ctrl=" + ctrl + ", stack=" + stack.toString() + "]";
  }

  public RequestMessage() {}

  public RequestMessage(int id, ItemStack stackIn, boolean shift, boolean ctrl) {
    mouseButton = id;
    this.stack = stackIn.copy();
    if (this.stack.getCount() > 64) {
      this.stack.setCount(64); //important or it will be killed by a filter
    }
    this.shift = shift;
    this.ctrl = ctrl;
  }

  public static void handle(RequestMessage message, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer player = ctx.get().getSender();
      TileMain root = null;
      ContainerNetwork ctr = null;
      if (player.containerMenu instanceof ContainerNetwork) {
        ctr = (ContainerNetwork) player.containerMenu;
        root = ctr.getTileMain();
      }
      else {
        StorageNetworkMod.log("Bad container");
      }
      if (root == null) {
        //maybe the table broke after doing this, rare case
        StorageNetworkMod.log("Request message cancelled, null tile");
        return;
      }
      final int THRESH = 256;
      final boolean isRefresh = message.stack.isEmpty()
                                && message.mouseButton == 0
                                && !message.shift && !message.ctrl;

      if (isRefresh) {
        final String q = message.query == null ? "" : message.query.trim();
        final List<ItemStack> send = q.isEmpty()
            ? root.getNetwork().getSortedStacksUpTo(THRESH)
            : root.getNetwork().getSortedStacksFiltered(q, 4096);
        PacketRegistry.INSTANCE.sendTo(new StackRefreshClientMessage(send, new ArrayList<>()),
            player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        player.containerMenu.broadcastChanges();
        return;
      }
      int in = root.getNetwork().getAmount(new ItemStackMatcher(message.stack, false, true));
      ItemStack stack;
      boolean isLeftClick = message.mouseButton == UtilTileEntity.MOUSE_BTN_LEFT;
      boolean isRightClick = message.mouseButton == UtilTileEntity.MOUSE_BTN_RIGHT;
      int sizeRequested = 0;
      if (message.ctrl) {
        sizeRequested = 1;
      }
      else if (isLeftClick) {
        sizeRequested = message.stack.getMaxStackSize();
      }
      else if (isRightClick) {
        sizeRequested = Math.min(message.stack.getMaxStackSize() / 2, in / 2);
      }
      sizeRequested = Math.max(sizeRequested, 1);
      boolean ore = false;
      boolean nbt = true;
      //try NBT first
      stack = root.request(
          new ItemStackMatcher(message.stack, ore, nbt),
          sizeRequested, false);
      if (stack.isEmpty()) {
        //try again with NBT as false, ONLY if true didnt work
        nbt = false;
        stack = root.request(
            new ItemStackMatcher(message.stack, ore, nbt),
            sizeRequested, false);
      }
      if (!stack.isEmpty()) {
        if (message.shift) {
          ItemHandlerHelper.giveItemToPlayer(player, stack);
        }
        else {
          //when player TAKES an item, go here
          player.containerMenu.setCarried(stack);
          PacketRegistry.INSTANCE.sendTo(new StackResponseClientMessage(stack),
              player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        }
      }
      // refresh using the SAME query (keeps filtered view stable)
      {
        final String q = message.query == null ? "" : message.query.trim();
        final List<ItemStack> list = q.isEmpty()
            ? root.getNetwork().getSortedStacksUpTo(THRESH)
            : root.getNetwork().getSortedStacksFiltered(q, 4096);
        PacketRegistry.INSTANCE.sendTo(
            new StackRefreshClientMessage(list, new ArrayList<>()),
            player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        player.containerMenu.broadcastChanges();
      }
    });
    ctx.get().setPacketHandled(true);
  }

  public static RequestMessage decode(FriendlyByteBuf buf) {
    RequestMessage msg = new RequestMessage();
    msg.mouseButton = buf.readInt();
    msg.stack = ItemStack.of(buf.readNbt());
    msg.shift = buf.readBoolean();
    msg.ctrl = buf.readBoolean();
    msg.query = buf.readUtf(32767);
    return msg;
  }

  public static void encode(RequestMessage msg, FriendlyByteBuf buf) {
    buf.writeInt(msg.mouseButton);
    buf.writeNbt(msg.stack.serializeNBT());
    buf.writeBoolean(msg.shift);
    buf.writeBoolean(msg.ctrl);
    buf.writeUtf(msg.query == null ? "" : msg.query);
  }
 
  public RequestMessage withQuery(String q) {
    this.query = (q == null) ? "" : q;
    return this;
  }
}
