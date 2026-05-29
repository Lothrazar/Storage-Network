package com.lothrazar.storagenetwork.network;

import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.item.remote.RemoteHelpers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class KeybindCurioMessage implements CustomPacketPayload {

  public static final CustomPacketPayload.Type<KeybindCurioMessage> TYPE =
      new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(StorageNetworkMod.MODID, "keybind_curio"));

  public static final StreamCodec<FriendlyByteBuf, KeybindCurioMessage> STREAM_CODEC =
      StreamCodec.unit(new KeybindCurioMessage());

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  public static void handle(KeybindCurioMessage message, IPayloadContext ctx) {
    ctx.enqueueWork(() -> {
      ServerPlayer player = (ServerPlayer) ctx.player();
      ServerLevel serverWorld = (ServerLevel) player.level();
      RemoteHelpers.searchAndOpen(player, serverWorld);
    });
  }
}
