package com.lothrazar.storagenetwork.api.capabilities;

import com.lothrazar.storagenetwork.api.network.BlockEntityMainNetwork;

/**
 * Only expose this capability if you want your cable/block to auto-export and import blocks controlled by the networks main. You could quite as well just expose ConnectableNode and do the
 * exporting/importing in your own update() method.
 * <p>
 * If you indeed want to add another exporting/importing cable in the style of the integrated ones, this might be for you. In all other cases, this is probably not what you want.
 */
public interface CapabilityProcessing {

//  Direction facingInventory();

  /**
   * Storages with a higher priority (== lower number) are processed first. You probably want to add a way to configure the priority of your storage.
   *
   * @return Return the priority here
   */
  int getPriority();

  void setPriority(int value);

  void execute(BlockEntityMainNetwork main);

}
