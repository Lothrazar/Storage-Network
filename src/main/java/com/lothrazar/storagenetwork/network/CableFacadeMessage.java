package com.lothrazar.storagenetwork.network;

import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.block.cable.TileCable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CableFacadeMessage implements CustomPacketPayload {

  public static final CustomPacketPayload.Type<CableFacadeMessage> TYPE =
      new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(StorageNetworkMod.MODID, "cable_facade"));

  public static final StreamCodec<RegistryFriendlyByteBuf, CableFacadeMessage> STREAM_CODEC = StreamCodec.of(
      CableFacadeMessage::write,
      CableFacadeMessage::read
  );

  private final BlockPos pos;
  private final boolean erase;
  private final CompoundTag blockStateTag;

  public CableFacadeMessage(BlockPos pos, CompoundTag state) {
    this.pos = pos;
    this.blockStateTag = state;
    this.erase = false;
  }

  public CableFacadeMessage(BlockPos pos, boolean eraseIn) {
    this.pos = pos;
    this.erase = eraseIn;
    this.blockStateTag = new CompoundTag();
  }

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  private static void write(RegistryFriendlyByteBuf buf, CableFacadeMessage msg) {
    buf.writeBoolean(msg.erase);
    buf.writeBlockPos(msg.pos);
    buf.writeNbt(msg.blockStateTag);
  }

  private static CableFacadeMessage read(RegistryFriendlyByteBuf buf) {
    boolean erase = buf.readBoolean();
    BlockPos pos = buf.readBlockPos();
    CompoundTag tag = buf.readNbt();
    if (erase) {
      return new CableFacadeMessage(pos, true);
    }
    return new CableFacadeMessage(pos, tag != null ? tag : new CompoundTag());
  }

  public static void handle(CableFacadeMessage message, IPayloadContext ctx) {
    ctx.enqueueWork(() -> {
      ServerPlayer player = (ServerPlayer) ctx.player();
      ServerLevel serverWorld = (ServerLevel) player.level();
      TileCable tile = TileCable.getTileCable(serverWorld, message.pos);
      if (tile != null) {
        if (message.erase) {
          tile.setFacadeState(null);
        }
        else {
          tile.setFacadeState(message.blockStateTag);
        }
        serverWorld.markAndNotifyBlock(message.pos, serverWorld.getChunkAt(message.pos),
            tile.getBlockState(), tile.getBlockState(), 3, 1);
      }
    });
  }
}
