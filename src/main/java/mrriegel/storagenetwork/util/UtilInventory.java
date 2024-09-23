package mrriegel.storagenetwork.util;

import mrriegel.storagenetwork.data.ItemStackMatcher;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

public class UtilInventory {

  public static boolean hasItemHandler(IBlockAccess world, BlockPos pos, EnumFacing facing) {
    return getItemHandler(world.getTileEntity(pos), facing) != null;
  }

  public static boolean doOverlap(final String text, final String name) {
    return text.toLowerCase().contains(name.toLowerCase())
        || name.toLowerCase().contains(text.toLowerCase());
  }

  public static IItemHandler getItemHandler(TileEntity tile, EnumFacing side) {
    if (tile == null)
      return null;
    if (tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side))
      return tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
    if (tile instanceof ISidedInventory)
      return new SidedInvWrapper((ISidedInventory) tile, side);
    if (tile instanceof IInventory)
      return new InvWrapper((IInventory) tile);
    return null;
  }

  public static String formatLargeNumber(int size) {
    if (size < Math.pow(10, 3)) {
      return size + "";
    }
    else if (size < Math.pow(10, 6)) {
      int rounded = Math.round(size / 1000.0F);//so 1600 => 1.6 and then rounded to become 2.
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

  public static boolean contains(IItemHandler inv, ItemStack stack) {
    for (int i = 0; i < inv.getSlots(); i++) {
      if (ItemHandlerHelper.canItemStacksStack(inv.getStackInSlot(i), stack)) {
        return true;
      }
    }
    return false;
  }

  public static int containsAtLeastHowManyNeeded(IItemHandler inv, ItemStack stack, int minimumCount) {
    if (inv == null) {
      return 0;
    }
    int found = 0;
    for (int i = 0; i < inv.getSlots(); i++) {
      if (ItemHandlerHelper.canItemStacksStack(inv.getStackInSlot(i), stack)) {
        found += inv.getStackInSlot(i).getCount();
      }
      if (found >= minimumCount) {
        return 0;
      }
    }
    return minimumCount - found;
  }

  public static int getAmount(IItemHandler inv, ItemStackMatcher fil) {
    if (inv == null || fil == null) {
      return 0;
    }
    int amount = 0;
    for (int i = 0; i < inv.getSlots(); i++) {
      ItemStack slot = inv.getStackInSlot(i);
      if (fil.match(slot))
        amount += slot.getCount();
    }
    return amount;
  }

  // this doesn't look like it works right if multiple slots match but wouldn't stack
  public static ItemStack extractItem(IItemHandler inv, ItemStackMatcher fil, int num, boolean simulate) {
    if (inv == null || fil == null) {
      return ItemStack.EMPTY;
    }
    int extracted = 0;
    for (int i = 0; i < inv.getSlots(); i++) {
      ItemStack slot = inv.getStackInSlot(i);
      if (fil.match(slot)) {
        ItemStack ex = inv.extractItem(i, num - extracted, simulate);
        if (ex.isEmpty()) {
          continue;
        }
        int exFromSlot = ex.getCount();
        extracted += exFromSlot;
        if (extracted == num) {
          ex.setCount(num);
          return ex;
        }
      }
    }
    return ItemStack.EMPTY;
  }
}
