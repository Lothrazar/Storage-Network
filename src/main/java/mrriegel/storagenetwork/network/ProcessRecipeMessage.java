package mrriegel.storagenetwork.network;

import java.util.ArrayList;
import io.netty.buffer.ByteBuf;
import mrriegel.storagenetwork.block.cable.ContainerCable;
import mrriegel.storagenetwork.block.cable.TileCable;
import mrriegel.storagenetwork.block.cable.processing.TileCableProcess;
import mrriegel.storagenetwork.registry.PacketRegistry;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ProcessRecipeMessage implements IMessage, IMessageHandler<ProcessRecipeMessage, IMessage> {

  private ArrayList<ItemStack> inputs;
  private ArrayList<ItemStack> outputs;

  private static void ensureListSize(ArrayList<ItemStack> list) {
    while (list.size() > 9) {
      list.remove(list.size() - 1);
    }
    while (list.size() < 9) {
      list.add(ItemStack.EMPTY);
    }
  }

  public ProcessRecipeMessage() {}

  public ProcessRecipeMessage(ArrayList<ItemStack> inputs, ArrayList<ItemStack> outputs) {
    ensureListSize(inputs);
    ensureListSize(outputs);
    this.inputs = inputs;
    this.outputs = outputs;
  }

  @Override
  public IMessage onMessage(final ProcessRecipeMessage message, final MessageContext ctx) {
    EntityPlayerMP player = ctx.getServerHandler().player;
    IThreadListener mainThread = (WorldServer) player.world;
    mainThread.addScheduledTask(() -> {
      if (player.openContainer instanceof ContainerCable) {
        TileCable tileCable = ((ContainerCable) player.openContainer).tile;
        if (tileCable instanceof TileCableProcess) {
          TileCableProcess processCable = (TileCableProcess) tileCable;
          for (int i = 0; i < 9; i++) {
            processCable.filters.setStackInSlot(i, message.inputs.get(i));
            processCable.filters.setStackInSlot(i + 9, message.outputs.get(i));
          }
          PacketRegistry.INSTANCE.sendTo(new RefreshFilterClientMessage(processCable.filters.getStacks()), player);
          processCable.markDirty();
        }
      }
    });
    return null;
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    this.inputs = new ArrayList<ItemStack>();
    this.outputs = new ArrayList<ItemStack>();
    for (int i = 0; i < 9; i++) {
      this.inputs.add(new ItemStack(ByteBufUtils.readTag(buf)));
      this.outputs.add(new ItemStack(ByteBufUtils.readTag(buf)));
    }
  }

  @Override
  public void toBytes(ByteBuf buf) {
    NBTTagCompound nbt = null;
    for (int i = 0; i < 9; i++) {
      nbt = inputs.get(i).serializeNBT();
      ByteBufUtils.writeTag(buf, nbt);
      nbt = outputs.get(i).serializeNBT();
      ByteBufUtils.writeTag(buf, nbt);
    }
  }
}
