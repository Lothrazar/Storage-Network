package com.lothrazar.storagenetwork.block.request;

import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.api.EnumSortType;
import com.lothrazar.storagenetwork.api.network.TileNetworkSync;
import com.lothrazar.storagenetwork.block.TileConnectable;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileRequest extends TileConnectable implements MenuProvider, TileNetworkSync {

  public static final String NBT_JEI = StorageNetworkMod.MODID + "jei";
  private static final String NBT_DIR = StorageNetworkMod.MODID + "dir";
  private static final String NBT_SORT = StorageNetworkMod.MODID + "sort";
  public static final String NBT_FULLSTACK = StorageNetworkMod.MODID + "fullstack";
  private boolean downwards;
  private EnumSortType sort = EnumSortType.NAME;
  private boolean isJeiSearchSynced;
  private boolean autoFocus = true;
  private boolean fullStackCraft = true;

  public TileRequest(BlockPos pos, BlockState state) {
    super(SsnRegistry.Tiles.REQUEST.get(), pos, state);
  }

  @Override
  protected void loadAdditional(ValueInput input) {
    autoFocus = input.getBooleanOr("autoFocus", true);
    setDownwards(input.getBooleanOr(NBT_DIR, false));
    setSort(EnumSortType.values()[input.getIntOr(NBT_SORT, getSort().ordinal())]);
    this.setJeiSearchSynced(input.getBooleanOr(NBT_JEI, false));
    this.setFullStackCraft(input.getBooleanOr(NBT_FULLSTACK, true));
    super.loadAdditional(input);
  }

  @Override
  protected void saveAdditional(ValueOutput output) {
    output.putBoolean("autoFocus", autoFocus);
    output.putBoolean(NBT_DIR, isDownwards());
    output.putInt(NBT_SORT, getSort().ordinal());
    output.putBoolean(NBT_JEI, this.isJeiSearchSynced());
    output.putBoolean(NBT_FULLSTACK, this.isFullStackCraft());
    super.saveAdditional(output);
  }

  @Override
  public boolean isDownwards() {
    return downwards;
  }

  @Override
  public void setDownwards(boolean downwards) {
    this.downwards = downwards;
  }

  @Override
  public EnumSortType getSort() {
    return sort;
  }

  @Override
  public void setSort(EnumSortType sort) {
    this.sort = sort;
  }

  @Override
  public Component getDisplayName() {
    return Component.translatable("block.storagenetwork.request");
  }

  @Override
  public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
    return new ContainerNetworkCraftingTable(i, level, worldPosition, playerInventory, playerEntity);
  }

  public boolean isJeiSearchSynced() {
    return isJeiSearchSynced;
  }

  @Override
  public void setJeiSearchSynced(boolean val) {
    isJeiSearchSynced = val;
  }

  public boolean getAutoFocus() {
    return this.autoFocus;
  }

  @Override
  public void setAutoFocus(boolean autoFocus) {
    this.autoFocus = autoFocus;
  }

  @Override
  public boolean isFullStackCraft() {
    return fullStackCraft;
  }

  @Override
  public void setFullStackCraft(boolean val) {
    this.fullStackCraft = val;
  }

  // Block#onRemove is gone in 26.1; this cleanup (used to live in BlockRequest#onRemove) now
  // runs here, called by the game at the equivalent point in the block-removal sequence.
  @Override
  public void preRemoveSideEffects(BlockPos pos, BlockState state) {
    super.preRemoveSideEffects(pos, state);
    if (level == null) {
      return;
    }
    BlockEntity blockentity = level.getBlockEntity(pos);
    if (blockentity instanceof Container container) {
      Containers.dropContents(level, pos, container);
      level.updateNeighbourForOutputSignal(pos, state.getBlock());
    }
  }
}
