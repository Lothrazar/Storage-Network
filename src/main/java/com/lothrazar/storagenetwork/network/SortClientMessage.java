package com.lothrazar.storagenetwork.network;

import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.api.EnumSortType;
import com.lothrazar.storagenetwork.api.ITileNetworkSync;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SortClientMessage implements CustomPacketPayload {

  public static final CustomPacketPayload.Type<SortClientMessage> TYPE =
      new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(StorageNetworkMod.MODID, "sort_client"));

  public static final StreamCodec<RegistryFriendlyByteBuf, SortClientMessage> STREAM_CODEC = StreamCodec.of(
      SortClientMessage::write,
      SortClientMessage::read
  );

  private final BlockPos pos;
  private final boolean direction;
  private final EnumSortType sort;

  public SortClientMessage(BlockPos pos, boolean direction, EnumSortType sort) {
    this.pos = pos != null ? pos : BlockPos.ZERO;
    this.direction = direction;
    this.sort = sort;
  }

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  private static void write(RegistryFriendlyByteBuf buf, SortClientMessage msg) {
    buf.writeBoolean(msg.direction);
    buf.writeInt(msg.sort.ordinal());
    buf.writeBlockPos(msg.pos);
  }

  private static SortClientMessage read(RegistryFriendlyByteBuf buf) {
    boolean direction = buf.readBoolean();
    EnumSortType sort = EnumSortType.values()[buf.readInt()];
    BlockPos pos = buf.readBlockPos();
    return new SortClientMessage(pos, direction, sort);
  }

  public static void handle(SortClientMessage message, IPayloadContext ctx) {
    ctx.enqueueWork(() -> {
      Minecraft mc = Minecraft.getInstance();
      BlockEntity tileEntity = mc.level.getBlockEntity(message.pos);
      if (tileEntity instanceof ITileNetworkSync) {
        ITileNetworkSync ts = (ITileNetworkSync) tileEntity;
        ts.setDownwards(message.direction);
        ts.setSort(message.sort);
        tileEntity.setChanged();
      }
    });
  }
}
