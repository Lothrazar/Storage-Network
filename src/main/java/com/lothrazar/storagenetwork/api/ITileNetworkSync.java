package com.lothrazar.storagenetwork.api;
public interface ITileNetworkSync {

  boolean isDownwards();

  void setDownwards(boolean downwards);

  EnumSortType getSort();

  void setSort(EnumSortType sort);

  void setJeiSearchSynced(boolean val);

  void setAutoFocus(boolean autoFocus);

  default boolean isFullStackCraft() {
    return true;
  }

  default void setFullStackCraft(boolean val) {}
}
