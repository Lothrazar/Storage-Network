package mrriegel.storagenetwork.network;

import io.netty.buffer.ByteBuf;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.api.network.INetworkMaster;
import mrriegel.storagenetwork.block.cable.io.ContainerCableIO;
import mrriegel.storagenetwork.block.cable.link.ContainerCableLink;
import mrriegel.storagenetwork.block.cable.processing.ContainerCableProcessing;
import mrriegel.storagenetwork.block.cable.processing.ProcessRequestModel;
import mrriegel.storagenetwork.block.cable.processing.TileCableProcess;
import mrriegel.storagenetwork.registry.PacketRegistry;
import mrriegel.storagenetwork.util.UtilTileEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class CableDataMessage implements IMessage, IMessageHandler<CableDataMessage, IMessage> {

  // TODO: This message handling should be split up into multiple messages
  public enum CableMessageType {
    PRIORITY_DOWN, PRIORITY_UP, P_ONOFF, TOGGLE_WHITELIST, TOGGLE_MODE, IMPORT_FILTER, TOGGLE_WAY, P_FACE_TOP, P_FACE_BOTTOM, TOGGLE_P_RESTARTTRIGGER, P_CTRL_MORE, P_CTRL_LESS;
  }

  private int id;
  private int value = 0;

  public CableDataMessage() {}

  public CableDataMessage(int id) {
    this.id = id;
  }

  public CableDataMessage(int id, int value) {
    this(id);
    this.value = value;
  }

  @Override
  public IMessage onMessage(final CableDataMessage message, final MessageContext ctx) {
    EntityPlayerMP player = ctx.getServerHandler().player;
    IThreadListener mainThread = (WorldServer) player.world;
    mainThread.addScheduledTask(new Runnable() {

      @Override
      public void run() {
        CableMessageType type = CableMessageType.values()[message.id];
        if (player.openContainer instanceof ContainerCableIO) {
          updateCableIO(message, player, type);
        }
        if (player.openContainer instanceof ContainerCableLink) {
          updateCableLink(message, player, type);
        }
        if (player.openContainer instanceof ContainerCableProcessing) {
          updateProcessing(message, player, type);
        }
      }

      private void updateProcessing(final CableDataMessage message, EntityPlayerMP player, CableMessageType type) {
        ContainerCableProcessing con = (ContainerCableProcessing) player.openContainer;
        if (!(con.tile instanceof TileCableProcess)) {
          return;
        }
        TileCableProcess tileCable = (TileCableProcess) con.tile;
        switch (type) {
          case TOGGLE_P_RESTARTTRIGGER:
            //stop listening for result, export recipe into block
            tileCable.getRequest().setStatus(ProcessRequestModel.ProcessStatus.EXPORTING);
          break;
          case P_FACE_BOTTOM:
            tileCable.processingBottom = EnumFacing.values()[message.value];
          break;
          case P_FACE_TOP:
            tileCable.processingTop = EnumFacing.values()[message.value];
          //                StorageNetwork.log(tileCable.processingTop.name() + " server is ?" + message.value);
          break;
          default:
          break;
        }
        tileCable.markDirty();
        UtilTileEntity.updateTile(tileCable.getWorld(), tileCable.getPos());
      }

      private void updateCableLink(final CableDataMessage message, EntityPlayerMP player, CableMessageType type) {
        ContainerCableLink con = (ContainerCableLink) player.openContainer;
        if (con == null || con.link == null) {
          return;
        }
        INetworkMaster master = StorageNetwork.helpers.getTileMasterForConnectable(con.link.connectable);
        switch (type) {
          case TOGGLE_WAY:
            con.link.filterDirection = con.link.filterDirection.next();
          break;
          case TOGGLE_WHITELIST:
            con.link.filters.isWhitelist = !con.link.filters.isWhitelist;
          break;
          case PRIORITY_UP:
            con.link.priority = message.value;
            if (master != null) {
              master.clearCache();
            }
          break;
          case PRIORITY_DOWN:
            con.link.priority = message.value;
            if (master != null) {
              master.clearCache();
            }
          break;
          case IMPORT_FILTER:
            con.link.importFilterStacks();
            PacketRegistry.INSTANCE.sendTo(new RefreshFilterClientMessage(con.link.filters.getStacks()), player);
            con.tile.markDirty();
          break;
          default:
          break;
        }
      }

      private void updateCableIO(final CableDataMessage message, EntityPlayerMP player, CableMessageType type) {
        ContainerCableIO con = (ContainerCableIO) player.openContainer;
        if (con == null || con.cap == null) {
          return;
        }
        INetworkMaster master = StorageNetwork.helpers.getTileMasterForConnectable(con.cap.connectable);
        switch (type) {
          case TOGGLE_MODE:
            con.cap.operationMustBeSmaller = !con.cap.operationMustBeSmaller;
          break;
          case TOGGLE_WHITELIST:
            con.cap.filters.isWhitelist = !con.cap.filters.isWhitelist;
          break;
          case PRIORITY_UP:
            con.cap.priority = message.value;
            if (master != null) {
              master.clearCache();
            }
          break;
          case PRIORITY_DOWN:
            con.cap.priority = message.value;
            if (master != null) {
              master.clearCache();
            }
          break;
          case IMPORT_FILTER:
            con.cap.importFilterStacks();
          break;
          default:
          break;
        }
        PacketRegistry.INSTANCE.sendTo(new RefreshFilterClientMessage(con.cap.filters.getStacks()), player);
        con.tile.markDirty();
      }
    });
    return null;
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    this.id = buf.readInt();
    value = buf.readInt();
  }

  @Override
  public void toBytes(ByteBuf buf) {
    buf.writeInt(this.id);
    buf.writeInt(value);
  }
}
