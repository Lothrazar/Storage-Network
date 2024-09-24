package mrriegel.storagenetwork.network;

import java.util.List;
import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import mrriegel.storagenetwork.block.cable.GuiCableBase;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Refresh the current screen with large data set of stacks.
 *
 * Used by Containers displaying network inventory as well as most other packets that perform small actions
 *
 */
public class RefreshFilterClientMessage implements IMessage, IMessageHandler<RefreshFilterClientMessage, IMessage> {

  private int size;
  private List<ItemStack> stacks;

  public RefreshFilterClientMessage() {}

  public RefreshFilterClientMessage(List<ItemStack> stacks) {
    super();
    this.stacks = stacks;
    this.size = stacks.size();
  }

  @Override
  public IMessage onMessage(final RefreshFilterClientMessage message, final MessageContext ctx) {
    IThreadListener mainThread = Minecraft.getMinecraft();
    mainThread.addScheduledTask(new Runnable() {

      @Override
      public void run() {
        if (Minecraft.getMinecraft().currentScreen instanceof GuiCableBase) {
          GuiCableBase gui = (GuiCableBase) Minecraft.getMinecraft().currentScreen;
          gui.setFilterItems(message.stacks);
        }
      }
    });
    return null;
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    this.size = buf.readInt();
    stacks = Lists.newArrayList();
    for (int i = 0; i < size; i++) {
      ItemStack stack = new ItemStack(ByteBufUtils.readTag(buf));
      stacks.add(stack);
    }
  }

  @Override
  public void toBytes(ByteBuf buf) {
    buf.writeInt(this.size);
    for (ItemStack stack : stacks) {
      ByteBufUtils.writeTag(buf, stack.serializeNBT());
    }
  }
}
