package com.lothrazar.storagenetwork.network;

import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import com.lothrazar.storagenetwork.api.util.UtilInventory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class KeybindCollectorToggleMessage implements CustomPacketPayload {

  public static final KeybindCollectorToggleMessage INSTANCE = new KeybindCollectorToggleMessage();

  public static final CustomPacketPayload.Type<KeybindCollectorToggleMessage> TYPE =
      new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(StorageNetworkMod.MODID, "keybind_collector_toggle"));

  public static final StreamCodec<FriendlyByteBuf, KeybindCollectorToggleMessage> STREAM_CODEC =
      StreamCodec.unit(INSTANCE);

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  public static void handle(KeybindCollectorToggleMessage msg, IPayloadContext ctx) {
    ctx.enqueueWork(() -> {
      ServerPlayer player = (ServerPlayer) ctx.player();
      if (player != null) {
        var searchResult = UtilInventory.getCurioRemote(player, SsnRegistry.Items.COLLECTOR_REMOTE.get());
        ItemStack remoteFound = searchResult.getRight();
        if (!remoteFound.isEmpty()) {
          SsnRegistry.Items.COLLECTOR_REMOTE.get().toggleEnabled(remoteFound, player);
        }
      }
    });
  }
}
