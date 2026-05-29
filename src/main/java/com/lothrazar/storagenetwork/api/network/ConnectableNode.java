package com.lothrazar.storagenetwork.api.network;

import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.api.capabilities.FilterItemStackHandler;
import net.minecraft.world.item.ItemStack;

/**
 * All blocks that can connect to the storage-network need to expose this capability. Because of the way the storage-networking is built up, each connectable needs to expose its own position and
 * dimension, so it can be fully traversed when necessary.
 *
 * If you want to expose this yourself instead of accessing it, you probably want to extend the DefaultConnectable implementation for your own capability.
 */
public interface ConnectableNode {

  void toggleNeedsRedstone();

  boolean needsRedstone();

  void needsRedstone(boolean in);

  /**
   * Return the position of the main. For historic reasons each block currently needs to know this.
   *
   * @return a DimPos with the proper dimension and position
   */
  DimPos getMainPos();

  /**
   * Return the position of this connectable.
   *
   * This is used to traverse the network and might be different from the actual block position. For example the Compact Machines mod bridges capabilities across dimensions and we need to continue
   * traversing the network inside the compact machine and not at the machine block itself.
   *
   * You should simply return the position of your block here.
   *
   * @return
   */
  DimPos getPos();

  /**
   * When your block is placed and a connected network updates, it calls this method to tell your capability where the {@link com.lothrazar.storagenetwork.block.main.TileMain} is. Store this value and return it in getmainPos().
   *
   * @param mainPos
   */
  void setMainPos(DimPos mainPos);

  /**
   * Set data used in getPos
   * 
   * @param lookPos
   */
  void setPos(DimPos lookPos);

  void setFilter(int value, ItemStack copy);

  FilterItemStackHandler getFilter();
}
