package com.lothrazar.storagenetwork.api.network;

import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.api.capabilities.FilterItemStackHandler;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public class ConnectableNodeDefault implements ConnectableNode, ValueIOSerializable {

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
  public void serialize(ValueOutput output) {
    if (getMainPos() == null) {
      return;
    }
    getMainPos().serialize(output.child("master"));
    if (getPos() != null) {
      getPos().serialize(output.child("self"));
    }
    this.getFilter().serialize(output.child("filters"));
    output.putBoolean("needsRedstone", this.needsRedstone());
  }

  @Override
  public void deserialize(ValueInput input) {
    setMainPos(DimPos.of(input.childOrEmpty("master")));
    input.child("self").ifPresent(self -> setPos(DimPos.of(self)));
    input.child("filters").ifPresent(filters -> this.getFilter().deserialize(filters));
    this.needsRedstone(input.getBooleanOr("needsRedstone", false));
  }
}
