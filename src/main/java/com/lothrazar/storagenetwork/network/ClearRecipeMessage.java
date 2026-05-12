package com.lothrazar.storagenetwork.network;

import java.util.ArrayList;
import java.util.List;
import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.gui.ContainerNetwork;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClearRecipeMessage implements CustomPacketPayload {

  public static final CustomPacketPayload.Type<ClearRecipeMessage> TYPE =
      new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(StorageNetworkMod.MODID, "clear_recipe"));

  public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClearRecipeMessage> STREAM_CODEC =
      StreamCodec.unit(new ClearRecipeMessage());

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  public static void handle(ClearRecipeMessage message, IPayloadContext ctx) {
    ctx.enqueueWork(() -> {
      ServerPlayer player = (ServerPlayer) ctx.player();
      ClearRecipeMessage.clearContainerRecipe(player, true);
    });
  }

  static void clearContainerRecipe(ServerPlayer player, boolean doRefresh) {
    if (player.containerMenu instanceof ContainerNetwork) {
      ContainerNetwork container = (ContainerNetwork) player.containerMenu;
      CraftingContainer craftMatrix = container.getCraftMatrix();
      if (container.isCrafting() && craftMatrix != null) {
        TileMain root = container.getTileMain();
        for (int i = 0; i < 9; i++) {
          if (root == null) {
            break;
          }
          ItemStack stackInSlot = craftMatrix.getItem(i);
          if (stackInSlot.isEmpty()) {
            continue;
          }
          int numBeforeInsert = stackInSlot.getCount();
          int remainingAfter = root.insertStack(stackInSlot.copy(), false);
          if (numBeforeInsert == remainingAfter) {
            continue;
          }
          if (remainingAfter == 0) {
            craftMatrix.setItem(i, ItemStack.EMPTY);
          }
          else {
            craftMatrix.setItem(i, stackInSlot.copyWithCount(remainingAfter));
          }
        }
        if (doRefresh) {
          List<ItemStack> list = root.getNetwork().getStacks();
          PacketDistributor.sendToPlayer(player, new StackRefreshClientMessage(list, new ArrayList<>()));
          container.broadcastChanges();
        }
      }
    }
  }
}
