package com.lothrazar.storagenetwork.block;

import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.api.EnumSortType;
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
    super.loadAdditional(compound, registries);
  }

  @Override
  protected void saveAdditional(CompoundTag compound, HolderLookup.Provider registries) {
    compound.put("connectable", connectable.serializeNBT(registries));
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
        LOGGER.info("Error on chunk unload " + e);
      }
    }
  }

  public DimPos getMain() {
    if (connectable == null) {
      return null;
    }
    return connectable.getMainPos();
  }
}
