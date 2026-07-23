package com.lothrazar.storagenetwork.block.collection;

import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.block.TileConnectable;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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
  protected void loadAdditional(ValueInput input) {
    super.loadAdditional(input);
  }

  @Override
  protected void saveAdditional(ValueOutput output) {
    super.saveAdditional(output);
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
