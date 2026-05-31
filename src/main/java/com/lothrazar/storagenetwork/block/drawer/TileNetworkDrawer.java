package com.lothrazar.storagenetwork.block.drawer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.api.capabilities.ItemStackMatcherDefault;
import com.lothrazar.storagenetwork.block.TileConnectable;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TileNetworkDrawer extends TileConnectable {

  private static final Logger LOGGER = LogManager.getLogger();
  private static final String NBT_LOCKED = "lockedStack";
  private static final String NBT_CACHED = "cachedCount";
  private static final int POLL_INTERVAL = 10;

  private ItemStack lockedStack = ItemStack.EMPTY;
  private int cachedCount = 0;
  // Transient: per-player last punch tick for double-punch detection.
  private final Map<UUID, Long> lastRightClickTick = new HashMap<>();

  public TileNetworkDrawer(BlockPos pos, BlockState state) {
    super(SsnRegistry.Tiles.NETWORK_DRAWER.get(), pos, state);
  }

  public ItemStack getLockedStack() {
    return lockedStack;
  }

  public void setLockedStack(ItemStack stack) {
    this.lockedStack = stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1);
    this.cachedCount = 0;
    setChanged();
    syncToClient();
  }

  public int getCachedCount() {
    return cachedCount;
  }

  public Long getLastRightClickTick(UUID id) {
    return lastRightClickTick.get(id);
  }

  public void setLastRightClickTick(UUID id, long tick) {
    // Opportunistic cleanup of stale entries.
    if (lastRightClickTick.size() > 32) {
      lastRightClickTick.entrySet().removeIf(e -> (tick - e.getValue()) > 200L);
    }
    lastRightClickTick.put(id, tick);
  }

  public boolean isConnected() {
    return getTileMain() != null;
  }

  public TileMain getTileMain() {
    TileMain main = resolveCachedMain();
    if (main != null) {
      return main;
    }
    // Master's periodic refresh hasn't reached us yet (or this is a fresh placement).
    // Walk neighbors to find any connected node, adopt its master, and nudge that
    // master to rescan so its own connectables set picks us up too.
    return adoptMasterFromNeighbor();
  }

  private TileMain resolveCachedMain() {
    if (getMain() == null) {
      return null;
    }
    return getMain().getTileEntity(TileMain.class);
  }

  private TileMain adoptMasterFromNeighbor() {
    if (level == null || level.isClientSide) {
      return null;
    }
    for (Direction d : Direction.values()) {
      BlockEntity be = level.getBlockEntity(worldPosition.relative(d));
      if (!(be instanceof TileConnectable tc) || tc == this) {
        continue;
      }
      DimPos otherMain = tc.getMain();
      if (otherMain == null) {
        continue;
      }
      TileMain main = otherMain.getTileEntity(TileMain.class, level);
      if (main == null) {
        continue;
      }
      getConnectableNode().setMainPos(otherMain);
      main.getNetwork().setShouldRefresh();
      setChanged();
      return main;
    }
    return null;
  }

  public int insertIntoNetwork(ItemStack stack) {
    TileMain main = getTileMain();
    if (main == null || stack.isEmpty()) {
      LOGGER.info("[drawer-tile] insert aborted: main={} stackEmpty={}", main, stack.isEmpty());
      return stack.getCount();
    }
    int connectableCount = main.getNetwork().getConnectableSize();
    int beforeAmount = main.getNetwork().getAmount(new ItemStackMatcherDefault(stack));
    int leftover = main.insertStack(stack, false);
    int afterAmount = main.getNetwork().getAmount(new ItemStackMatcherDefault(stack));
    LOGGER.info("[drawer-tile] insert: connectables={} beforeAmt={} leftover={} afterAmt={} masterPos={}",
        connectableCount, beforeAmount, leftover, afterAmount, main.getBlockPos());
    return leftover;
  }

  public ItemStack extractFromNetwork(int count) {
    TileMain main = getTileMain();
    if (main == null || lockedStack.isEmpty() || count <= 0) {
      return ItemStack.EMPTY;
    }
    return main.request(new ItemStackMatcherDefault(lockedStack), count, false);
  }

  public static void clientTick(Level level, BlockPos pos, BlockState state, TileNetworkDrawer tile) {}

  public static void serverTick(Level level, BlockPos pos, BlockState state, TileNetworkDrawer tile) {
    if ((level.getGameTime() % POLL_INTERVAL) != 0) {
      return;
    }
    int newCount = 0;
    if (!tile.lockedStack.isEmpty()) {
      TileMain main = tile.getTileMain();
      if (main != null) {
        newCount = main.getNetwork().getAmount(new ItemStackMatcherDefault(tile.lockedStack));
      }
    }
    if (newCount != tile.cachedCount) {
      tile.cachedCount = newCount;
      tile.setChanged();
      tile.syncToClient();
    }
  }

  private void syncToClient() {
    if (level != null && !level.isClientSide) {
      level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }
  }

  @Override
  protected void loadAdditional(CompoundTag compound, HolderLookup.Provider registries) {
    super.loadAdditional(compound, registries);
    if (compound.contains(NBT_LOCKED)) {
      lockedStack = ItemStack.parseOptional(registries, compound.getCompound(NBT_LOCKED));
    }
    else {
      lockedStack = ItemStack.EMPTY;
    }
    cachedCount = compound.getInt(NBT_CACHED);
  }

  @Override
  protected void saveAdditional(CompoundTag compound, HolderLookup.Provider registries) {
    super.saveAdditional(compound, registries);
    if (!lockedStack.isEmpty()) {
      compound.put(NBT_LOCKED, lockedStack.save(registries, new CompoundTag()));
    }
    compound.putInt(NBT_CACHED, cachedCount);
  }
}
