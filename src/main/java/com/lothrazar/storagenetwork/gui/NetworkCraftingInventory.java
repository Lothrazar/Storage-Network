package com.lothrazar.storagenetwork.gui;

import java.util.Map;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.util.NonNullList;

public class NetworkCraftingInventory extends CraftingInventory {

  private static final int SIZE = 3;
  /** stupid thing is private with no getter so overwrite */
  private final NonNullList<ItemStack> stackList;
  private final Container eventHandler;
  public boolean skipEvents;

  public NetworkCraftingInventory(Container eventHandlerIn) {
    super(eventHandlerIn, SIZE, SIZE);
    eventHandler = eventHandlerIn;
    stackList = NonNullList.<ItemStack> withSize(SIZE * SIZE, ItemStack.EMPTY);
  }

  public NetworkCraftingInventory(Container eventHandlerIn, Map<Integer, ItemStack> matrix) {
    this(eventHandlerIn);
    skipEvents = true;
    for (int i = 0; i < SIZE * SIZE; i++) {
      if (matrix.get(i) != null && matrix.get(i).isEmpty() == false) {
        setInventorySlotContents(i, matrix.get(i));
      }
    }
    skipEvents = false;
  }

  @Override
  public void setInventorySlotContents(int index, ItemStack stack) {
    stackList.set(index, stack);
    if (skipEvents == false) {
      eventHandler.onCraftMatrixChanged(this);
    }
  }

  @Override
  public int getSizeInventory() {
    return stackList.size();
  }

  @Override
  public boolean isEmpty() {
    for (ItemStack itemstack : stackList) {
      if (!itemstack.isEmpty()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public ItemStack getStackInSlot(int index) {
    return index >= getSizeInventory() ? ItemStack.EMPTY : (ItemStack) stackList.get(index);
  }

  @Override
  public ItemStack removeStackFromSlot(int index) {
    return ItemStackHelper.getAndRemove(stackList, index);
  }

  @Override
  public ItemStack decrStackSize(int index, int count) {
    ItemStack itemstack = ItemStackHelper.getAndSplit(stackList, index, count);
    if (!itemstack.isEmpty()) {
      eventHandler.onCraftMatrixChanged(this);
    }
    return itemstack;
  }

  @Override
  public void clear() {
    stackList.clear();
  }

  @Override
  public void fillStackedContents(RecipeItemHelper helper) {
    for (ItemStack itemstack : stackList) {
      helper.accountStack(itemstack);
    }
  }
}
