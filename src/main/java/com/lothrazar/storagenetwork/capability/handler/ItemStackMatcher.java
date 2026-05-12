package com.lothrazar.storagenetwork.capability.handler;

import com.lothrazar.storagenetwork.api.IItemStackMatcher;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class ItemStackMatcher implements IItemStackMatcher {

  private ItemStack stack;
  private boolean ore;
  private boolean nbt;

  public ItemStackMatcher(ItemStack stack) {
    this(stack, false, false);
  }

  public ItemStackMatcher(ItemStack stack, boolean ore, boolean nbt) {
    this.stack = stack;
    this.ore = ore;
    this.nbt = nbt;
  }

  private ItemStackMatcher() {}

  public void readFromNBT(HolderLookup.Provider registries, CompoundTag compound) {
    CompoundTag c = (CompoundTag) compound.get("stack");
    stack = c != null ? ItemStack.parseOptional(registries, c) : ItemStack.EMPTY;
    ore = compound.getBoolean("ore");
    nbt = compound.getBoolean("nbt");
  }

  public CompoundTag writeToNBT(HolderLookup.Provider registries, CompoundTag compound) {
    compound.put("stack", stack.save(registries));
    compound.putBoolean("ore", ore);
    compound.putBoolean("nbt", nbt);
    return compound;
  }

  @Override
  public String toString() {
    return "ItemStackMatcher [stack=" + stack + ", ore=" + ore + ", nbt=" + nbt + "]";
  }

  @Override
  public ItemStack getStack() {
    return stack;
  }

  public void setStack(ItemStack stack) {
    this.stack = stack;
  }

  public boolean isOre() {
    return ore;
  }

  public void setOre(boolean ore) {
    this.ore = ore;
  }

  public boolean isNbt() {
    return nbt;
  }

  public void setNbt(boolean nbt) {
    this.nbt = nbt;
  }

  public static ItemStackMatcher loadFilterItemFromNBT(HolderLookup.Provider registries, CompoundTag nbt) {
    ItemStackMatcher fil = new ItemStackMatcher();
    fil.readFromNBT(registries, nbt);
    return fil.getStack() != null && fil.getStack().getItem() != null ? fil : null;
  }

  @Override
  public boolean match(ItemStack stackIn) {
    if (stackIn.isEmpty()) {
      return false;
    }
    if (nbt && !ItemStack.isSameItemSameComponents(stack, stackIn)) {
      return false;
    }
    return stackIn.getItem() == stack.getItem();
  }

  public boolean match(IItemStackMatcher matcher) {
    ItemStack stack = matcher.getStack();
    return match(stack);
  }
}
