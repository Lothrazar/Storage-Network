package com.lothrazar.storagenetwork.block.exchange;

import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.block.TileConnectable;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TileExchange extends TileConnectable {
  public static final Logger LOGGER = LogManager.getLogger();

  private ExchangeItemStackHandler itemHandler;

  public TileExchange(BlockPos pos, BlockState state) {
    super(SsnRegistry.Tiles.EXCHANGE.get(), pos, state);
    itemHandler = new ExchangeItemStackHandler();
  }

  public ExchangeItemStackHandler getItemHandler() {
    try {
      DimPos m = getMain();
      if (m != null && itemHandler != null && itemHandler.tileMain == null) {
        TileMain tileMain = m.getTileEntity(TileMain.class);
        if (tileMain != null) {
          itemHandler.setMain(tileMain);
        }
      }
      return itemHandler;
    }
    catch (Exception e) {
      LOGGER.error("Exchange caught error from a mod", e);
      return null;
    }
  }

  @Override
  protected void loadAdditional(ValueInput input) {
    super.loadAdditional(input);
  }

  @Override
  protected void saveAdditional(ValueOutput output) {
    super.saveAdditional(output);
  }

  private void tick() {
    if (this.itemHandler != null && getLevel().getGameTime() % StorageNetworkMod.CONFIG.refreshTicks() == 0) {
      this.itemHandler.update();
    }
  }

  public static void clientTick(Level level, BlockPos blockPos, BlockState blockState, TileExchange tile) {
    tile.tick();
  }

  public static <E extends BlockEntity> void serverTick(Level level, BlockPos blockPos, BlockState blockState, TileExchange tile) {
    tile.tick();
  }
}
