package com.lothrazar.storagenetwork.api;

import java.util.List;
import com.lothrazar.storagenetwork.gui.NetworkWidget;
import com.lothrazar.storagenetwork.network.SettingsSyncMessage;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

public interface IGuiNetwork extends IGuiPrivate {

  NetworkWidget getNetwork();

  void setStacks(List<ItemStack> stacks);

  boolean getDownwards();

  boolean isJeiSearchSynced();

  void setJeiSearchSynced(boolean val);

  void setDownwards(boolean val);

  EnumSortType getSort();

  default BlockPos getPos() {
    return null;
  }

  default void syncDataToServer() {
    PacketDistributor.sendToServer(new SettingsSyncMessage(getPos(), getDownwards(), getSort(), isJeiSearchSynced(), getAutoFocus()));
  }

  void setSort(EnumSortType val);

  boolean getAutoFocus();

  void setAutoFocus(boolean b);
}
