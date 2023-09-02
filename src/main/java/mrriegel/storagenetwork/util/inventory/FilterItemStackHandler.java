package mrriegel.storagenetwork.util.inventory;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import mrriegel.storagenetwork.api.data.IItemStackMatcher;
import mrriegel.storagenetwork.data.ItemStackMatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class FilterItemStackHandler extends ItemStackHandlerEx {

  public static final int FILTER_SIZE = 18;
  public boolean ores = false;
  public boolean meta = true;
  public boolean nbt = false;
  public boolean isWhitelist = true;

  public FilterItemStackHandler() {
    super(FILTER_SIZE);
  }

  public void setMatchOreDict(boolean ores) {
    this.ores = ores;
  }

  public void setMatchMeta(boolean meta) {
    this.meta = meta;
  }

  public void setMatchNbt(boolean nbt) {
    this.nbt = nbt;
  }

  public void setIsWhitelist(boolean whitelist) {
    isWhitelist = whitelist;
  }

  @Override
  protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
    return 1;
  }

  public List<IItemStackMatcher> getStackMatchers() {
    List<ItemStack> storedStacks = getStacks();
    List<IItemStackMatcher> ret = new ArrayList<>(storedStacks.size());
    for (ItemStack stack : storedStacks) {
      if (!stack.isEmpty())
        ret.add(new ItemStackMatcher(stack, meta, ores, nbt));
    }
    return ret;
  }

  public void clear() {
    for (int slot = 0; slot < getSlots(); slot++) {
      this.setStackInSlot(slot, ItemStack.EMPTY);
    }
  }

  public boolean exactStackAlreadyInList(ItemStack stack) {
    // Should we want not to use the configured rules for nbt, oredict and meta, we can use this line instead, which really matches for the exact stack:
    //return getStacks().stream().map(filteredStack -> new ItemStackMatcher(filteredStack, true, false, true)).anyMatch(matcher -> matcher.match(stack));
    return getStackMatchers().stream().anyMatch(matcher -> matcher.match(stack));
  }

  public boolean isStackFiltered(ItemStack stack) {
    if (isWhitelist) {
      return getStackMatchers().stream().noneMatch(matcher -> matcher.match(stack));
    }
    return getStackMatchers().stream().anyMatch(matcher -> matcher.match(stack));
  }

  @Override
  public void deserializeNBT(NBTTagCompound nbt) {
    super.deserializeNBT(nbt);
    NBTTagCompound rulesTag = nbt.getCompoundTag("rules");
    this.ores = rulesTag.getBoolean("ores");
    this.meta = rulesTag.getBoolean("meta");
    this.nbt = rulesTag.getBoolean("nbt");
    this.isWhitelist = rulesTag.getBoolean("whitelist");
  }

  @Override
  public NBTTagCompound serializeNBT() {
    NBTTagCompound result = super.serializeNBT();
    NBTTagCompound rulesTag = new NBTTagCompound();
    rulesTag.setBoolean("ores", ores);
    rulesTag.setBoolean("meta", meta);
    rulesTag.setBoolean("nbt", nbt);
    rulesTag.setBoolean("whitelist", isWhitelist);
    result.setTag("rules", rulesTag);
    return result;
  }
}
