package com.lothrazar.storagenetwork.gui;

import java.util.Map;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;

public class NetworkCraftingInventory extends CraftingContainer {

  /** stupid thing is private with no getter so overwrite */
  private final NonNullList<ItemStack> stackList;
  private final AbstractContainerMenu eventHandler;
  private boolean skipEvents;

  public NetworkCraftingInventory(AbstractContainerMenu eventHandlerIn) {
    super(eventHandlerIn, 3, 3);
    eventHandler = eventHandlerIn;
    stackList = NonNullList.<ItemStack> withSize(3 * 3, ItemStack.EMPTY);
  }

  public NetworkCraftingInventory(AbstractContainerMenu eventHandlerIn, Map<Integer, ItemStack> matrix) {
    this(eventHandlerIn);
    skipEvents = true;
    for (int i = 0; i < 9; i++) {
      if (matrix.get(i) != null && matrix.get(i).isEmpty() == false) {
        setItem(i, matrix.get(i));
      }
    }
    skipEvents = false;
  }

  @Override
  public void setItem(int index, ItemStack stack) {
    stackList.set(index, stack);
    if (skipEvents == false) {
      eventHandler.slotsChanged(this);
    }
  }

  @Override
  public int getContainerSize() {
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
  public ItemStack getItem(int index) {
    return index >= getContainerSize() ? ItemStack.EMPTY : (ItemStack) stackList.get(index);
  }

  @Override
  public ItemStack removeItemNoUpdate(int index) {
    return ContainerHelper.takeItem(stackList, index);
  }

  @Override
  public ItemStack removeItem(int index, int count) {
    ItemStack itemstack = ContainerHelper.removeItem(stackList, index, count);
    if (!itemstack.isEmpty()) {
      eventHandler.slotsChanged(this);
    }
    return itemstack;
  }

  @Override
  public void clearContent() {
    stackList.clear();
  }

  @Override
  public void fillStackedContents(StackedContents helper) {
    for (ItemStack itemstack : stackList) {
      helper.accountStack(itemstack);
    }
  }
}
