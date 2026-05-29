package com.lothrazar.storagenetwork.item.remote;

import com.lothrazar.library.util.ChatUtil;
import com.lothrazar.storagenetwork.api.util.UtilInventory;
import org.apache.commons.lang3.tuple.Triple;
import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.registry.ConfigRegistry;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class RemoteHelpers {

  public static void searchAndOpen(ServerPlayer player, ServerLevel serverWorld) {
    //TODO: search for data tag? or for a list?
    Triple<String, Integer, ItemStack> searchResult = UtilInventory.getCurioRemote(player, SsnRegistry.Items.CRAFTING_REMOTE.get());
    ItemStack curioRemote = searchResult.getRight();
    if (!curioRemote.isEmpty()) {
      RemoteHelpers.openRemote(serverWorld, player, curioRemote, SsnRegistry.Items.CRAFTING_REMOTE.get());
    }
    else { //crafting is the upgrade, so otherwise do regular 
      searchResult = UtilInventory.getCurioRemote(player, SsnRegistry.Items.INVENTORY_REMOTE.get());
      curioRemote = searchResult.getRight();
      if (!curioRemote.isEmpty()) {
        RemoteHelpers.openRemote(serverWorld, player, curioRemote, SsnRegistry.Items.INVENTORY_REMOTE.get());
      }
      else {
        //TODO: refactor this to be much smarter this nested stuff is terrible
        searchResult = UtilInventory.getCurioRemote(player, SsnRegistry.Items.EXPANDED_REMOTE.get());
        curioRemote = searchResult.getRight();
        if (!curioRemote.isEmpty()) {
          RemoteHelpers.openRemote(serverWorld, player, curioRemote, SsnRegistry.Items.EXPANDED_REMOTE.get());
        }
      }
    }
  }

  @SuppressWarnings("deprecation")
  public static boolean openRemote(Level world, Player player, ItemStack itemStackIn, MenuProvider theRemoteItem) {
    DimPos dp = DimPos.getPosStored(itemStackIn);
    if (dp == null) {
      //unbound or invalid data
      ChatUtil.sendStatusMessage(player, "item.remote.notconnected");
      return false;
    }
    //assume we are in the same world
    BlockPos posTarget = dp.getBlockPos();
    if (ConfigRegistry.ITEMRANGE.get() != -1) {
      double distance = player.distanceToSqr(posTarget.getX() + 0.5D, posTarget.getY() + 0.5D, posTarget.getZ() + 0.5D);
      if (distance >= ConfigRegistry.ITEMRANGE.get()) {
        ChatUtil.sendStatusMessage(player, "item.remote.outofrange");
        return false;
      }
    }
    //else it is -1 so dont even check distance
    //k now server only 
    if (world.isClientSide) {
      return false;
    }
    //now check the dimension world
    ServerLevel serverTargetWorld = null;
    try {
      serverTargetWorld = DimPos.stringDimensionLookup(dp.getDimension(), world.getServer());
      if (serverTargetWorld == null) {
        StorageNetworkMod.LOGGER.error("Missing dimension key " + dp.getDimension());
      }
    }
    catch (Exception e) {
      //
      StorageNetworkMod.LOGGER.error("unknown exception on dim " + dp.getDimension(), e);
      return false;
    }
    //now check is the area chunk loaded
    if (!serverTargetWorld.isAreaLoaded(posTarget, 1)) {
      ChatUtil.addChatMessage(player, "item.remote.notloaded");
      StorageNetworkMod.LOGGER.info(ChatUtil.lang("item.remote.notloaded") + posTarget);
      return false;
    }
    BlockEntity tile = serverTargetWorld.getBlockEntity(posTarget);
    if (tile instanceof TileMain) {
      ((ServerPlayer) player).openMenu(theRemoteItem);
      return true;
    }
    else {
      player.displayClientMessage(Component.translatable("item.remote.notfound"), true);
      return false;
    }
  }
}
