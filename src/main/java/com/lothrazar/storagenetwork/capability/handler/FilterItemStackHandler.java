package com.lothrazar.storagenetwork.capability.handler;

import java.util.List;
import java.util.stream.Collectors;
import com.lothrazar.library.cap.ItemStackHandlerEx;
import com.lothrazar.storagenetwork.api.IItemStackMatcher;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class FilterItemStackHandler extends ItemStackHandlerEx {

  public static final int FILTER_SIZE = 18;
  public boolean tags = false;
  public boolean nbt = false;
  public boolean isAllowList = true;

  public FilterItemStackHandler() {
    super(FILTER_SIZE);
  }

  public FilterItemStackHandler(int size) {
    super(size);
  }

  public void setMatchOreDict(boolean ores) {
    this.tags = ores;
  }

  public void setMatchNbt(boolean nbt) {
    this.nbt = nbt;
  }

  public void setIsAllowlist(boolean is) {
    isAllowList = is;
  }

  @Override
  protected int getStackLimit(int slot, ItemStack stack) {
    return 1;
  }

  public List<IItemStackMatcher> getStackMatchers() {
    return getStacks().stream().map(stack -> new ItemStackMatcher(stack, tags, nbt)).collect(Collectors.toList());
  }

  public void clear() {
    for (int slot = 0; slot < getSlots(); slot++) {
      setStackInSlot(slot, ItemStack.EMPTY);
    }
  }

  public boolean exactStackAlreadyInList(ItemStack stack) {
    // Should we want not to use the configured rules for nbt, oredict and meta, we can use this line instead, which really matches for the exact stack:
    //return getStacks().stream().map(filteredStack -> new ItemStackMatcher(filteredStack, true, false, true)).anyMatch(matcher -> matcher.match(stack));
    return getStackMatchers().stream().anyMatch(matcher -> matcher.match(stack));
  }

  public IItemStackMatcher getFirstMatcher(ItemStack stack) {
    for (IItemStackMatcher m : getStackMatchers()) {
      if (m.match(stack)) {
        return m;
      }
    }
    return null;
  }

  public boolean isStackFiltered(ItemStack stack) {
    if (isAllowList) {
      return getStackMatchers().stream().noneMatch(matcher -> matcher.match(stack));
    }
    return getStackMatchers().stream().anyMatch(matcher -> matcher.match(stack));
  }

  public boolean allAreEmpty() {
    for (int slot = 0; slot < getSlots(); slot++) {
      if (!this.getStackInSlot(slot).isEmpty()) {
        //found something not empty. so allAreEmpty is false
        return false;
      }
    }
    //none found that were !empty. so allempty true
    return true;
  }

  @Override
  public void deserializeNBT(HolderLookup.Provider registries, CompoundTag nbt) {
    super.deserializeNBT(registries, nbt);
    CompoundTag rulesTag = nbt.getCompound("rules");
    tags = rulesTag.getBoolean("tags");
    this.nbt = rulesTag.getBoolean("nbt");
    isAllowList = rulesTag.getBoolean("whitelist");
  }

  @Override
  public CompoundTag serializeNBT(HolderLookup.Provider registries) {
    CompoundTag result = super.serializeNBT(registries);
    CompoundTag rulesTag = new CompoundTag();
    rulesTag.putBoolean("tags", tags);
    rulesTag.putBoolean("nbt", nbt);
    rulesTag.putBoolean("whitelist", isAllowList);
    result.put("rules", rulesTag);
    return result;
  }

  public int getStackCount(ItemStack stackCurrent) {
    int s = 0;
    for (IItemStackMatcher m : getStackMatchers()) {
      if (ItemStack.isSameItemSameComponents(stackCurrent, m.getStack())) {
        return s += m.getStack().getCount();
      }
    }
    return s;
  }
}
