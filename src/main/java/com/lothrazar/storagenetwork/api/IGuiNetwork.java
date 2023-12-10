package com.lothrazar.storagenetwork.api;

import java.util.List;

import com.lothrazar.storagenetwork.gui.NetworkWidget;
import net.minecraft.world.item.ItemStack;

public interface IGuiNetwork extends IGuiPrivate {

  public NetworkWidget getNetworkWidget();

  void setStacks(List<ItemStack> stacks);

  boolean getDownwards();

  boolean isJeiSearchSynced();

  void setJeiSearchSynced(boolean val);

  void setDownwards(boolean val);

  EnumSortType getSort();

  void syncDataToServer();

  void setSort(EnumSortType val);

  boolean getAutoFocus();

  void setAutoFocus(boolean b);
}
