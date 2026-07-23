package com.lothrazar.storagenetwork.network;

import java.util.ArrayList;
import java.util.List;
import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.api.gui.GuiNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class StackRefreshClientMessage implements CustomPacketPayload {

  public static final CustomPacketPayload.Type<StackRefreshClientMessage> TYPE =
      new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(StorageNetworkMod.MODID, "stack_refresh_client"));

  public static final StreamCodec<RegistryFriendlyByteBuf, StackRefreshClientMessage> STREAM_CODEC = StreamCodec.of(
      StackRefreshClientMessage::write,
      StackRefreshClientMessage::read
  );

  private final List<ItemStack> stacks;
  private final List<ItemStack> craftableStacks;

  public StackRefreshClientMessage(List<ItemStack> stacks, List<ItemStack> craftableStacks) {
    this.stacks = stacks;
    this.craftableStacks = craftableStacks;
  }

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  private static void write(RegistryFriendlyByteBuf buf, StackRefreshClientMessage msg) {
    buf.writeInt(msg.stacks.size());
    buf.writeInt(msg.craftableStacks.size());
    for (ItemStack stack : msg.stacks) {
      ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, stack);
      buf.writeInt(stack.getCount());
    }
    for (ItemStack stack : msg.craftableStacks) {
      ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, stack);
      buf.writeInt(stack.getCount());
    }
  }

  private static StackRefreshClientMessage read(RegistryFriendlyByteBuf buf) {
    int size = buf.readInt();
    int csize = buf.readInt();
    List<ItemStack> stacks = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
      stack.setCount(buf.readInt());
      stacks.add(stack);
    }
    List<ItemStack> craftableStacks = new ArrayList<>();
    for (int i = 0; i < csize; i++) {
      ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
      stack.setCount(buf.readInt());
      craftableStacks.add(stack);
    }
    return new StackRefreshClientMessage(stacks, craftableStacks);
  }

  public static void handle(StackRefreshClientMessage message, IPayloadContext ctx) {
    ctx.enqueueWork(() -> {
      Minecraft mc = Minecraft.getInstance();
      if (mc.screen instanceof GuiNetwork) {
        GuiNetwork gui = (GuiNetwork) mc.screen;
        gui.setStacks(message.stacks);
      }
    });
  }
}
