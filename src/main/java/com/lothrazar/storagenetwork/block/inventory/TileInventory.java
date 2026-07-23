package com.lothrazar.storagenetwork.block.inventory;

import com.lothrazar.storagenetwork.api.EnumSortType;
import com.lothrazar.storagenetwork.api.network.TileNetworkSync;
import com.lothrazar.storagenetwork.block.TileConnectable;
import com.lothrazar.storagenetwork.block.request.TileRequest;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileInventory extends TileConnectable implements MenuProvider, TileNetworkSync {

  public static final String NBT_JEI = TileRequest.NBT_JEI;
  private boolean downwards;
  private EnumSortType sort = EnumSortType.NAME;
  private boolean isJeiSearchSynced;
  private boolean autoFocus = true;

  public TileInventory(BlockPos pos, BlockState state) {
    this(SsnRegistry.Tiles.INVENTORY.get(), pos, state);
  }

  public TileInventory(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
    super(blockEntityType, pos, state);
  }

  @Override
  public Component getDisplayName() {
    return Component.translatable("block.storagenetwork.inventory");
  }

  @Override
  public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
    return new ContainerNetworkInventory(i, level, worldPosition, playerInventory, playerEntity);
  }

  @Override
  protected void loadAdditional(ValueInput input) {
    super.loadAdditional(input);
    autoFocus = input.getBooleanOr("autoFocus", true);
    setDownwards(input.getBooleanOr("dir", false));
    setSort(EnumSortType.values()[input.getIntOr("sort", EnumSortType.NAME.ordinal())]);
    this.setJeiSearchSynced(input.getBooleanOr(NBT_JEI, false));
  }

  @Override
  protected void saveAdditional(ValueOutput output) {
    super.saveAdditional(output);
    output.putBoolean("dir", isDownwards());
    output.putInt("sort", getSort().ordinal());
    output.putBoolean("autoFocus", autoFocus);
    output.putBoolean(NBT_JEI, this.isJeiSearchSynced());
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

  public boolean isJeiSearchSynced() {
    return isJeiSearchSynced;
  }

  @Override
  public void setJeiSearchSynced(boolean val) {
    isJeiSearchSynced = val;
  }

  public boolean getAutoFocus() {
    return autoFocus;
  }

  @Override
  public void setAutoFocus(boolean autoFocus) {
    this.autoFocus = autoFocus;
  }
}
