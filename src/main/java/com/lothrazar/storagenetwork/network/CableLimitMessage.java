package com.lothrazar.storagenetwork.network;

import com.lothrazar.storagenetwork.StorageNetworkMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CableLimitMessage implements CustomPacketPayload {

  public static final CustomPacketPayload.Type<CableLimitMessage> TYPE =
      new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(StorageNetworkMod.MODID, "cable_limit"));

  public static final StreamCodec<RegistryFriendlyByteBuf, CableLimitMessage> STREAM_CODEC = StreamCodec.of(
      CableLimitMessage::write,
      CableLimitMessage::read
  );

  private final int limit;
  private final ItemStack stack;

  public CableLimitMessage(int limit, ItemStack stack) {
    this.limit = limit;
    this.stack = stack;
  }

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  private static void write(RegistryFriendlyByteBuf buf, CableLimitMessage msg) {
    buf.writeInt(msg.limit);
    ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, msg.stack);
  }

  private static CableLimitMessage read(RegistryFriendlyByteBuf buf) {
    int limit = buf.readInt();
    ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
    return new CableLimitMessage(limit, stack);
  }

  public static void handle(CableLimitMessage message, IPayloadContext ctx) {
    ctx.enqueueWork(() -> {
      // handler body intentionally left empty (see original comments)
    });
  }
}
