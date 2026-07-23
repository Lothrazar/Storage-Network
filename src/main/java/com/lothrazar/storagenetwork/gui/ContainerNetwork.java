package com.lothrazar.storagenetwork.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.google.common.collect.Lists;
import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.api.capabilities.ItemStackMatcherDefault;
import com.lothrazar.storagenetwork.network.StackRefreshClientMessage;
import net.neoforged.neoforge.network.PacketDistributor;
import com.lothrazar.storagenetwork.util.SsnConsts;
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
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.PlayerMainInvWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class ContainerNetwork extends AbstractContainerMenu {
  public static final Logger LOGGER = LogManager.getLogger();

  public abstract TileMain getTileMain();

  public abstract void slotChanged();

  protected final ResultContainer resultInventory;
  protected Inventory playerInv;
  protected ResultSlot result;
  protected List<Slot> playerSlots = new ArrayList<Slot>();
  protected boolean recipeLocked = false;
  protected Player player;
  protected CraftingRecipe recipeCurrent;
  private NetworkCraftingInventory matrix;
  protected int xPlayer = 8;
  protected int yPlayer = 174;
  protected int yCrafting = this.yPlayer - 64;

  protected ContainerNetwork(MenuType<?> type, int id) {
    super(type, id);
    this.resultInventory = new ResultContainer();
  }

  public NetworkCraftingInventory getCraftMatrix() {
    return matrix;
  }

  public void setCraftMatrix(NetworkCraftingInventory matrixIn) {
    matrix = matrixIn;
  }

  public abstract boolean isCrafting();

  //subclasses backed by a tile/remote override this; default true matches the post-bugfix shift-craft-to-full-stack behavior
  public boolean isFullStackCraft() {
    return true;
  }

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
        Slot slot = addSlot(new Slot(playerInv, j + i * 9 + 9, xPlayer + j * SsnConsts.SQ, yPlayer + i * SsnConsts.SQ));
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
      Slot slot = addSlot(new Slot(playerInv, i, xPlayer + i * SsnConsts.SQ, yPlayer + 4 + 3 * SsnConsts.SQ)); // 232
      playerSlots.add(slot);
    }
  }

  protected void bindGrid() {
    int index = 0;
    //3x3 crafting grid
    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 3; ++j) {
        Slot slot = addSlot(new Slot(matrix, index++, 8 + j * SsnConsts.SQ, yCrafting + i * SsnConsts.SQ));
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
    CraftingInput craftingInput = inventory.asCraftInput();
    if (world.getServer() == null) {
      return;
    }
    Optional<RecipeHolder<CraftingRecipe>> optional = world.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftingInput, world);
    if (optional.isPresent()) {
      this.recipeCurrent = optional.get().value();
    }
  }

  //from WorkbenchContainer::slotChangedCraftingGrid
  private void findMatchingRecipe(int containerId, Level world, Player player, CraftingContainer inventory, ResultContainer result) {
    if (!world.isClientSide()) {
      final int slotId = 0;
      ServerPlayer serverplayerentity = (ServerPlayer) player;
      ItemStack itemstack = ItemStack.EMPTY;
      CraftingInput craftingInput = inventory.asCraftInput();
      Optional<RecipeHolder<CraftingRecipe>> optional = world.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftingInput, world);
      if (optional.isPresent()) {
        RecipeHolder<CraftingRecipe> holder = optional.get();
        CraftingRecipe icraftingrecipe = holder.value();
        result.setRecipeUsed(holder);
        itemstack = icraftingrecipe.assemble(craftingInput);
        this.recipeCurrent = icraftingrecipe;
      }
      result.setItem(slotId, itemstack);
      serverplayerentity.connection.send(new ClientboundContainerSetSlotPacket(containerId, this.incrementStateId(), slotId, itemstack));
    }
  }

  @Override
  public ItemStack quickMoveStack(Player playerIn, int slotIndex) {
    Level level = playerIn.level();
    if (level.isClientSide()) {
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
        ItemStack stack = rest == 0 ? ItemStack.EMPTY : itemstack1.copyWithCount(rest);
        slot.set(stack);
        broadcastChanges();
        List<ItemStack> list = tileMain.getNetwork().getSortedStacks();
        if (playerIn instanceof ServerPlayer) {
          ServerPlayer sp = (ServerPlayer) playerIn;
          PacketDistributor.sendToPlayer(sp, new StackRefreshClientMessage(list, new ArrayList<>()));
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

  private String dumpMatrix() {
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < matrix.getContainerSize(); i++) {
      ItemStack s = matrix.getItem(i);
      sb.append(i).append("=").append(s.isEmpty() ? "empty" : (s.getItem() + "x" + s.getCount())).append(" ");
    }
    return sb.append("]").toString();
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
    ItemStack res = recipeCurrent.assemble(matrix.asCraftInput());
    if (res.isEmpty()) {
      StorageNetworkMod.LOGGER.error("err Recipe output is an empty stack " + recipeCurrent);
      return;
    }
    int sizePerCraft = res.getCount();
    //full-stack mode: keep crafting until a stack is filled (or ingredients/inventory run out)
    //single-craft mode: exactly one recipe execution
    final int limit = isFullStackCraft() ? res.getMaxStackSize() : sizePerCraft;
    int iter = 0;
    StorageNetworkMod.LOGGER.debug("[craftShift] START sizePerCraft={} limit={} fullStack={} for {}", sizePerCraft, limit, isFullStackCraft(), res);
    while (crafted + sizePerCraft <= limit) {
      iter++;
      res = recipeCurrent.assemble(matrix.asCraftInput());
      //StorageNetworkMod.LOGGER.debug("[craftShift] iter={} crafted={} res.count={}", iter, crafted, res.getCount());
      if (!ItemHandlerHelper.insertItemStacked(new PlayerMainInvWrapper(playerInv), res, true).isEmpty()) {
        //StorageNetworkMod.LOGGER.debug("[craftShift] BREAK iter={}: simulate-insert says no room", iter);
        break;
      }
      //stop if empty
      if (recipeCurrent.matches(matrix.asCraftInput(), level) == false) {
     //   StorageNetworkMod.LOGGER.debug("[craftShift] BREAK iter={}: recipe no longer matches. matrix={}", iter, dumpMatrix());
        break;
      }
      //onTake replaced with this handcoded rewrite
      //StorageNetworkMod.LOGGER.debug("[craftShift] addItemStackToInventory " + res);
      if (!player.getInventory().add(res)) {
        player.drop(res, false);
      }
      //iterate matrix indices directly; CraftingInput trims empty rows/cols so its index space doesn't match the 3x3 matrix
      for (int i = 0; i < matrix.getContainerSize(); ++i) {
        ItemStack slot = this.matrix.getItem(i);
        if (slot.isEmpty()) {
          continue;
        }
        var remainderTemplate = slot.getItem().getCraftingRemainder(slot);
        ItemStack containerItem = remainderTemplate != null ? remainderTemplate.create() : ItemStack.EMPTY;
        if (!containerItem.isEmpty()) {
          //milk bucket, water bucket, etc - replace ingredient with its container
          this.matrix.setItem(i, containerItem);
        }
        else {
          this.matrix.removeItem(i, 1);
        }
      }
      //END onTake redo
      crafted += sizePerCraft;
      ItemStack stackInSlot;
      ItemStack recipeStack;
      ItemStackMatcherDefault itemStackMatcherCurrent;
      for (int i = 0; i < matrix.getContainerSize(); i++) {
        stackInSlot = matrix.getItem(i);
        if (stackInSlot.isEmpty()) {
          recipeStack = recipeCopy.get(i);
          //////////////// booleans are meta, ore(?ignored?), nbt
          itemStackMatcherCurrent = !recipeStack.isEmpty() ? new ItemStackMatcherDefault(recipeStack, false, false) : null;
          //false here means dont simulate
          ItemStack req = tile.request(itemStackMatcherCurrent, 1, false);
          matrix.setItem(i, req);
        }
      }
      slotsChanged(matrix);
    }
    StorageNetworkMod.LOGGER.debug("[craftShift] END iter={} crafted={} (max would be {})", iter, crafted, res.getMaxStackSize());
    broadcastChanges();
    this.recipeLocked = false;
    //update recipe again in case remnants left : IE hammer and such
    this.slotsChanged(this.matrix);
    //dated network contents to the client so the top panel reflects consumed ingredients !!!
    if (player instanceof ServerPlayer sp) {
      List<ItemStack> list = tile.getNetwork().getSortedStacks();
      PacketDistributor.sendToPlayer(sp, new StackRefreshClientMessage(list, new ArrayList<>()));
    }
  }
}
