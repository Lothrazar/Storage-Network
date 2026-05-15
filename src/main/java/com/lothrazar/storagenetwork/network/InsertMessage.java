package com.lothrazar.storagenetwork.network;

import java.util.ArrayList;
import java.util.List;
import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.gui.ContainerNetwork;
import com.lothrazar.storagenetwork.util.UtilTileEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class InsertMessage implements CustomPacketPayload {

  public static final CustomPacketPayload.Type<InsertMessage> TYPE =
      new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(StorageNetworkMod.MODID, "insert"));

  public static final StreamCodec<RegistryFriendlyByteBuf, InsertMessage> STREAM_CODEC = StreamCodec.of(
      InsertMessage::write,
      InsertMessage::read
  );

  private final int dim;
  private final int mouseButton;

  public InsertMessage(int dim, int buttonID) {
    this.dim = dim;
    this.mouseButton = buttonID;
  }

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  private static void write(RegistryFriendlyByteBuf buf, InsertMessage msg) {
    buf.writeInt(msg.dim);
    buf.writeInt(msg.mouseButton);
  }

  private static InsertMessage read(RegistryFriendlyByteBuf buf) {
    return new InsertMessage(buf.readInt(), buf.readInt());
  }

  public static void handle(InsertMessage message, IPayloadContext ctx) {
    ctx.enqueueWork(() -> {
      ServerPlayer player = (ServerPlayer) ctx.player();
      TileMain root = null;
      if (player.containerMenu instanceof ContainerNetwork) {
        root = ((ContainerNetwork) player.containerMenu).getTileMain();
      }
      int rest;
      ItemStack send = ItemStack.EMPTY;
      ItemStack stack = player.containerMenu.getCarried();
      if (message.mouseButton == UtilTileEntity.MOUSE_BTN_LEFT) {
        rest = root.insertStack(stack, false);
        if (rest != 0) {
          send = stack.copyWithCount(rest);
        }
      }
      else if (message.mouseButton == UtilTileEntity.MOUSE_BTN_RIGHT) {
        ItemStack stack1 = stack.copy();
        stack1.setCount(1);
        stack.shrink(1);
        rest = root.insertStack(stack1, false) + stack.getCount();
        if (rest != 0) {
          send = stack.copyWithCount(rest);
        }
      }
      player.containerMenu.setCarried(send);
      PacketDistributor.sendToPlayer(player, new StackResponseClientMessage(send));
      List<ItemStack> list = root.getNetwork().getStacks();
      PacketDistributor.sendToPlayer(player, new StackRefreshClientMessage(list, new ArrayList<>()));
      player.containerMenu.broadcastChanges();
    });
  }
}
