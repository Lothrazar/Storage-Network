package com.lothrazar.storagenetwork.capability;

import com.lothrazar.storagenetwork.api.DimPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class CapabilityConnectable extends DefaultConnectable implements INBTSerializable<CompoundTag> {

  public CapabilityConnectable() {
    filters.setIsAllowlist(true);
  }

  @Override
  public CompoundTag serializeNBT(HolderLookup.Provider registries) {
    CompoundTag result = new CompoundTag();
    if (getMainPos() == null) {
      return result;
    }
    result.put("master", getMainPos().serializeNBT(registries));
    if (getPos() != null) {
      result.put("self", getPos().serializeNBT(registries));
    }
    CompoundTag filters = this.filters.serializeNBT(registries);
    result.put("filters", filters);
    result.putBoolean("needsRedstone", this.needsRedstone());
    return result;
  }

  @Override
  public void deserializeNBT(HolderLookup.Provider registries, CompoundTag nbt) {
    setMainPos(new DimPos(nbt.getCompound("master")));
    if (nbt.contains("self")) {
      setPos(new DimPos(nbt.getCompound("self")));
    }
    if (nbt.contains("filters")) {
      CompoundTag filters = nbt.getCompound("filters");
      this.filters.deserializeNBT(registries, filters);
    }
    this.needsRedstone(nbt.getBoolean("needsRedstone"));
  }
}
