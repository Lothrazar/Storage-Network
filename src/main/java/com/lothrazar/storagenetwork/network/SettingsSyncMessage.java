package com.lothrazar.storagenetwork.network;

import java.util.function.Supplier;
import com.lothrazar.storagenetwork.api.EnumSortType;
import com.lothrazar.storagenetwork.api.ITileNetworkSync;
import com.lothrazar.storagenetwork.gui.ContainerNetwork;
import com.lothrazar.storagenetwork.item.remote.ContainerNetworkCraftingRemote;
import com.lothrazar.storagenetwork.item.remote.ContainerNetworkRemote;
import com.lothrazar.storagenetwork.item.remote.ItemRemote;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public class SettingsSyncMessage {

  private BlockPos pos;
  private boolean direction;
  private EnumSortType sort;
  private boolean targetTileEntity;
  private boolean jeiSync;
  private boolean autoFocus;

  private SettingsSyncMessage() {}

  public SettingsSyncMessage(BlockPos pos, boolean direction, EnumSortType sort, boolean jeiSync, boolean autoFocus) {
    this.pos = pos;
    this.direction = direction;
    this.sort = sort;
    this.jeiSync = jeiSync;
    this.autoFocus = autoFocus;
  }

  public static void handle(SettingsSyncMessage message, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer player = ctx.get().getSender();
      /// is it a block?
      if (message.targetTileEntity) {
        BlockEntity tileEntity = player.level().getBlockEntity(message.pos);
        if (tileEntity instanceof ITileNetworkSync tile) {
          tile.setSort(message.sort);
          tile.setDownwards(message.direction);
          tile.setJeiSearchSynced(message.jeiSync);
          tile.setAutoFocus(message.autoFocus);
          tileEntity.setChanged();
        }
      } // else is it an item?
      else if (player.containerMenu instanceof ContainerNetwork remoteContainer) {
        ItemStack stackPlayerHeld = remoteContainer.getRemote();
        //if it passes the instanceof check, we also know it is not-Empty
        if (stackPlayerHeld.getItem() instanceof ItemRemote) {
          ItemRemote.setSort(stackPlayerHeld, message.sort);
          ItemRemote.setDownwards(stackPlayerHeld, message.direction);
          ItemRemote.setJeiSearchSynced(stackPlayerHeld, message.jeiSync);
          ItemRemote.setAutoFocus(stackPlayerHeld, message.autoFocus);
        }
      }
    });
    ctx.get().setPacketHandled(true);
  }

  public static SettingsSyncMessage decode(FriendlyByteBuf buf) {
    SettingsSyncMessage message = new SettingsSyncMessage();
    message.direction = buf.readBoolean();
    int sort = buf.readInt();
    message.sort = EnumSortType.values()[sort];
    message.targetTileEntity = buf.readBoolean();
    message.pos = buf.readBlockPos();
    message.jeiSync = buf.readBoolean();
    message.autoFocus = buf.readBoolean();
    return message;
  }

  public static void encode(SettingsSyncMessage msg, FriendlyByteBuf buf) {
    buf.writeBoolean(msg.direction);
    buf.writeInt(msg.sort.ordinal());
    if (msg.pos != null) {
      buf.writeBoolean(true);
      buf.writeBlockPos(msg.pos);
    }
    else { // to avoid null values // inconsistent buffer size
      buf.writeBoolean(false);
      buf.writeBlockPos(BlockPos.ZERO);
    }
    buf.writeBoolean(msg.jeiSync);
    buf.writeBoolean(msg.autoFocus);
  }
}
