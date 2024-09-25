package mrriegel.storagenetwork.util.inventory;

import javax.annotation.Nonnull;
import mrriegel.storagenetwork.api.data.EnumUpgradeType;
import mrriegel.storagenetwork.block.cable.io.ContainerCableIO;
import net.minecraft.item.ItemStack;

public class UpgradesItemStackHandler extends ItemStackHandlerEx {

  public static final int IO_DEFAULT_SPEED = 30;

  public UpgradesItemStackHandler() {
    super(ContainerCableIO.UPGRADE_COUNT);
  }

  @Override
  protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
    return 1;
  }

  public int getSpeedRatio() {
    int speedUpgrades = getUpgradesOfType(EnumUpgradeType.SPEED);
    int slowUpgrades = getUpgradesOfType(EnumUpgradeType.SLOW);
    int speedRatio = IO_DEFAULT_SPEED; // no upgrades
    if (speedUpgrades > 0) {
      //so 1 speed upgrade is run every 30/2=15t, two is 30/3 ticks etc
      speedRatio = IO_DEFAULT_SPEED / (speedUpgrades + 1);
    }
    else if (slowUpgrades > 0) {
      //meaning IF one or more speed upgrades are present, then all slowness upgrades are IGNORED
      //so 1 Slow upgrade is run every 30*2=60t, two is 30*3=90 ticks 
      speedRatio = IO_DEFAULT_SPEED * (slowUpgrades + 1);
    }
    if (speedRatio < 1) {
      speedRatio = 1; // 0 wont happen but idk maybe
    }
    return speedRatio;
  }

  public int getUpgradesOfType(EnumUpgradeType upgradeType) {
    int res = 0;
    for (ItemStack stack : getStacks()) {
      if (stack.getItemDamage() != upgradeType.ordinal()) {
        continue;
      }
      res += Math.max(stack.getCount(), 0);
    }
    return res;
  }
}
