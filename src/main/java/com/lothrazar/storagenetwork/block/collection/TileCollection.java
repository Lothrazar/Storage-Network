package com.lothrazar.storagenetwork.block.collection;

import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.block.TileConnectable;
import com.lothrazar.storagenetwork.block.main.TileMain;
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

public class TileCollection extends TileConnectable implements MenuProvider {

  private CollectionItemStackHandler itemHandler;

  public TileCollection(BlockPos pos, BlockState state) {
    super(SsnRegistry.Tiles.COLLECTOR.get(), pos, state);
    itemHandler = new CollectionItemStackHandler();
    itemHandler.tile = this;
  }

  public CollectionItemStackHandler getItemHandler() {
    DimPos m = getMain();
    if (m != null) {
      TileMain tileMain = m.getTileEntity(TileMain.class);
      itemHandler.setMain(tileMain);
    }
    return itemHandler;
  }

  @Override
  protected void loadAdditional(CompoundTag compound, HolderLookup.Provider registries) {
    super.loadAdditional(compound, registries);
  }

  @Override
  protected void saveAdditional(CompoundTag compound, HolderLookup.Provider registries) {
    super.saveAdditional(compound, registries);
  }

  @Override
  public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
    return new ContainerCollectionFilter(i, level, worldPosition, playerInventory, playerEntity);
  }

  @Override
  public Component getDisplayName() {
    return Component.translatable("block.storagenetwork.collector");
  }
}
