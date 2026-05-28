package com.lothrazar.storagenetwork.network;

import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.api.EnumSortType;
import com.lothrazar.storagenetwork.api.ITileNetworkSync;
import com.lothrazar.storagenetwork.item.remote.ContainerNetworkCraftingRemote;
import com.lothrazar.storagenetwork.item.remote.ContainerNetworkRemote;
import com.lothrazar.storagenetwork.item.remote.ItemRemote;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SettingsSyncMessage implements CustomPacketPayload {

  public static final CustomPacketPayload.Type<SettingsSyncMessage> TYPE =
      new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(StorageNetworkMod.MODID, "settings_sync"));

  public static final StreamCodec<RegistryFriendlyByteBuf, SettingsSyncMessage> STREAM_CODEC = StreamCodec.of(
      SettingsSyncMessage::write,
      SettingsSyncMessage::read
  );

  private final BlockPos pos;
  private final boolean direction;
  private final EnumSortType sort;
  private final boolean targetTileEntity;
  private final boolean jeiSync;
  private final boolean autoFocus;
  private final boolean fullStackCraft;

  public SettingsSyncMessage(BlockPos pos, boolean direction, EnumSortType sort, boolean jeiSync, boolean autoFocus, boolean fullStackCraft) {
    this.pos = pos;
    this.direction = direction;
    this.sort = sort;
    this.jeiSync = jeiSync;
    this.autoFocus = autoFocus;
    this.fullStackCraft = fullStackCraft;
    this.targetTileEntity = (pos != null);
  }

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  private static void write(RegistryFriendlyByteBuf buf, SettingsSyncMessage msg) {
    buf.writeBoolean(msg.direction);
    buf.writeInt(msg.sort.ordinal());
    if (msg.pos != null) {
      buf.writeBoolean(true);
      buf.writeBlockPos(msg.pos);
    }
    else {
      buf.writeBoolean(false);
      buf.writeBlockPos(BlockPos.ZERO);
    }
    buf.writeBoolean(msg.jeiSync);
    buf.writeBoolean(msg.autoFocus);
    buf.writeBoolean(msg.fullStackCraft);
  }

  private static SettingsSyncMessage read(RegistryFriendlyByteBuf buf) {
    boolean direction = buf.readBoolean();
    EnumSortType sort = EnumSortType.values()[buf.readInt()];
    boolean hasPos = buf.readBoolean();
    BlockPos pos = buf.readBlockPos();
    boolean jeiSync = buf.readBoolean();
    boolean autoFocus = buf.readBoolean();
    boolean fullStackCraft = buf.readBoolean();
    return new SettingsSyncMessage(hasPos ? pos : null, direction, sort, jeiSync, autoFocus, fullStackCraft);
  }

  public static void handle(SettingsSyncMessage message, IPayloadContext ctx) {
    ctx.enqueueWork(() -> {
      ServerPlayer player = (ServerPlayer) ctx.player();
      if (message.targetTileEntity) {
        BlockEntity tileEntity = player.level().getBlockEntity(message.pos);
        if (tileEntity instanceof ITileNetworkSync) {
          ITileNetworkSync tile = (ITileNetworkSync) tileEntity;
          tile.setSort(message.sort);
          tile.setDownwards(message.direction);
          tile.setJeiSearchSynced(message.jeiSync);
          tile.setAutoFocus(message.autoFocus);
          tile.setFullStackCraft(message.fullStackCraft);
          tileEntity.setChanged();
        }
      }
      else if (player.containerMenu instanceof ContainerNetworkCraftingRemote remoteContainer) {
        ItemStack stackPlayerHeld = remoteContainer.getRemote();
        if (stackPlayerHeld.getItem() instanceof ItemRemote) {
          ItemRemote.setSort(stackPlayerHeld, message.sort);
          ItemRemote.setDownwards(stackPlayerHeld, message.direction);
          ItemRemote.setJeiSearchSynced(stackPlayerHeld, message.jeiSync);
          ItemRemote.setAutoFocus(stackPlayerHeld, message.autoFocus);
          ItemRemote.setFullStackCraft(stackPlayerHeld, message.fullStackCraft);
        }
      }
      else if (player.containerMenu instanceof ContainerNetworkRemote rcc) {
        ItemStack stackPlayerHeld = rcc.getRemote();
        if (stackPlayerHeld.getItem() instanceof ItemRemote) {
          ItemRemote.setSort(stackPlayerHeld, message.sort);
          ItemRemote.setDownwards(stackPlayerHeld, message.direction);
          ItemRemote.setJeiSearchSynced(stackPlayerHeld, message.jeiSync);
          ItemRemote.setAutoFocus(stackPlayerHeld, message.autoFocus);
          ItemRemote.setFullStackCraft(stackPlayerHeld, message.fullStackCraft);
        }
      }
    });
  }
}
