package com.lothrazar.storagenetwork.registry;

import com.lothrazar.library.packet.BlockFacadeMessage;
import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.block.cable.TileCable;
import com.lothrazar.storagenetwork.block.drawer.BlockDrawer;
import com.lothrazar.storagenetwork.block.drawer.TileDrawer;
import com.lothrazar.storagenetwork.item.ItemBuilder;
import com.lothrazar.storagenetwork.network.KeybindCollectorToggleMessage;
import com.lothrazar.storagenetwork.network.KeybindCurioMessage;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

public class SsnEvents {

  @SubscribeEvent
  public void onEntityItemPickupEvent(ItemEntityPickupEvent.Pre event) {
    SsnRegistry.Items.COLLECTOR_REMOTE.get().onEntityItemPickupEvent(event);
  }

  @SubscribeEvent
  public void onHit(PlayerInteractEvent.LeftClickBlock event) {
    ItemBuilder.onLeftClickBlock(event);
    if (ConfigRegistry.enableFacades.get()) {
      onHitFacadeHandler(event);
    }
    BlockDrawer.handleLeftClick(event);
  }

  private void onHitFacadeHandler(PlayerInteractEvent.LeftClickBlock event) {
    Level level = event.getLevel();
    if (!level.isClientSide) {
      return; // dont save client data; server side only from here on
    }
    Player player = event.getEntity();
    if (!player.isCrouching()) {
      return; // match with 1.12 pr, only put facades when crouching
    }
    ItemStack held = player.getItemInHand(event.getHand());
    TileCable cable = TileCable.getTileCable(level, event.getPos());
    if (cable != null) {
      if (held.isEmpty()) {
        PacketDistributor.sendToServer(new BlockFacadeMessage(event.getPos(), true));
      } else {
        Block block = Block.byItem(held.getItem());
        if (block == null || block == Blocks.AIR) {
          return;
        }
        if (!ConfigRegistry.isFacadeAllowed(held)) {
          StorageNetworkMod.LOGGER.debug("not allowed as a facade from config file: " + held.getItem());
          return;
        }
        //pick the block, write to tags, and send to server
        boolean pickFluids = false;
        BlockHitResult bhr = (BlockHitResult) player.pick(player.blockInteractionRange(), 1, pickFluids);
        BlockPlaceContext context = new BlockPlaceContext(player, event.getHand(), held, bhr);
        BlockState facadeState = block.getStateForPlacement(context);
        CompoundTag tags = (facadeState == null) ? new CompoundTag() : NbtUtils.writeBlockState(facadeState);
        PacketDistributor.sendToServer(new BlockFacadeMessage(event.getPos(), tags));
      }
    }
  }

  @SubscribeEvent
  public void onKeyInput(InputEvent.Key event) {
    if (ClientEventRegistry.INVENTORY_KEY.consumeClick()) {
      PacketDistributor.sendToServer(new KeybindCurioMessage());
    }

    if (ClientEventRegistry.COLLECTOR_TOGGLE_KEY.consumeClick()) {
      PacketDistributor.sendToServer(new KeybindCollectorToggleMessage());
    }
  }
}
