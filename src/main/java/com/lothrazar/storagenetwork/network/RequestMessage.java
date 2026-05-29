package com.lothrazar.storagenetwork.network;

import java.util.ArrayList;
import java.util.List;
import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.api.capabilities.DefaultItemStackMatcher;
import com.lothrazar.storagenetwork.gui.ContainerNetwork;
import com.lothrazar.storagenetwork.util.UtilTileEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class RequestMessage implements CustomPacketPayload {

  public static final CustomPacketPayload.Type<RequestMessage> TYPE =
      new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(StorageNetworkMod.MODID, "request"));

  public static final StreamCodec<RegistryFriendlyByteBuf, RequestMessage> STREAM_CODEC = StreamCodec.of(
      RequestMessage::write,
      RequestMessage::read
  );

  private final int mouseButton;
  private final ItemStack stack;
  private final boolean shift;
  private final boolean ctrl;

  public RequestMessage() {
    this(0, ItemStack.EMPTY, false, false);
  }

  public RequestMessage(int id, ItemStack stackIn, boolean shift, boolean ctrl) {
    this.mouseButton = id;
    ItemStack s = stackIn.copy();
    if (s.getCount() > 64) {
      s.setCount(64);
    }
    this.stack = s;
    this.shift = shift;
    this.ctrl = ctrl;
  }

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  @Override
  public String toString() {
    return "RequestMessage [mouseButton=" + mouseButton + ", shift=" + shift + ", ctrl=" + ctrl + ", stack=" + stack.toString() + "]";
  }

  private static void write(RegistryFriendlyByteBuf buf, RequestMessage msg) {
    buf.writeInt(msg.mouseButton);
    ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, msg.stack);
    buf.writeBoolean(msg.shift);
    buf.writeBoolean(msg.ctrl);
  }

  private static RequestMessage read(RegistryFriendlyByteBuf buf) {
    int mouseButton = buf.readInt();
    ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
    boolean shift = buf.readBoolean();
    boolean ctrl = buf.readBoolean();
    return new RequestMessage(mouseButton, stack, shift, ctrl);
  }

  public static void handle(RequestMessage message, IPayloadContext ctx) {
    ctx.enqueueWork(() -> {
      ServerPlayer player = (ServerPlayer) ctx.player();
      TileMain root = null;
      ContainerNetwork ctr = null;
      if (player.containerMenu instanceof ContainerNetwork) {
        ctr = (ContainerNetwork) player.containerMenu;
        root = ctr.getTileMain();
      }
      else {
      }
      if (root == null) {
        StorageNetworkMod.LOGGER.debug("Request message cancelled, null tile");
        return;
      }
      int in = root.getNetwork().getAmount(new DefaultItemStackMatcher(message.stack, false, true));
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
      stack = root.request(new DefaultItemStackMatcher(message.stack, false, true), sizeRequested, false);
      if (stack.isEmpty()) {
        stack = root.request(new DefaultItemStackMatcher(message.stack, false, false), sizeRequested, false);
      }
      if (!stack.isEmpty()) {
        if (message.shift) {
          ItemHandlerHelper.giveItemToPlayer(player, stack);
        }
        else {
          player.containerMenu.setCarried(stack);
          PacketDistributor.sendToPlayer(player, new StackResponseClientMessage(stack));
        }
      }
      List<ItemStack> list = root.getNetwork().getSortedStacks();
      PacketDistributor.sendToPlayer(player, new StackRefreshClientMessage(list, new ArrayList<>()));
      player.containerMenu.broadcastChanges();
    });
  }
}
