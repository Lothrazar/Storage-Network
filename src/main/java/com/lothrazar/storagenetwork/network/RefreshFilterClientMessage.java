package com.lothrazar.storagenetwork.network;

import java.util.ArrayList;
import java.util.List;
import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.block.cable.export.ScreenCableExportFilter;
import com.lothrazar.storagenetwork.block.cable.inputfilter.ScreenCableImportFilter;
import com.lothrazar.storagenetwork.block.cable.linkfilter.ScreenCableFilter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@SuppressWarnings("resource")
public class RefreshFilterClientMessage implements CustomPacketPayload {

  public static final CustomPacketPayload.Type<RefreshFilterClientMessage> TYPE =
      new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(StorageNetworkMod.MODID, "refresh_filter_client"));

  public static final StreamCodec<RegistryFriendlyByteBuf, RefreshFilterClientMessage> STREAM_CODEC = StreamCodec.of(
      RefreshFilterClientMessage::write,
      RefreshFilterClientMessage::read
  );

  private final List<ItemStack> stacks;

  public RefreshFilterClientMessage(List<ItemStack> stacks) {
    this.stacks = stacks;
  }

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  private static void write(RegistryFriendlyByteBuf buf, RefreshFilterClientMessage msg) {
    buf.writeInt(msg.stacks.size());
    for (ItemStack stack : msg.stacks) {
      ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, stack);
      buf.writeInt(stack.getCount());
    }
  }

  private static RefreshFilterClientMessage read(RegistryFriendlyByteBuf buf) {
    int size = buf.readInt();
    List<ItemStack> stacks = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
      stack.setCount(buf.readInt());
      stacks.add(stack);
    }
    return new RefreshFilterClientMessage(stacks);
  }

  public static void handle(RefreshFilterClientMessage message, IPayloadContext ctx) {
    ctx.enqueueWork(() -> {
      if (Minecraft.getInstance().screen instanceof ScreenCableFilter) {
        ScreenCableFilter gui = (ScreenCableFilter) Minecraft.getInstance().screen;
        gui.setFilterItems(message.stacks);
      }
      if (Minecraft.getInstance().screen instanceof ScreenCableImportFilter) {
        ScreenCableImportFilter gui = (ScreenCableImportFilter) Minecraft.getInstance().screen;
        gui.setFilterItems(message.stacks);
      }
      if (Minecraft.getInstance().screen instanceof ScreenCableExportFilter) {
        ScreenCableExportFilter gui = (ScreenCableExportFilter) Minecraft.getInstance().screen;
        gui.setFilterItems(message.stacks);
      }
    });
  }
}
