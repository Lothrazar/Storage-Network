package com.lothrazar.storagenetwork.network;

import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.block.TileConnectable;
import com.lothrazar.storagenetwork.block.cable.export.ContainerCableExportFilter;
import com.lothrazar.storagenetwork.block.cable.inputfilter.ContainerCableImportFilter;
import com.lothrazar.storagenetwork.block.collection.ContainerCollectionFilter;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.api.capabilities.CapabilityConnectable;
import com.lothrazar.storagenetwork.capabilities.CapabilityConnectableAutoIO;
import com.lothrazar.storagenetwork.util.UtilTileEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CableIOMessage implements CustomPacketPayload {

  public static final CustomPacketPayload.Type<CableIOMessage> TYPE =
      new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(StorageNetworkMod.MODID, "cable_io"));

  public static final StreamCodec<RegistryFriendlyByteBuf, CableIOMessage> STREAM_CODEC = StreamCodec.of(
      CableIOMessage::write,
      CableIOMessage::read
  );

  public enum CableMessageType {
    SYNC_DATA, IMPORT_FILTER, SAVE_FITLER, REDSTONE, SYNC_OP, SYNC_OP_TEXT, SYNC_OP_STACK;
  }

  private final boolean isAllowlist;
  private final int id;
  private final int value;
  private ItemStack stack;

  public CableIOMessage(int id) {
    this.id = id;
    this.value = 0;
    this.isAllowlist = false;
    this.stack = ItemStack.EMPTY;
  }

  public CableIOMessage(int id, int value, boolean isall) {
    this.id = id;
    this.value = value;
    this.isAllowlist = isall;
    this.stack = ItemStack.EMPTY;
  }

  public CableIOMessage(int id, int value, ItemStack stackin) {
    this.id = id;
    this.value = value;
    this.isAllowlist = false;
    this.stack = stackin;
  }

  public CableIOMessage(int id, ItemStack s) {
    this.id = id;
    this.value = 0;
    this.isAllowlist = false;
    this.stack = s;
  }

  @Override
  public String toString() {
    return "CableDataMessage{isAllowlist=" + isAllowlist + ", id=" + id + ", value=" + value + ", stack=" + stack + '}';
  }

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  private static void write(RegistryFriendlyByteBuf buf, CableIOMessage msg) {
    buf.writeInt(msg.id);
    buf.writeInt(msg.value);
    buf.writeBoolean(msg.isAllowlist);
    ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, msg.stack);
  }

  private static CableIOMessage read(RegistryFriendlyByteBuf buf) {
    CableIOMessage c = new CableIOMessage(buf.readInt(), buf.readInt(), buf.readBoolean());
    c.stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
    return c;
  }

  public static void handle(CableIOMessage message, IPayloadContext ctx) {
    ctx.enqueueWork(() -> handleInternal(message, ctx));
  }

  private static void handleInternal(CableIOMessage message, IPayloadContext ctx) {
    ServerPlayer player = (ServerPlayer) ctx.player();
    CapabilityConnectableAutoIO link = null;
    TileConnectable tile = null;
    CapabilityConnectable connectable = null;
    if (player.containerMenu instanceof ContainerCableExportFilter) {
      ContainerCableExportFilter ctr = (ContainerCableExportFilter) player.containerMenu;
      link = ctr.cap;
      tile = ctr.tile;
    }
    if (player.containerMenu instanceof ContainerCableImportFilter) {
      ContainerCableImportFilter ctr = (ContainerCableImportFilter) player.containerMenu;
      link = ctr.cap;
      tile = ctr.tile;
    }
    if (player.containerMenu instanceof ContainerCollectionFilter) {
      ContainerCollectionFilter ctr = (ContainerCollectionFilter) player.containerMenu;
      connectable = ctr.cap;
      tile = ctr.tile;
    }
    TileMain root = null;
    if (link != null) {
      root = UtilTileEntity.getTileMainForConnectable(link.connectable);
    }
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
        if (link != null) {
          link.setFilter(message.value, message.stack.copy());
        }
        else if (connectable != null) {
          connectable.setFilter(message.value, message.stack.copy());
        }
      break;
      case REDSTONE:
        if (link != null) {
          link.toggleNeedsRedstone();
        }
        if (connectable != null) {
          connectable.toggleNeedsRedstone();
        }
      break;
      case SYNC_OP:
        link.operationType = message.value;
      break;
      case SYNC_OP_STACK:
        link.operationStack = message.stack;
      break;
      case SYNC_OP_TEXT:
        link.operationLimit = message.value;
      break;
      default:
      break;
    }
    tile.setChanged();
    player.connection.send(tile.getUpdatePacket());
  }
}
