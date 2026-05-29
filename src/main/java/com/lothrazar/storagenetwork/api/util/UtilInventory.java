package com.lothrazar.storagenetwork.api.util;

import org.apache.commons.lang3.tuple.Triple;
import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.api.capabilities.ItemStackMatcherDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.items.IItemHandler;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

public class UtilInventory {

  public static void nukeAndDrop(DimPos lookPos) {
    lookPos.getWorld().destroyBlock(lookPos.getBlockPos(), true);
    lookPos.getWorld().removeBlockEntity(lookPos.getBlockPos());
  }

  public static String getStackKey(ItemStack stackInCopy) {
    return BuiltInRegistries.ITEM.getKey(stackInCopy.getItem()).toString();
  }

  /**
   * First check curios. Then ender chest, Then player inventory. Then left/right hands
   * 
   * @param player
   * @param remote
   * @return
   */
  public static Triple<String, Integer, ItemStack> getCurioRemote(Player player, Item remote) {
    Triple<String, Integer, ItemStack> stackFound = Triple.of("", -1, ItemStack.EMPTY);
    //MAIN hand first 
    if (isRemoteWithData(player.getMainHandItem(), remote)) {
      return Triple.of("hand", -1, player.getMainHandItem());
    }
    if (ModList.get().isLoaded("curios")) {
      //check curios slots
      SlotResult first = CuriosApi.getCuriosInventory(player)
          .flatMap(handler -> handler.findFirstCurio(remote))
          .orElse(null);
      if (first != null && isRemoteWithData(first.stack(), remote)) {
        //success: try to insert items to network thru this remote 
        return Triple.of("curios", first.slotContext().index(), first.stack());
      }
    }
    //not curios, check others
    for (int i = 0; i < player.getEnderChestInventory().getContainerSize(); i++) {
      ItemStack temp = player.getEnderChestInventory().getItem(i);
      if (isRemoteWithData(temp, remote)) {
        return Triple.of("ender", i, temp);
      }
    }
    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
      ItemStack temp = player.getInventory().getItem(i);
      if (isRemoteWithData(temp, remote)) {
        return Triple.of("player", i, temp);
      }
    }
    //default
    if (isRemoteWithData(player.getOffhandItem(), remote)) {
      return Triple.of("offhand", -1, player.getOffhandItem());
    }
    return stackFound;
  }

  private static boolean isRemoteWithData(ItemStack stack, Item remote) {
    //if it has a tag, assume pos to network is valid
    return stack.getItem() == remote && stack.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
  }

  public static String formatLargeNumber(int size) {
    if (size < Math.pow(10, 3)) {
      return size + "";
    }
    else if (size < Math.pow(10, 6)) {
      //      float r = (size) / 1000.0F;
      int rounded = Math.round(size / 1000.0F); //so 1600 => 1.6 and then rounded to become 2.
      return rounded + "K";
    }
    else if (size < Math.pow(10, 9)) {
      int rounded = Math.round(size / (float) Math.pow(10, 6));
      return rounded + "M";
    }
    else if (size < Math.pow(10, 12)) {
      int rounded = Math.round(size / (float) Math.pow(10, 9));
      return rounded + "B";
    }
    return size + "";
  }

  public static int countHowMany(IItemHandler inv, ItemStack stackIn) {
    int found = 0;
    for (int i = 0; i < inv.getSlots(); i++) {
      if (ItemStack.isSameItemSameComponents(inv.getStackInSlot(i), stackIn)) {
        found += inv.getStackInSlot(i).getCount();
      }
    }
    return found;
  }

  public static int containsAtLeastHowManyNeeded(IItemHandler inv, ItemStack stackIn, int minimumCount) {
    int found = 0;
    for (int i = 0; i < inv.getSlots(); i++) {
      if (ItemStack.isSameItemSameComponents(inv.getStackInSlot(i), stackIn)) {
        found += inv.getStackInSlot(i).getCount();
      }
    }
    //do you have all 4? or do you need 2 still
    if (found >= minimumCount) {
      return 0;
    }
    return minimumCount - found;
  }

  public static ItemStack extractItem(IItemHandler inv, ItemStackMatcherDefault fil, int num, boolean simulate) {
    if (inv == null || fil == null) {
      return ItemStack.EMPTY;
    }
    int extracted = 0;
    for (int i = 0; i < inv.getSlots(); i++) {
      ItemStack slot = inv.getStackInSlot(i);
      if (fil.match(slot)) {
        ItemStack ex = inv.extractItem(i, 1, simulate);
        if (!ex.isEmpty()) {
          extracted++;
          if (extracted == num) {
            return slot.copyWithCount(num);
          }
          else {
            i--;
          }
        }
      }
    }
    return ItemStack.EMPTY;
  }

  public static void dropItem(Level world, BlockPos pos, ItemStack stack) {
    if (pos == null || world.isClientSide || stack.isEmpty()) {
      return;
    }
    world.addFreshEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack));
  }
}
