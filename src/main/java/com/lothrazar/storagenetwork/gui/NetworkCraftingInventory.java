package com.lothrazar.storagenetwork.gui;

import java.util.Map;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;

public class NetworkCraftingInventory extends TransientCraftingContainer {

  private static final int SIZE = 3;
  private boolean skipEvents;

  public NetworkCraftingInventory(AbstractContainerMenu eventHandlerIn) {
    super(eventHandlerIn, SIZE, SIZE);
  }

  public NetworkCraftingInventory(AbstractContainerMenu eventHandlerIn, Map<Integer, ItemStack> matrix) {
    this(eventHandlerIn);
    skipEvents = true;
    for (int i = 0; i < SIZE * SIZE; i++) {
      ItemStack stack = matrix.get(i);
      if (stack != null && !stack.isEmpty()) {
        setItem(i, stack);
      }
    }
    skipEvents = false;
  }

  @Override
  public void setItem(int index, ItemStack stack) {
    if (skipEvents) {
      getItems().set(index, stack);
    }
    else {
      super.setItem(index, stack);
    }
  }
}
