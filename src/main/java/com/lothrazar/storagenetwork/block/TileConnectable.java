package com.lothrazar.storagenetwork.block;

import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.api.ChunkLoadingTicket;
import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.api.EnumSortType;
import com.lothrazar.storagenetwork.api.UpgradeType;
import com.lothrazar.storagenetwork.api.capabilities.UpgradesItemStackHandler;
import com.lothrazar.storagenetwork.api.network.BlockEntityConnectableNode;
import com.lothrazar.storagenetwork.api.network.ConnectableNode;
import com.lothrazar.storagenetwork.api.network.ConnectableNodeDefault;
import com.lothrazar.storagenetwork.block.cable.CableHelpers;
import com.lothrazar.storagenetwork.block.main.TileMain;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Base class for Cable, Control, Request
 */
public abstract class TileConnectable extends BlockEntity implements BlockEntityConnectableNode {

  public static final Logger LOGGER = LogManager.getLogger();
  private final ConnectableNodeDefault connectable;
  // Forced-chunk ticket for this tile's own chunk. Used by any cable/connectable
  // that exposes a chunkload upgrade slot. The helper encapsulates the held flag,
  // chunk pos, dim, and the idempotent assert/release calls.
  protected final ChunkLoadingTicket ownChunk = new ChunkLoadingTicket();

  public TileConnectable(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
    super(tileEntityTypeIn, pos, state);
    connectable = new ConnectableNodeDefault();
  }

  @Override
  public ConnectableNode getConnectableNode() {
    return connectable;
  }

  public EnumSortType getSort() {
    return EnumSortType.NAME;
  }

  public boolean isDownwards() {
    return false;
  }

  @Override
  public void setChanged() {
    super.setChanged();
    connectable.setPos(new DimPos(level, worldPosition));
  }

  @Override
  protected void loadAdditional(CompoundTag compound, HolderLookup.Provider registries) {
    if (compound.contains("connectable")) {
      connectable.deserializeNBT(registries, compound.getCompound("connectable"));
    }
    ownChunk.load(compound, "chunkTicketHeld", "ownTicketDim", "ownTicketChunkX", "ownTicketChunkZ");
    super.loadAdditional(compound, registries);
  }

  @Override
  protected void saveAdditional(CompoundTag compound, HolderLookup.Provider registries) {
    compound.put("connectable", connectable.serializeNBT(registries));
    ownChunk.save(compound, "chunkTicketHeld", "ownTicketDim", "ownTicketChunkX", "ownTicketChunkZ");
    super.saveAdditional(compound, registries);
  }

  @Override
  public ClientboundBlockEntityDataPacket getUpdatePacket() {
    return ClientboundBlockEntityDataPacket.create(this);
  }

  @Override
  public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
    CompoundTag updateTag = new CompoundTag();
    this.saveAdditional(updateTag, registries);
    return updateTag;
  }

  @Override
  public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
    loadAdditional(pkt.getTag() == null ? new CompoundTag() : pkt.getTag(), registries);
  }

  @Override
  public void onChunkUnloaded() {
    super.onChunkUnloaded();
    if (StorageNetworkMod.CONFIG.doReloadOnChunk() && connectable != null && connectable.getMainPos() != null) {
      try {
        TileMain maybe = CableHelpers.getTileMainForConnectable(connectable);
        if (maybe != null) {
          maybe.getNetwork().setShouldRefresh();
        }
      }
      catch (Exception e) {
        LOGGER.error("Error on chunk unload {}", String.valueOf(e));
      }
    }
  }

  public DimPos getMain() {
    if (connectable == null) {
      return null;
    }
    return connectable.getMainPos();
  }

  /**
   * Convenience for cables that store their upgrades inside a capability handler.
   * Pass the handler from the cable's serverTick; the helper diffs the desired
   * state against actual ticket state and only mutates forced-chunks on change.
   * Cheap to call every tick if you want, but cables usually throttle.
   */
  public void tickChunkloadFor(UpgradesItemStackHandler upgrades) {
    boolean want = upgrades != null && upgrades.hasUpgradesOfType(UpgradeType.CHUNKLOAD);
    updateChunkloadTicket(want);
  }

  protected void updateChunkloadTicket(boolean want) {
    if (level == null || level.isClientSide || !(level instanceof ServerLevel sl)) {
      return;
    }
    if (want) {
      if (ownChunk.assertOn(sl, new ChunkPos(worldPosition))) {
        setChanged();
        LOGGER.debug("Asserting own-chunk forced ticket at {} in {}", ownChunk.chunkPos(), sl.dimension().location());
      }
    }
    else if (ownChunk.release(level.getServer())) {
      setChanged();
      LOGGER.debug("Released own-chunk forced ticket at {} in {}", new ChunkPos(worldPosition), sl.dimension().location());
    }
  }

  /**
   * Called from the cable block's onRemove path (real break, not chunk unload).
   * Releases any chunk ticket this tile is holding for itself.
   */
  public void releaseChunkTicket() {
    if (level != null) {
      ownChunk.release(level.getServer());
    }
    else {
      ownChunk.clear();
    }
  }
}
