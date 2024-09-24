package com.lothrazar.storagenetwork.network;

import java.util.function.Supplier;
import com.lothrazar.storagenetwork.block.cable.TileCable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class CableFacadeMessage {

  //sync sort data TO client gui FROM server
  private BlockPos pos;
  private boolean erase = false;
  private CompoundTag blockStateTag = new CompoundTag();

  private CableFacadeMessage() {}

  public CableFacadeMessage(BlockPos pos, CompoundTag state) {
    this.pos = pos;
    this.blockStateTag = state;
    this.erase = false;
  }

  public CableFacadeMessage(BlockPos pos, boolean eraseIn) {
    this.pos = pos;
    this.erase = eraseIn;
    blockStateTag = new CompoundTag();
  }

  public static void handle(CableFacadeMessage message, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer player = ctx.get().getSender();
      ServerLevel serverWorld = (ServerLevel) player.level();
      TileCable tile = TileCable.getTileCable(serverWorld, message.pos);
      if (tile != null) {
        if (message.erase) {
          //   StorageNetworkMod.log("Network Packet facade  SAVE NULL EMPTY ERASE " + message.blockStateTag);
          tile.setFacadeState(null);
        }
        else {
          //  StorageNetworkMod.log("Network Packet facade  SAVE " + message.blockStateTag);
          tile.setFacadeState(message.blockStateTag);
        }
        serverWorld.markAndNotifyBlock(message.pos, serverWorld.getChunkAt(message.pos),
            tile.getBlockState(), tile.getBlockState(), 3, 1);
      }
    });
    ctx.get().setPacketHandled(true);
  }

  public static CableFacadeMessage decode(FriendlyByteBuf buf) {
    CableFacadeMessage message = new CableFacadeMessage();
    message.erase = buf.readBoolean();
    message.pos = buf.readBlockPos();
    message.blockStateTag = buf.readNbt();
    return message;
  }

  public static void encode(CableFacadeMessage msg, FriendlyByteBuf buf) {
    buf.writeBoolean(msg.erase);
    buf.writeBlockPos(msg.pos);
    buf.writeNbt(msg.blockStateTag);
  }
}
