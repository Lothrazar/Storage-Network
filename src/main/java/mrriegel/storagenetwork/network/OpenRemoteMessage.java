package mrriegel.storagenetwork.network;

import io.netty.buffer.ByteBuf;
import mrriegel.storagenetwork.item.remote.ItemRemote;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class OpenRemoteMessage implements IMessage, IMessageHandler<OpenRemoteMessage, IMessage> {

  @Override
  public IMessage onMessage(final OpenRemoteMessage message, final MessageContext ctx) {
    EntityPlayerMP player = ctx.getServerHandler().player;
    IThreadListener mainThread = (WorldServer) player.world;
    mainThread.addScheduledTask(new Runnable() {

      @Override
      public void run() {
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
          ItemStack slot = player.inventory.getStackInSlot(i);
          if (slot.getItem() instanceof ItemRemote && slot.hasTagCompound()) {
            ItemRemote.tryOpenGui(player.world, player, slot, i);
            break;
          }
        }
      }
    });
    return null;
  }

  @Override
  public void fromBytes(ByteBuf buf) {}

  @Override
  public void toBytes(ByteBuf buf) {}
}
