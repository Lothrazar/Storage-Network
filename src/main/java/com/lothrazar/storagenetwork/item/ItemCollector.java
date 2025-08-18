package com.lothrazar.storagenetwork.item;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import org.apache.commons.lang3.tuple.Triple;
import com.lothrazar.library.item.ItemFlib;
import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.util.UtilInventory;
import com.lothrazar.storagenetwork.util.UtilTileEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;

public class ItemCollector extends ItemFlib {

  public static final String NBT_BOUND = "bound";

  public ItemCollector(Properties properties) {
    super(properties.stacksTo(1));
  }

  protected ItemStack findAmmo(Player player, Item item) {
    //is curios installed? doesnt matter this is safe
    Triple<String, Integer, ItemStack> remote = UtilInventory.getCurioRemote(player, item);
    return remote.getRight();
  }

    public void toggleEnabled(ItemStack stack, Player player) {
        boolean enabled = stack.getOrCreateTag().getBoolean(NBT_ENABLED);
        stack.getOrCreateTag().putBoolean(NBT_ENABLED, !enabled);
        player.displayClientMessage(
                Component.literal("Collector " + (!enabled ? "enabled" : "disabled")),
                true
        );
    }

    // not subscribe, called from SsnEvents.java
    public void onEntityItemPickupEvent(EntityItemPickupEvent event) {
        if (event.getEntity() instanceof Player &&
                event.getItem() != null &&
                !event.getItem().getItem().isEmpty()) {
            Player player = event.getEntity();

            // find the collector that the player has with them (main hand, offhand, curios...)
            ItemStack collectorStack = this.findAmmo(player, this);
            if (collectorStack.isEmpty()) {
                return;
            }

            // check if it is turned on
            CompoundTag tag = collectorStack.getOrCreateTag();
            if (!tag.getBoolean("Enabled")) {
                return;
            }

            ItemStack item = event.getItem().getItem();
            Level world = player.level();
            DimPos dp = DimPos.getPosStored(collectorStack);
            if (dp != null && !world.isClientSide) {
                ServerLevel serverTargetWorld = DimPos.stringDimensionLookup(dp.getDimension(), world.getServer());
                if (serverTargetWorld == null) {
                    StorageNetworkMod.LOGGER.error("Missing dimension key " + dp.getDimension());
                    return;
                }
                BlockEntity tile = serverTargetWorld.getBlockEntity(dp.getBlockPos());
                if (tile instanceof TileMain network) {
                    int countUnmoved = network.insertStack(item.copy(), false);
                    item.setCount(countUnmoved);
                    if (countUnmoved == 0) {
                        UtilTileEntity.playSoundFromServer((ServerPlayer) player, SoundEvents.ITEM_PICKUP, 0.2F);
                    }
                }
                // else { StorageNetworkMod.LOGGER.error("item.remote.notfound"); }
            }
        }
    }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    InteractionHand hand = context.getHand();
    Level world = context.getLevel();
    BlockPos pos = context.getClickedPos();
    Player player = context.getPlayer();
    if (world.getBlockEntity(pos) instanceof TileMain) {
      ItemStack stack = player.getItemInHand(hand);
      DimPos.putPos(stack, pos, world);
      UtilTileEntity.statusMessage(player, "item.remote.connected");
      return InteractionResult.SUCCESS;
    }
    return InteractionResult.PASS;
  }

  @Override
  @OnlyIn(Dist.CLIENT)
  public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
    MutableComponent t = Component.translatable(getDescriptionId() + ".tooltip");
    t.withStyle(ChatFormatting.GRAY);
    tooltip.add(t);
    if (stack.hasTag()) {
      DimPos dp = DimPos.getPosStored(stack);
      if (dp != null) {
        tooltip.add(dp.makeTooltip());
      }
    }
  }
}
