package mrriegel.storagenetwork.util.inventory;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class ItemStackHandlerEx extends ItemStackHandler {

  public ItemStackHandlerEx(int size) {
    super(size);
  }

  public List<ItemStack> getStacks() {
    List<ItemStack> result = new ArrayList<>();
    for (int slot = 0; slot < this.getSlots(); slot++) {
      ItemStack stack = this.getStackInSlot(slot);
      if (stack == null) {
        continue;
      }
      result.add(stack);
    }
    return result;
  }
}
