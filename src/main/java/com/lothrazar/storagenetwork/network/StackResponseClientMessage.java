package com.lothrazar.storagenetwork.network;

import com.lothrazar.storagenetwork.StorageNetworkMod;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class StackResponseClientMessage implements CustomPacketPayload {

  public static final CustomPacketPayload.Type<StackResponseClientMessage> TYPE =
      new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(StorageNetworkMod.MODID, "stack_response_client"));

  public static final StreamCodec<RegistryFriendlyByteBuf, StackResponseClientMessage> STREAM_CODEC = StreamCodec.of(
      StackResponseClientMessage::write,
      StackResponseClientMessage::read
  );

  private final ItemStack stack;

  public StackResponseClientMessage(ItemStack stack) {
    this.stack = stack;
  }

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  private static void write(RegistryFriendlyByteBuf buf, StackResponseClientMessage msg) {
    ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, msg.stack);
  }

  private static StackResponseClientMessage read(RegistryFriendlyByteBuf buf) {
    return new StackResponseClientMessage(ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
  }

  public static void handle(StackResponseClientMessage message, IPayloadContext ctx) {
    ctx.enqueueWork(() -> {
      Minecraft.getInstance().player.containerMenu.setCarried(message.stack);
    });
  }
}
