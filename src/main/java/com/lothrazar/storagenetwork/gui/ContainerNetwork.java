package com.lothrazar.storagenetwork.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.google.common.collect.Lists;
import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.capability.handler.ItemStackMatcher;
import com.lothrazar.storagenetwork.network.StackRefreshClientMessage;
import com.lothrazar.storagenetwork.registry.PacketRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import net.minecraftforge.network.NetworkDirection;

public abstract class ContainerNetwork extends AbstractContainerMenu {

  public abstract TileMain getTileMain();

  public abstract void slotChanged();

  protected final ResultContainer resultInventory;
  protected Inventory playerInv;
  protected ResultSlot result;

  protected List<Slot> playerSlots = new ArrayList<Slot>();
  protected boolean recipeLocked = false;
  protected Player player;
  protected CraftingRecipe recipeCurrent;
  public NetworkCraftingInventory matrix;

  protected ContainerNetwork(MenuType<?> type, int id) {
    super(type, id);
    this.resultInventory = new ResultContainer();
  }

  public CraftingContainer getCraftMatrix() {
    return matrix;
  }

  public abstract boolean isCrafting();

  public Slot getResultSlot() {
    return result;
  }
  public List<Slot> getPlayerSlots() {
    return playerSlots;
  }

  protected void bindPlayerInvo(Inventory playerInv) {
    this.player = playerInv.player;
    //player inventory
    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 9; ++j) {
        Slot slot = addSlot(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 174 + i * 18));
        playerSlots.add(slot);
      }
    }
  }

  @Override
  public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
    if (!this.isCrafting()) {
      return super.canTakeItemForPickAll(stack, slot);
    }
    return slot.container != result && super.canTakeItemForPickAll(stack, slot);
  }

  public void bindHotbar() {
    //player hotbar
    for (int i = 0; i < 9; ++i) {
      Slot slot = addSlot(new Slot(playerInv, i, 8 + i * 18, 232));
      playerSlots.add(slot);
    }
  }

  protected void bindGrid() {
    int index = 0;
    //3x3 crafting grid
    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 3; ++j) {
        Slot slot = addSlot(new Slot(matrix, index++, 8 + j * 18, 110 + i * 18));
        playerSlots.add(slot);
      }
    }
  }

  @Override
  public void removed(Player playerIn) {
    slotChanged();
    super.removed(playerIn);
  }

  @Override
  public void slotsChanged(Container inventoryIn) {
    if (recipeLocked) {
      return;
    }
    super.slotsChanged(inventoryIn);
    this.recipeCurrent = null;
    findMatchingRecipe(this.containerId, this.player.level(), this.player, this.matrix, this.resultInventory);
  }

  //it runs on server tho
  protected void findMatchingRecipeClient(Level world, CraftingContainer inventory, ResultContainer result) {
    Optional<CraftingRecipe> optional = world.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, inventory, world);
    if (optional.isPresent()) {
      CraftingRecipe icraftingrecipe = optional.get();
      this.recipeCurrent = icraftingrecipe;
    }
  }

  //from WorkbenchContainer::slotChangedCraftingGrid
  private void findMatchingRecipe(int containerId, Level world, Player player, CraftingContainer inventory, ResultContainer result) {
    if (!world.isClientSide) {
      final int slotId = 0;
      ServerPlayer serverplayerentity = (ServerPlayer) player;
      ItemStack itemstack = ItemStack.EMPTY;
      Optional<CraftingRecipe> optional = world.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, inventory, world);
      if (optional.isPresent()) {
        CraftingRecipe icraftingrecipe = optional.get();
        if (result.setRecipeUsed(world, serverplayerentity, icraftingrecipe)) {
          itemstack = icraftingrecipe.assemble(inventory, world.registryAccess());
          this.recipeCurrent = icraftingrecipe;
        }
      }
      result.setItem(slotId, itemstack);
      serverplayerentity.connection.send(new ClientboundContainerSetSlotPacket(containerId, this.incrementStateId(), slotId, itemstack));
    }
  }

  @Override
  public ItemStack quickMoveStack(Player playerIn, int slotIndex) {
    Level level = playerIn.level();
    if (level.isClientSide) {
      return ItemStack.EMPTY;
    }
    ItemStack itemstack = ItemStack.EMPTY;
    Slot slot = this.slots.get(slotIndex);
    if (slot != null && slot.hasItem()) {
      ItemStack itemstack1 = slot.getItem();
      itemstack = itemstack1.copy();
      TileMain tileMain = this.getTileMain();
      if (this.isCrafting() && slotIndex == 0) {
        craftShift(playerIn, tileMain);
        return ItemStack.EMPTY;
      }
      else if (tileMain != null) {
        int rest = tileMain.insertStack(itemstack1, false);
        ItemStack stack = rest == 0 ? ItemStack.EMPTY : ItemHandlerHelper.copyStackWithSize(itemstack1, rest);
        slot.set(stack);
        broadcastChanges();
        List<ItemStack> list = tileMain.nw.getSortedStacks();
        if (playerIn instanceof ServerPlayer) {
          ServerPlayer sp = (ServerPlayer) playerIn;
          PacketRegistry.INSTANCE.sendTo(new StackRefreshClientMessage(list, new ArrayList<>()),
              sp.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        }
        if (stack.isEmpty()) {
          return ItemStack.EMPTY;
        }
        slot.onTake(playerIn, itemstack1);
        return ItemStack.EMPTY;
      }
      if (itemstack1.getCount() == 0) {
        slot.set(ItemStack.EMPTY);
      }
      else {
        slot.setChanged();
      }
      if (itemstack1.getCount() == itemstack.getCount()) {
        return ItemStack.EMPTY;
      }
      slot.onTake(playerIn, itemstack1);
    }
    return itemstack;
  }

  /**
   * A note on the shift-craft delay bug root cause was ANY interaction with matrix (setting contents etc) was causing triggers/events to do a recipe lookup. Meaning during this shift-click action you
   * can get up to 9x64 FULL recipe scans Solution is just to disable all those triggers but only for duration of this action
   *
   * @param player
   * @param tile
   */
  @SuppressWarnings("deprecation")
  protected void craftShift(Player player, TileMain tile) {
    if (!this.isCrafting() || matrix == null || tile == null) {
      return;
    }
    recipeCurrent = null;
    Level level = player.level();
    this.findMatchingRecipeClient(level, this.matrix, this.resultInventory);
    if (recipeCurrent == null) {
      return;
    }
    this.recipeLocked = true;
    int crafted = 0;
    List<ItemStack> recipeCopy = Lists.newArrayList();
    for (int i = 0; i < matrix.getContainerSize(); i++) {
      recipeCopy.add(matrix.getItem(i).copy());
    }
    ItemStack res = recipeCurrent.assemble(matrix, level.registryAccess());
    if (res.isEmpty()) {
      StorageNetworkMod.LOGGER.error("err Recipe output is an empty stack " + recipeCurrent);
      return;
    }
    int sizePerCraft = res.getCount();
    //StorageNetwork.log("[craftShift] sizePerCraft = " + sizePerCraft + " for stack " + res);
    while (crafted + sizePerCraft <= res.getMaxStackSize()) {
      res = recipeCurrent.assemble(matrix, level.registryAccess());
      //  StorageNetwork.log("[craftShift]  crafted = " + crafted + " ; res.count() = " + res.getCount() + " MAX=" + res.getMaxStackSize());
      if (!ItemHandlerHelper.insertItemStacked(new PlayerMainInvWrapper(playerInv), res, true).isEmpty()) {
        //  StorageNetwork.log("[craftShift] cannot insert more, end");
        break;
      }
      //stop if empty
      if (recipeCurrent.matches(matrix, level) == false) {
        // StorageNetwork.log("[craftShift] recipe doesnt match i quit");
        break;
      }
      //onTake replaced with this handcoded rewrite
      //StorageNetwork.log("[craftShift] addItemStackToInventory " + res);
      if (!player.getInventory().add(res)) {
        player.drop(res, false);
      }
      NonNullList<ItemStack> remainder = recipeCurrent.getRemainingItems(this.matrix);
      for (int i = 0; i < remainder.size(); ++i) {
        ItemStack remainderCurrent = remainder.get(i);
        ItemStack slot = this.matrix.getItem(i);
        if (remainderCurrent.isEmpty()) {
          matrix.getItem(i).shrink(1);
          continue;
        }
        if (slot.getItem().getCraftingRemainingItem() != null) { //is the fix for milk and similar
          slot = new ItemStack(slot.getItem().getCraftingRemainingItem());
          matrix.setItem(i, slot);
        }
        else if (!slot.getItem().getCraftingRemainingItem(slot).isEmpty()) { //is the fix for milk and similar
          slot = slot.getItem().getCraftingRemainingItem(slot);
          matrix.setItem(i, slot);
        }
        else if (!remainderCurrent.isEmpty()) {
          if (slot.isEmpty()) {
            this.matrix.setItem(i, remainderCurrent);
          }
          else if (ItemStack.matches(slot, remainderCurrent) && ItemStack.isSameItemSameTags(slot, remainderCurrent)) {
            remainderCurrent.grow(slot.getCount());
            this.matrix.setItem(i, remainderCurrent);
          }
          else if (ItemStack.isSameItem(slot, remainderCurrent)) { //isSameIgnoreDurability
            //crafting that consumes durability
            this.matrix.setItem(i, remainderCurrent);
          }
          else {
            if (!player.getInventory().add(remainderCurrent)) {
              player.drop(remainderCurrent, false);
            }
          }
        }
        else if (!slot.isEmpty()) {
          this.matrix.removeItem(i, 1);
          slot = this.matrix.getItem(i);
        }
      } //end loop on remiainder
      //END onTake redo
      crafted += sizePerCraft;
      ItemStack stackInSlot;
      ItemStack recipeStack;
      ItemStackMatcher itemStackMatcherCurrent;
      for (int i = 0; i < matrix.getContainerSize(); i++) {
        stackInSlot = matrix.getItem(i);
        if (stackInSlot.isEmpty()) {
          recipeStack = recipeCopy.get(i);
          //////////////// booleans are meta, ore(?ignored?), nbt
          itemStackMatcherCurrent = !recipeStack.isEmpty() ? new ItemStackMatcher(recipeStack, false, false) : null;
          //false here means dont simulate
          ItemStack req = tile.request(itemStackMatcherCurrent, 1, false);
          matrix.setItem(i, req);
        }
      }
      slotsChanged(matrix);
    }
    broadcastChanges();
    this.recipeLocked = false;
    //update recipe again in case remnants left : IE hammer and such
    this.slotsChanged(this.matrix);
  }
}
