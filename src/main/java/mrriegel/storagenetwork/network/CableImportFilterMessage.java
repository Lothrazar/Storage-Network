package mrriegel.storagenetwork.network;

import io.netty.buffer.ByteBuf;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.block.cable.processing.ContainerCableProcessing;
import mrriegel.storagenetwork.block.cable.processing.TileCableProcess;
import mrriegel.storagenetwork.registry.PacketRegistry;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class CableImportFilterMessage implements IMessage, IMessageHandler<CableImportFilterMessage, IMessage> {
  //  private int limit;
  //  private ItemStack stack;

  public CableImportFilterMessage() {}
  //  public CableImportFilterMessage(int limit, ItemStack stack) {
  //    super();
  //    this.limit = limit;
  //    this.stack = stack;
  //  }

  @Override
  public IMessage onMessage(final CableImportFilterMessage message, final MessageContext ctx) {
    EntityPlayerMP player = ctx.getServerHandler().player;
    IThreadListener mainThread = (WorldServer) player.world;
    mainThread.addScheduledTask(() -> {
      if (player.openContainer instanceof ContainerCableProcessing) {
        ContainerCableProcessing con = (ContainerCableProcessing) player.openContainer;
        StorageNetwork.log("cab proc import filter clicked " + con.tile);
        if (con.tile instanceof TileCableProcess) {
          TileCableProcess proc = (TileCableProcess) con.tile;
          if (proc.getDirection() == null) {
            return; // it has no attached machine. button effectively disabled 
          }
          proc.importFilters();
          //sync back to client 
          PacketRegistry.INSTANCE.sendTo(new RefreshFilterClientMessage(proc.filters.getStacks()),
              player);
          //
          con.tile.markDirty();
        }
      }
    });
    return null;
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    //    this.limit = buf.readInt();
    //    this.stack = ByteBufUtils.readItemStack(buf);
  }

  @Override
  public void toBytes(ByteBuf buf) {
    //    buf.writeInt(this.limit);
    //    ByteBufUtils.writeItemStack(buf, this.stack);
  }
}
