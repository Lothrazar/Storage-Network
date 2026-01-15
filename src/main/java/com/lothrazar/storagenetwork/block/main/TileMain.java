package com.lothrazar.storagenetwork.block.main;

import java.util.List;
import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.api.EnumStorageDirection;
import com.lothrazar.storagenetwork.api.IConnectable;
import com.lothrazar.storagenetwork.api.IConnectableItemAutoIO;
import com.lothrazar.storagenetwork.api.IConnectableItemProcessing;
import com.lothrazar.storagenetwork.capability.handler.ItemStackMatcher;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import com.lothrazar.storagenetwork.registry.StorageNetworkCapabilities;
import com.lothrazar.storagenetwork.util.Request;
import com.lothrazar.storagenetwork.util.RequestBatch;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TileMain extends BlockEntity {

  //currently this has one network
  private NetworkModule nw = new NetworkModule();

  public TileMain(BlockPos pos, BlockState state) {
    super(SsnRegistry.Tiles.MASTER.get(), pos, state);
  }

  public NetworkModule getNetwork() {
    return nw;
  }

  @Override
  public CompoundTag getUpdateTag() {
    CompoundTag nbt = new CompoundTag();
    this.saveAdditional(nbt);
    return nbt;
  }

  @Override
  public ClientboundBlockEntityDataPacket getUpdatePacket() {
    saveWithFullMetadata();
    return ClientboundBlockEntityDataPacket.create(this);
  }

  @Override
  public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
    load(pkt.getTag() == null ? new CompoundTag() : pkt.getTag());
    super.onDataPacket(net, pkt);
  }

  /**
   * insert into my network
   */
  public int insertStack(ItemStack stack, boolean simulate) {
    int totalInserted = nw.insertStack(stack, simulate);
    //subnetwork ?
    return totalInserted;
  }

  /**
   * request from my network
   */
  public ItemStack request(ItemStackMatcher matcher, int size, boolean simulate) {
    ItemStack result = nw.request(matcher, size, simulate);
    //if not found then ? check other wireless / remote networks goes here?
    return result;
  }

  public void executeRequestBatch(RequestBatch batch) {
    if (batch == null) {
      return;
    }
    batch.sort();
    nw.executeRequestBatch(batch);
  }

  private DimPos getDimPos() {
    return new DimPos(level, worldPosition);
  }

  public void clearCache() {
    nw.ch.clearCache();
  }

  public static void clientTick(Level level, BlockPos blockPos, BlockState blockState, TileMain tile) {}

  public static <E extends BlockEntity> void serverTick(Level level, BlockPos blockPos, BlockState blockState, TileMain tile) {
    tile.tick();
  }

  private boolean isRunnable(IConnectable connectable) {
    if (connectable.needsRedstone()) {
      boolean hasPower = level.hasNeighborSignal(connectable.getPos().getBlockPos());
      if (!hasPower) {
        // if io tells me it needs redstone, and power is nothing, dont execute
        //instead go to the next entry in the loop
        return false; // needs redsone and does not have power
      }
    }
    return true;
  }

  /**
   * Huge refactor now all in one loop instead of three
   *
   * heavy lifting moved to the capabilities inside the cables, so the main node is no longer doing all the work of import cables and export cables
   *
   * Pull into the network from the relevant linked cables.
   *
   * After import, run export to push OUT of the network to attached export cables.
   *
   * Finally run processing at the end
   */
  private void tick() {
    if (level == null || level.isClientSide) {
      return;
    }
    refresh();
    RequestBatch requestBatch = new RequestBatch();
    int exportCableCount = 0;
    for (IConnectable connectable : nw.getConnectables()) {
      if (connectable == null || connectable.getPos() == null) {
        continue;
      }
      //does it have processing capability?
      //in practice it will not have both, its either IO or processing
      IConnectableItemProcessing processingCap = connectable.getPos().getCapability(StorageNetworkCapabilities.PROCESSING_CAPABILITY, null);
      if (processingCap != null && isRunnable(connectable)) {
        processingCap.execute(this);
      }
      //now  try running all import and export capabilities, known as IO
      IConnectableItemAutoIO ioCap = connectable.getPos().getCapability(StorageNetworkCapabilities.CONNECTABLE_AUTO_IO, null);
      if (ioCap != null && isRunnable(connectable)) {
        // Give the storage a chance to have a cooldown or other conditions that prevent it from running
        if (!ioCap.canRunNow(connectable.getPos(), this)) {
          continue; // go to the next entry in the loop
        }
        //it has IO, so run imports and then exports
        if (ioCap.ioDirection() == EnumStorageDirection.IN) {
          ioCap.runImport(this);
        }
        if (ioCap.ioDirection() == EnumStorageDirection.OUT) {
          exportCableCount++;
          RequestBatch cableBatch = ioCap.runExport(this);
          StorageNetworkMod.log("Export cable #" + exportCableCount + " at " + connectable.getPos().getBlockPos() + " created batch with " + (cableBatch == null ? "0" : cableBatch.size()) + " item types");
          if (cableBatch != null) {
            // Merge this cable's requests into the accumulated batch
            for (Item item : cableBatch.keySet()) {
              List<Request> requests = cableBatch.get(item);
              if (requests != null) {
                for (Request request : requests) {
                  requestBatch.put(item, request);
                }
              }
            }
          }
        }
      }
    }
    // Execute all accumulated export requests at once, properly sorted by priority
    if (!requestBatch.isEmpty()) {
      StorageNetworkMod.log("Executing accumulated batch with " + requestBatch.size() + " item types from " + exportCableCount + " cables");
    }
    executeRequestBatch(requestBatch);
  }

  private void refresh() {
    if (level == null) return;
    //refresh time in config, default 200 ticks aka 10 seconds
    if ((level.getGameTime() % StorageNetworkMod.CONFIG.refreshTicks() == 0)
        || nw.shouldRefresh()) {
      nw.doRefresh(this.getDimPos());
    }
  }
}
