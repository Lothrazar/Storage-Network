package mrriegel.storagenetwork.network;

import io.netty.buffer.ByteBuf;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.block.cable.BlockCable;
import mrriegel.storagenetwork.block.cable.TileCable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class CableFacadeMessage implements IMessage, IMessageHandler<CableFacadeMessage, IMessage> {

  private BlockPos pos;
  private boolean erase = false;
  //blockstate encoding matching tile storage
  private String block = "";
  private int meta = 0;

  public CableFacadeMessage() {}

  public CableFacadeMessage(BlockPos pos, IBlockState state) {
    this.pos = pos;
    this.block = ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString();
    this.meta = state.getBlock().getMetaFromState(state);
  }

  public CableFacadeMessage(BlockPos pos, boolean eraseIn) {
    this.pos = pos;
    this.erase = eraseIn;
  }

  @Override
  public IMessage onMessage(final CableFacadeMessage message, final MessageContext ctx) {
    EntityPlayerMP player = ctx.getServerHandler().player;
    World world = player.world;
    IThreadListener mainThread = (WorldServer) world;
    mainThread.addScheduledTask(new Runnable() {

      @Override
      public void run() {
        TileCable tile = BlockCable.getTileCable(world, message.pos);
        if (message.erase) {
          StorageNetwork.log("unset facade ");
          tile.setFacadeState(null);
          world.markAndNotifyBlock(message.pos.toImmutable(), world.getChunk(message.pos), tile.getBlockType().getDefaultState(), tile.getBlockType().getDefaultState(),
              3);
          tile.markDirty();
        }
        else {
          StorageNetwork.log("set facade: " + message.block);
          tile.setFacadeState(message.block, message.meta);
          world.markAndNotifyBlock(message.pos.toImmutable(), world.getChunk(message.pos), tile.getBlockType().getDefaultState(), tile.getBlockType().getDefaultState(), 1 | 2);
          tile.markDirty();
        }
      }
    });
    return null;
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    this.pos = BlockPos.fromLong(buf.readLong());
    this.erase = buf.readBoolean();
    this.block = ByteBufUtils.readUTF8String(buf);
    this.meta = buf.readInt();
  }

  @Override
  public void toBytes(ByteBuf buf) {
    buf.writeLong(this.pos.toLong());
    buf.writeBoolean(this.erase);
    ByteBufUtils.writeUTF8String(buf, this.block);
    buf.writeInt(this.meta);
  }
}
