package com.lothrazar.storagenetwork.network;

import com.lothrazar.storagenetwork.item.ItemCollector;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import com.lothrazar.storagenetwork.util.UtilInventory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class KeybindCollectorToggleMessage {

  public KeybindCollectorToggleMessage() {
  }

  public static void handle(KeybindCollectorToggleMessage msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer player = ctx.get().getSender();
      if (player != null) {

        // added curios compatibility to toggle feature
        var searchResult = UtilInventory.getCurioRemote(player, SsnRegistry.Items.COLLECTOR_REMOTE.get());
        ItemStack remoteFound = searchResult.getRight();
        if (!remoteFound.isEmpty()) {
          SsnRegistry.Items.COLLECTOR_REMOTE.get().toggleEnabled(remoteFound, player);
        }
      }
    });
    ctx.get().setPacketHandled(true);
  }

  public void encode(FriendlyByteBuf friendlyByteBuf) {
  }

  public static KeybindCollectorToggleMessage decode(FriendlyByteBuf friendlyByteBuf) {
    KeybindCollectorToggleMessage message = new KeybindCollectorToggleMessage();
    return message;
  }
}