package com.lothrazar.storagenetwork.api.gui;

import java.util.List;

import com.lothrazar.storagenetwork.api.EnumSortType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

public interface GuiNetwork extends GuiPrivate {

  NetworkWidget getNetwork(); // TODO: make NetworkWidget an API

  void setStacks(List<ItemStack> stacks);

  boolean getDownwards();

  boolean isJeiSearchSynced();

  void setJeiSearchSynced(boolean val);

  void setDownwards(boolean val);

  EnumSortType getSort();

  default BlockPos getPos() {
    return null;
  }

   void syncDataToServer();

  void setSort(EnumSortType val);

  boolean getAutoFocus();

  void setAutoFocus(boolean b);

  default boolean isFullStackCraft() {
    return true;
  }

  default void setFullStackCraft(boolean val) {}
}
