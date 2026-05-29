package com.lothrazar.storagenetwork.block.request;

import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.api.EnumSortType;
import com.lothrazar.storagenetwork.api.network.TileNetworkSync;
import com.lothrazar.storagenetwork.block.TileConnectable;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

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
  protected void loadAdditional(CompoundTag compound, HolderLookup.Provider registries) {
    autoFocus = compound.getBoolean("autoFocus");
    setDownwards(compound.getBoolean(NBT_DIR));
    if (compound.contains(NBT_SORT)) {
      setSort(EnumSortType.values()[compound.getInt(NBT_SORT)]);
    }
    if (compound.contains(NBT_JEI)) {
      this.setJeiSearchSynced(compound.getBoolean(NBT_JEI));
    }
    if (compound.contains(NBT_FULLSTACK)) {
      this.setFullStackCraft(compound.getBoolean(NBT_FULLSTACK));
    }
    super.loadAdditional(compound, registries);
  }

  @Override
  protected void saveAdditional(CompoundTag compound, HolderLookup.Provider registries) {
    compound.putBoolean("autoFocus", autoFocus);
    compound.putBoolean(NBT_DIR, isDownwards());
    compound.putInt(NBT_SORT, getSort().ordinal());
    compound.putBoolean(NBT_JEI, this.isJeiSearchSynced());
    compound.putBoolean(NBT_FULLSTACK, this.isFullStackCraft());
    super.saveAdditional(compound, registries);
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
}
