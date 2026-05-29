package com.lothrazar.storagenetwork.api.capabilities;

import com.lothrazar.library.cap.ItemStackHandlerEx;
import com.lothrazar.storagenetwork.api.UpgradeType;
import net.minecraft.world.item.ItemStack;

public class UpgradesItemStackHandler extends ItemStackHandlerEx {

  public UpgradesItemStackHandler() {
    super(4);
  }

  @Override
  protected void validateSlotIndex(int slot) {
    if (stacks.size() == 1) {
      this.setSize(4);
    }
    super.validateSlotIndex(slot);
  }

  @Override
  public int getSlotLimit(int slot) {
    return 1;
  }

  public boolean hasUpgradesOfType(UpgradeType upgradeType) {
    for (ItemStack stack : getStacks()) {
      if (UpgradeType.isUpgradeOfType(stack.getItem(), upgradeType)) {
        return true;
      }
    }
    return false;
  }  public int getUpgradesOfType(UpgradeType upgradeType) {
    int res = 0;
    for (ItemStack stack : getStacks()) {
      if (UpgradeType.isUpgradeOfType(stack.getItem(), upgradeType)) {
        res += Math.max(stack.getCount(), 0);
      }
    }
    return res;
  }
//  public boolean hasUpgradesOfType(ItemUpgrade upgradeType) {
//    for (ItemStack stack : getStacks()) {
//      if (stack.getItem() == upgradeType) {
//        return true;
//      }
//    }
//    return false;
//  }
//  public int getUpgradesOfType(ItemUpgrade upgradeType) {
//    int res = 0;
//    for (ItemStack stack : getStacks()) {
//      if (stack.getItem() == upgradeType) {
//        res += Math.max(stack.getCount(), 0);
//      }
//    }
//    return res;
//  }
}
