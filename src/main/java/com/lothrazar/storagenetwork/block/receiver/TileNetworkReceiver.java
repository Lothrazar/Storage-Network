package com.lothrazar.storagenetwork.block.receiver;

import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.api.UpgradeType;
import com.lothrazar.storagenetwork.api.ChunkLoadingTicket;
import com.lothrazar.storagenetwork.block.TileConnectable;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.registry.ConfigRegistry;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.items.ItemStackHandler;

public class TileNetworkReceiver extends TileConnectable implements MenuProvider {

  private DimPos boundMaster;
  private boolean registeredWithMaster = false;
  // Own-chunk ticket lives in TileConnectable (ownChunk). The cross-dim
  // master-chunk ticket is receiver-specific because it lives on a foreign level.
  private final ChunkLoadingTicket masterChunk = new ChunkLoadingTicket();

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
      if (level != null && !level.isClientSide()) {
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
    if (level == null || level.isClientSide() || boundMaster == null) {
      return;
    }
    // Throttle: receivers don't need per-tick logic; once a second is plenty for register/ticket housekeeping.
    if (level.getGameTime() % ConfigRegistry.CHUNKLOADER_REFRESH_TICKS.get() != 0L) {
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
    // Master chunk - cross-dim, so we use the ChunkLoadingTicket helper.
    Level masterLevel = boundMaster != null ? boundMaster.resolveLevel() : null;
    if (want && masterLevel instanceof ServerLevel msl) {
      ChunkPos cp = ChunkPos.containing(boundMaster.getBlockPos());
      boolean newlyHeld = masterChunk.assertOn(msl, cp);
      if (newlyHeld) {
        setChanged();
        LOGGER.debug("Receiver @ {} ({}) asserting MASTER chunk ticket on {} @ {} (chunkLoadedNow={})",
            worldPosition, level.dimension().identifier(), cp, masterChunk.dim(), msl.hasChunk(cp.x(), cp.z()));
      }
      else if (!msl.hasChunk(cp.x(), cp.z())) {
        // Ticket present but chunk not actually loaded - flag so we can see if
        // updateChunkForced is failing to translate into an actual chunk load.
        LOGGER.warn("Receiver @ {} ({}): master ticket asserted on {} @ {} but chunk reports NOT loaded",
            worldPosition, level.dimension().identifier(), cp, masterChunk.dim());
      }
    }
    else if (!want && masterChunk.isHeld() && level != null) {
      if (masterChunk.release(level.getServer())) {
        setChanged();
      }
    }
  }

  /**
   * Called by the block on actual removal (break / replace), NOT on chunk unload.
   * Doing this work in setRemoved caused world-save hangs because chunk unload
   * during shutdown would mutate the master's level's persistent forced-chunks
   * state mid-save.
   */
  public void onBlockBroken() {
    if (level == null || level.isClientSide()) {
      return;
    }
    TileMain master = resolveMaster();
    if (master != null) {
      master.unregisterReceiver(new DimPos(level, worldPosition));
    }
    releaseChunkTicket(); // own chunk, inherited
    masterChunk.release(level.getServer());
  }

  @Override
  public void onChunkUnloaded() {
    super.onChunkUnloaded();
    // Do not unregister - master should keep us in its list. We come back next load.
  }

  // Block#onRemove is gone in 26.1; the block-actually-changed gating that used to live in
  // BlockNetworkReceiver#onRemove is now done by the caller before this is even invoked.
  @Override
  public void preRemoveSideEffects(BlockPos pos, BlockState state) {
    super.preRemoveSideEffects(pos, state);
    onBlockBroken();
  }

  @Override
  protected void loadAdditional(ValueInput input) {
    super.loadAdditional(input);
    input.child("boundMaster").ifPresent(child -> boundMaster = DimPos.of(child));
    input.child("upgrades").ifPresent(upgrades::deserialize);
    masterChunk.load(input, "masterTicketHeld", "heldMasterDim", "heldMasterChunkX", "heldMasterChunkZ");
  }

  @Override
  protected void saveAdditional(ValueOutput output) {
    super.saveAdditional(output);
    if (boundMaster != null) {
      boundMaster.serialize(output.child("boundMaster"));
    }
    upgrades.serialize(output.child("upgrades"));
    masterChunk.save(output, "masterTicketHeld", "heldMasterDim", "heldMasterChunkX", "heldMasterChunkZ");
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
