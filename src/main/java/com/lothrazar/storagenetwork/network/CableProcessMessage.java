package com.lothrazar.storagenetwork.network;

import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.block.cable.processing.ContainerCableProcess;
import com.lothrazar.storagenetwork.block.cable.processing.ProcessRequestModel;
import com.lothrazar.storagenetwork.block.cable.processing.TileCableProcess;
import com.lothrazar.storagenetwork.block.cable.processing.CapabilityProcessingDefault;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CableProcessMessage implements CustomPacketPayload {

  public static final CustomPacketPayload.Type<CableProcessMessage> TYPE =
      new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(StorageNetworkMod.MODID, "cable_process"));

  public static final StreamCodec<RegistryFriendlyByteBuf, CableProcessMessage> STREAM_CODEC = StreamCodec.of(
      CableProcessMessage::write,
      CableProcessMessage::read
  );

  public enum ProcessMessageType {
    SAVE_FILTER_IN, SAVE_FILTER_OUT, SET_INPUT_FACE, SET_OUTPUT_FACE,
    SET_COUNT, SET_ALWAYS_ACTIVE, RESET_CYCLE, IMPORT_FILTERS, REDSTONE,
    SET_PRIORITY;
  }

  private final int id;
  private final int value;
  private ItemStack stack;

  public CableProcessMessage(int id, int value, ItemStack stack) {
    this.id = id;
    this.value = value;
    this.stack = stack;
  }

  public CableProcessMessage(int id) {
    this(id, 0, ItemStack.EMPTY);
  }

  public CableProcessMessage(int id, int value) {
    this(id, value, ItemStack.EMPTY);
  }

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  private static void write(RegistryFriendlyByteBuf buf, CableProcessMessage msg) {
    buf.writeInt(msg.id);
    buf.writeInt(msg.value);
    ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, msg.stack);
  }

  private static CableProcessMessage read(RegistryFriendlyByteBuf buf) {
    int id = buf.readInt();
    int value = buf.readInt();
    ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
    return new CableProcessMessage(id, value, stack);
  }

  public static void handle(CableProcessMessage message, IPayloadContext ctx) {
    ctx.enqueueWork(() -> handleInternal(message, ctx));
  }

  private static void handleInternal(CableProcessMessage message, IPayloadContext ctx) {
    ServerPlayer player = (ServerPlayer) ctx.player();
    if (!(player.containerMenu instanceof ContainerCableProcess ctr)) {
      return;
    }
    TileCableProcess tile = ctr.tile;
    if (tile == null) {
      return;
    }
    CapabilityProcessingDefault cap = ctr.cap;
    ProcessRequestModel model = tile.getProcessModel();
    ProcessMessageType type = ProcessMessageType.values()[message.id];
    switch (type) {
      case SAVE_FILTER_IN:
        cap.getFilters().setStackInSlot(message.value, message.stack.copy());
      break;
      case SAVE_FILTER_OUT:
        cap.getFiltersOut().setStackInSlot(message.value, message.stack.copy());
      break;
      case SET_INPUT_FACE:
        model.setInputFace(Direction.values()[message.value]);
      break;
      case SET_OUTPUT_FACE:
        model.setOutputFace(Direction.values()[message.value]);
      break;
      case SET_COUNT:
        model.setCount(message.value);
      break;
      case SET_ALWAYS_ACTIVE:
        model.setAlwaysActive(message.value != 0);
      break;
      case RESET_CYCLE:
        model.setStatus(ProcessRequestModel.ProcessStatus.EXPORTING);
      break;
      case IMPORT_FILTERS:
        tile.importFilters();
      break;
      case REDSTONE:
        cap.toggleNeedsRedstone();
      break;
      case SET_PRIORITY:
        cap.setPriority(cap.getPriority() + message.value);
      break;
      default:
      break;
    }
    tile.setChanged();
    player.connection.send(tile.getUpdatePacket());
  }
}
