package com.lothrazar.storagenetwork.network;

import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.block.cable.linkfilter.ContainerCableFilter;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.capability.CapabilityConnectableLink;
import com.lothrazar.storagenetwork.util.UtilTileEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CableDataMessage implements CustomPacketPayload {

  public static final CustomPacketPayload.Type<CableDataMessage> TYPE =
      new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(StorageNetworkMod.MODID, "cable_data"));

  public static final StreamCodec<RegistryFriendlyByteBuf, CableDataMessage> STREAM_CODEC = StreamCodec.of(
      CableDataMessage::write,
      CableDataMessage::read
  );

  public enum CableMessageType {
    SYNC_DATA, IMPORT_FILTER, SAVE_FITLER, REDSTONE;
  }

  private final boolean isAllowlist;
  private final int id;
  private final int value;
  private ItemStack stack;

  public CableDataMessage(int id) {
    this.id = id;
    this.value = 0;
    this.isAllowlist = false;
    this.stack = ItemStack.EMPTY;
  }

  public CableDataMessage(int id, int value, boolean is) {
    this.id = id;
    this.value = value;
    this.isAllowlist = is;
    this.stack = ItemStack.EMPTY;
  }

  public CableDataMessage(int id, int value, ItemStack mystack) {
    this.id = id;
    this.value = value;
    this.isAllowlist = false;
    this.stack = mystack;
  }

  @Override
  public String toString() {
    return "CableDataMessage{isAllowlist=" + isAllowlist + ", id=" + id + ", value=" + value + ", stack=" + stack + '}';
  }

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  private static void write(RegistryFriendlyByteBuf buf, CableDataMessage msg) {
    buf.writeInt(msg.id);
    buf.writeInt(msg.value);
    buf.writeBoolean(msg.isAllowlist);
    ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, msg.stack);
  }

  private static CableDataMessage read(RegistryFriendlyByteBuf buf) {
    CableDataMessage c = new CableDataMessage(buf.readInt(), buf.readInt(), buf.readBoolean());
    c.stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
    return c;
  }

  public static void handle(CableDataMessage message, IPayloadContext ctx) {
    ctx.enqueueWork(() -> {
      ServerPlayer player = (ServerPlayer) ctx.player();
      CapabilityConnectableLink link = null;
      ContainerCableFilter container = (ContainerCableFilter) player.containerMenu;
      if (container == null || container.cap == null) {
        return;
      }
      link = container.cap;
      TileMain root = UtilTileEntity.getTileMainForConnectable(link.connectable);
      CableMessageType type = CableMessageType.values()[message.id];
      switch (type) {
        case IMPORT_FILTER:
          link.getFilter().clear();
          int targetSlot = 0;
          for (ItemStack filterSuggestion : link.getStoredStacks(false)) {
            if (link.getFilter().exactStackAlreadyInList(filterSuggestion)) {
              continue;
            }
            try {
              link.getFilter().setStackInSlot(targetSlot, filterSuggestion.copy());
              targetSlot++;
              if (targetSlot >= link.getFilter().getSlots()) {
                continue;
              }
            }
            catch (Exception ex) {
              StorageNetworkMod.LOGGER.error("Exception saving filter slot ", message);
            }
          }
          PacketDistributor.sendToPlayer(player, new RefreshFilterClientMessage(link.getFilter().getStacks()));
        break;
        case SYNC_DATA:
          link.setPriority(link.getPriority() + message.value);
          link.getFilter().setIsAllowlist(message.isAllowlist);
          if (root != null) {
            root.clearCache();
          }
        break;
        case SAVE_FITLER:
          link.setFilter(message.value, message.stack.copy());
        break;
        case REDSTONE:
          if (link.connectable != null) {
            link.connectable.toggleNeedsRedstone();
          }
        break;
      }
      container.tile.setChanged();
      player.connection.send(container.tile.getUpdatePacket());
    });
  }
}
