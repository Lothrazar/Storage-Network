package com.lothrazar.storagenetwork.api.network;

import com.lothrazar.storagenetwork.api.capabilities.CapabilityConnectable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.List;

public interface BlockEntityCradle extends BlockEntityConnectableNode {

  // the meat
  List<CapabilityConnectable> getHeldLinks();

  /**
   * overlaps with BlockEntity
   */
  BlockPos getBlockPos();

  /**
   * overlaps with BlockEntity
   */
  Level getLevel();


}
