package com.lothrazar.storagenetwork.network;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import com.lothrazar.storagenetwork.block.cable.export.ScreenCableExportFilter;
import com.lothrazar.storagenetwork.block.cable.inputfilter.ScreenCableImportFilter;
import com.lothrazar.storagenetwork.block.cable.linkfilter.ScreenCableFilter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

/**
 * Refresh the current screen with large data set of stacks.
 * <p>
 * Used by Containers displaying network inventory as well as most other packets that perform small actions
 */
@SuppressWarnings("resource")
public class RefreshFilterClientMessage {

  private int size;
  private List<ItemStack> stacks;

  private RefreshFilterClientMessage() {}

  public RefreshFilterClientMessage(List<ItemStack> stacks) {
    super();
    this.stacks = stacks;
    size = stacks.size();
  }

  public static void handle(RefreshFilterClientMessage message, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      //TODO: optimize with base class or interface
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
    ctx.get().setPacketHandled(true);
  }

  public static RefreshFilterClientMessage decode(FriendlyByteBuf buf) {
    RefreshFilterClientMessage message = new RefreshFilterClientMessage();
    message.size = buf.readInt();
    message.stacks = new ArrayList<>();
    for (int i = 0; i < message.size; i++) {
      ItemStack stack = ItemStack.of(buf.readNbt());
      stack.setCount(buf.readInt());
      message.stacks.add(stack);
    }
    return message;
  }

  public static void encode(RefreshFilterClientMessage msg, FriendlyByteBuf buf) {
    buf.writeInt(msg.size);
    for (ItemStack stack : msg.stacks) {
      buf.writeNbt(stack.serializeNBT());
      buf.writeInt(stack.getCount());
    }
  }
}
