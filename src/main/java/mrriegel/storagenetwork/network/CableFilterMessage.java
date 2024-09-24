package mrriegel.storagenetwork.network;

import io.netty.buffer.ByteBuf;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.block.cable.ContainerCable;
import mrriegel.storagenetwork.block.cable.io.ContainerCableIO;
import mrriegel.storagenetwork.block.cable.link.ContainerCableLink;
import mrriegel.storagenetwork.block.cable.processing.ContainerCableProcessing;
import mrriegel.storagenetwork.block.cable.processing.TileCableProcess;
import mrriegel.storagenetwork.registry.PacketRegistry;
import mrriegel.storagenetwork.util.UtilTileEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class CableFilterMessage implements IMessage, IMessageHandler<CableFilterMessage, IMessage> {

  private int index;
  private ItemStack stack;
  private boolean ore, meta, nbt;

  public CableFilterMessage() {}

  public CableFilterMessage(int index, ItemStack stack, boolean ore, boolean meta, boolean nbt) {
    this.index = index;
    this.stack = stack;
    this.ore = ore;
    this.meta = meta;
    this.nbt = nbt;
  }

  @Override
  public IMessage onMessage(final CableFilterMessage message, final MessageContext ctx) {
    EntityPlayerMP player = ctx.getServerHandler().player;
    IThreadListener mainThread = (WorldServer) player.world;
    mainThread.addScheduledTask(() -> {
      if (player.openContainer instanceof ContainerCable) {
        //        TileCable tileCable = ((ContainerCable) player.openContainer).tile;
        //        if (tileCable instanceof TileCableProcess) {
        //          TileCableProcess processCable = (TileCableProcess) tileCable;
        //          if (message.stack != null && message.index >= 0) {
        //            processCable.filterInput.setStackInSlot(message.index, message.stack);
        //          }
        //          processCable.filterInput.ores = message.ore;
        //          processCable.filterInput.meta = message.meta;
        //          processCable.filterInput.nbt = message.nbt;
        //          processCable.markDirty();
        //        }
      }
      if (player.openContainer instanceof ContainerCableLink) {
        ContainerCableLink con = (ContainerCableLink) player.openContainer;
        if (con == null || con.link == null) {
          return;
        }
        if (message.stack != null && message.index >= 0) {
          con.link.filters.setStackInSlot(message.index, message.stack);
        }
        con.link.filters.ores = message.ore;
        con.link.filters.meta = message.meta;
        con.link.filters.nbt = message.nbt;
        con.tile.markDirty();
      }
      if (player.openContainer instanceof ContainerCableIO) {
        ContainerCableIO con = (ContainerCableIO) player.openContainer;
        if (con == null || con.cap == null) {
          return;
        }
        if (message.stack != null && message.index >= 0) {
          con.cap.filters.setStackInSlot(message.index, message.stack);
        }
        con.cap.filters.ores = message.ore;
        con.cap.filters.meta = message.meta;
        con.cap.filters.nbt = message.nbt;
        con.tile.markDirty();
        UtilTileEntity.updateTile(con.tile.getWorld(), con.tile.getPos());
      }
      if (player.openContainer instanceof ContainerCableProcessing) {
        StorageNetwork.log("CableFilterMessage in processing   " + message.index);
        ContainerCableProcessing con = (ContainerCableProcessing) player.openContainer;
        if (!(con.tile instanceof TileCableProcess)) {
          return;
        }
        TileCableProcess tileCable = (TileCableProcess) con.tile;
        StorageNetwork.log("CableFilter Messageinput or output for filter?? " + message.index);
        if (message.stack != null && message.index >= 0) {
          tileCable.filters.setStackInSlot(message.index, message.stack);
        }
        tileCable.filters.ores = message.ore;
        tileCable.filters.meta = message.meta;
        tileCable.filters.nbt = message.nbt;
        //sync back to client
        StorageNetwork.log("sync bac kt oprocess cable ");
        PacketRegistry.INSTANCE.sendTo(new RefreshFilterClientMessage(tileCable.filters.getStacks()),
            player);
        tileCable.markDirty();
      }
    });
    return null;
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    this.index = buf.readInt();
    this.ore = buf.readBoolean();
    this.meta = buf.readBoolean();
    this.nbt = buf.readBoolean();
    this.stack = new ItemStack(ByteBufUtils.readTag(buf));
  }

  @Override
  public void toBytes(ByteBuf buf) {
    buf.writeInt(this.index);
    buf.writeBoolean(this.ore);
    buf.writeBoolean(this.meta);
    buf.writeBoolean(this.nbt);
    NBTTagCompound nbt = new NBTTagCompound();
    if (this.stack != null) {
      nbt = this.stack.serializeNBT();
    }
    ByteBufUtils.writeTag(buf, nbt);
  }
}
