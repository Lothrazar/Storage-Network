package mrriegel.storagenetwork.util.inventory;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;

@Deprecated
public class ProcessingItemStackHandler extends FilterItemStackHandler {

  public List<ItemStack> getInputs() {
    List<ItemStack> result = new ArrayList<>();
    for (int slot = 0; slot < 9; slot++) {
      ItemStack stack = this.getStackInSlot(slot);
      if (stack == null || stack.isEmpty()) {
        continue;
      }
      result.add(stack);
    }
    return result;
  }

  public List<ItemStack> getOutputs() {
    List<ItemStack> result = new ArrayList<>();
    for (int slot = 9; slot < 18; slot++) {
      ItemStack stack = this.getStackInSlot(slot);
      if (stack == null || stack.isEmpty()) {
        continue;
      }
      result.add(stack);
    }
    return result;
  }

  public boolean isOutputEmpty() {
    return getOutputs().isEmpty();
  }

  public boolean isInputEmpty() {
    return getInputs().isEmpty();
  }
}
