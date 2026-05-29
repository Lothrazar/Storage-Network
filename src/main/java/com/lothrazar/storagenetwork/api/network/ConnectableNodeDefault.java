package com.lothrazar.storagenetwork.api.network;

import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.api.capabilities.FilterItemStackHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class ConnectableNodeDefault implements ConnectableNode, INBTSerializable<CompoundTag> {

  FilterItemStackHandler filters = new FilterItemStackHandler();
  DimPos main;
  DimPos self;
  private boolean needsRedstone = false;

  @Override
  public void toggleNeedsRedstone() {
    needsRedstone = !needsRedstone;
  }

  @Override
  public boolean needsRedstone() {
    return this.needsRedstone;
  }

  @Override
  public void needsRedstone(boolean in) {
    this.needsRedstone = in;
  }

  @Override
  public FilterItemStackHandler getFilter() {
    return filters;
  }

  @Override
  public void setFilter(int value, ItemStack stack) {
    filters.setStackInSlot(value, stack);
    filters.getStacks().set(value, stack);
  }

  @Override
  public DimPos getMainPos() {
    return main;
  }

  @Override
  public DimPos getPos() {
    return self;
  }

  @Override
  public void setMainPos(DimPos mainIn) {
    this.main = mainIn;
  }

  @Override
  public void setPos(DimPos pos) {
    this.self = pos;
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
    CompoundTag filters = this.getFilter().serializeNBT(registries);
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
      this.getFilter().deserializeNBT(registries, filters);
    }
    this.needsRedstone(nbt.getBoolean("needsRedstone"));
  }
}
