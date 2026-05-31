package com.lothrazar.storagenetwork.block.receiver;

import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.api.UpgradeType;
import com.lothrazar.storagenetwork.block.TileConnectable;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public class TileNetworkReceiver extends TileConnectable implements MenuProvider {

  private DimPos boundMaster;
  private boolean registeredWithMaster = false;
  // Own-chunk ticket lives in TileConnectable (chunkTicketHeld / heldChunk).
  // Only the cross-dim master-chunk ticket is receiver-specific.
  private boolean masterTicketHeld = false;
  private ChunkPos heldMasterChunk;
  private String heldMasterDim;

  private final ItemStackHandler upgrades = new ItemStackHandler(1) {

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
      return UpgradeType.isUpgradeOfType(stack.getItem(), UpgradeType.CHUNKLOAD);
    }

    @Override
    public int getSlotLimit(int slot) {
      return 1;
    }

    @Override
    protected void onContentsChanged(int slot) {
      setChanged();
      if (level != null && !level.isClientSide) {
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
      }
    }
  };

  public TileNetworkReceiver(BlockPos pos, BlockState state) {
    super(SsnRegistry.Tiles.RECEIVER.get(), pos, state);
  }

  public DimPos getBoundMaster() {
    return boundMaster;
  }

  public void setBoundMaster(DimPos master) {
    this.boundMaster = master;
    this.registeredWithMaster = false;
    setChanged();
  }

  public ItemStackHandler getUpgrades() {
    return upgrades;
  }

  public boolean hasChunkloadUpgrade() {
    ItemStack s = upgrades.getStackInSlot(0);
    return !s.isEmpty() && UpgradeType.isUpgradeOfType(s.getItem(), UpgradeType.CHUNKLOAD);
  }

  public boolean isBound() {
    return boundMaster != null;
  }

  public boolean isActive() {
    if (!isBound()) {
      return false;
    }
    TileMain master = resolveMaster();
    return master != null;
  }

  private TileMain resolveMaster() {
    if (boundMaster == null || level == null) {
      return null;
    }
    if (boundMaster.getWorld() == null) {
      boundMaster.setWorld(level);
    }
    return boundMaster.getTileEntity(TileMain.class, level);
  }

  public static void clientTick(Level level, BlockPos pos, BlockState state, TileNetworkReceiver tile) {}

  public static void serverTick(Level level, BlockPos pos, BlockState state, TileNetworkReceiver tile) {
    tile.tick();
  }

  private void tick() {
    if (level == null || level.isClientSide || boundMaster == null) {
      return;
    }
    // Throttle: receivers don't need per-tick logic; once a second is plenty for register/ticket housekeeping.
    if (level.getGameTime() % 20L != 0L) {
      return;
    }
    TileMain master = resolveMaster();
    if (master != null && !registeredWithMaster) {
      master.registerReceiver(new DimPos(level, worldPosition));
      registeredWithMaster = true;
    }
    if (master == null) {
      registeredWithMaster = false;
    }
    updateChunkTickets();
  }

  private void updateChunkTickets() {
    boolean want = hasChunkloadUpgrade() && isBound();
    // Own chunk - delegate to TileConnectable's shared ticket logic.
    updateChunkloadTicket(want);
    // Master chunk - receiver-specific because it lives in a different level.
    // Re-assert every tick rather than gating on masterTicketHeld: vanilla
    // updateChunkForced is idempotent (LongSet add), and the persisted
    // masterTicketHeld flag can desync from ForcedChunksSavedData across
    // server restarts (heldMasterChunk / heldMasterDim aren't saved). If we
    // trusted the flag we'd skip re-adding and leave the master unloaded.
    Level masterLevel = boundMaster != null ? boundMaster.resolveLevel() : null;
    if (want && masterLevel instanceof ServerLevel msl) {
      ChunkPos cp = new ChunkPos(boundMaster.getBlockPos());
      msl.getChunkSource().updateChunkForced(cp, true);
      boolean masterChunkLoaded = msl.hasChunk(cp.x, cp.z);
      if (!masterTicketHeld || heldMasterChunk == null || heldMasterDim == null) {
        masterTicketHeld = true;
        heldMasterChunk = cp;
        heldMasterDim = boundMaster.getDimension();
        setChanged();
        LOGGER.info("Receiver @ {} ({}) asserting MASTER chunk ticket on {} @ {} (chunkLoadedNow={})",
            worldPosition, level.dimension().location(), cp, heldMasterDim, masterChunkLoaded);
      }
      else if (!masterChunkLoaded) {
        // Ticket present but chunk not actually loaded - log so we can see if
        // updateChunkForced is failing to translate into an actual chunk load.
        LOGGER.warn("Receiver @ {} ({}): master ticket asserted on {} @ {} but chunk reports NOT loaded",
            worldPosition, level.dimension().location(), cp, heldMasterDim);
      }
    }
    else if (!want && masterTicketHeld) {
      releaseMasterTicket();
    }
  }

  private void releaseMasterTicket() {
    if (!masterTicketHeld) {
      return;
    }
    if (heldMasterDim != null && heldMasterChunk != null && level != null && level.getServer() != null) {
      ServerLevel sl = DimPos.stringDimensionLookup(heldMasterDim, level.getServer());
      if (sl != null) {
        sl.getChunkSource().updateChunkForced(heldMasterChunk, false);
      }
    }
    masterTicketHeld = false;
    heldMasterChunk = null;
    heldMasterDim = null;
  }

  /**
   * Called by the block on actual removal (break / replace), NOT on chunk unload.
   * Doing this work in setRemoved caused world-save hangs because chunk unload
   * during shutdown would mutate the master's level's persistent forced-chunks
   * state mid-save.
   */
  public void onBlockBroken() {
    if (level == null || level.isClientSide) {
      return;
    }
    TileMain master = resolveMaster();
    if (master != null) {
      master.unregisterReceiver(new DimPos(level, worldPosition));
    }
    releaseChunkTicket(); // own chunk, inherited
    releaseMasterTicket();
  }

  @Override
  public void onChunkUnloaded() {
    super.onChunkUnloaded();
    // Do not unregister - master should keep us in its list. We come back next load.
  }

  @Override
  protected void loadAdditional(CompoundTag compound, HolderLookup.Provider registries) {
    super.loadAdditional(compound, registries);
    if (compound.contains("boundMaster")) {
      boundMaster = new DimPos(compound.getCompound("boundMaster"));
    }
    if (compound.contains("upgrades")) {
      upgrades.deserializeNBT(registries, compound.getCompound("upgrades"));
    }
    masterTicketHeld = compound.getBoolean("masterTicketHeld");
    if (compound.contains("heldMasterDim")) {
      heldMasterDim = compound.getString("heldMasterDim");
    }
    if (compound.contains("heldMasterChunkX") && compound.contains("heldMasterChunkZ")) {
      heldMasterChunk = new ChunkPos(compound.getInt("heldMasterChunkX"), compound.getInt("heldMasterChunkZ"));
    }
  }

  @Override
  protected void saveAdditional(CompoundTag compound, HolderLookup.Provider registries) {
    super.saveAdditional(compound, registries);
    if (boundMaster != null) {
      compound.put("boundMaster", boundMaster.serializeNBT(registries));
    }
    compound.put("upgrades", upgrades.serializeNBT(registries));
    compound.putBoolean("masterTicketHeld", masterTicketHeld);
    if (heldMasterDim != null) {
      compound.putString("heldMasterDim", heldMasterDim);
    }
    if (heldMasterChunk != null) {
      compound.putInt("heldMasterChunkX", heldMasterChunk.x);
      compound.putInt("heldMasterChunkZ", heldMasterChunk.z);
    }
  }

  @Override
  public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
    return new ContainerNetworkReceiver(id, level, worldPosition, inv, player);
  }

  @Override
  public Component getDisplayName() {
    return Component.translatable("block." + StorageNetworkMod.MODID + ".receiver");
  }
}
