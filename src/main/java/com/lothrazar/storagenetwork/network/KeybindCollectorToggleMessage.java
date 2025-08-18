package com.lothrazar.storagenetwork.network;

import com.lothrazar.storagenetwork.item.ItemCollector;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class KeybindCollectorToggleMessage {

    public KeybindCollectorToggleMessage() {}

    public static void handle(KeybindCollectorToggleMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ItemStack held = player.getMainHandItem();
                if (held.getItem() instanceof ItemCollector collector) {
                    collector.toggleEnabled(held, player);
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