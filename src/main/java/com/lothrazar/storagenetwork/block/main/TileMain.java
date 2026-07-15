package com.lothrazar.storagenetwork.block.main;

import com.google.common.collect.Lists;
import com.lothrazar.storagenetwork.StorageNetwork;
import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.api.EnumStorageDirection;
import com.lothrazar.storagenetwork.api.IConnectable;
import com.lothrazar.storagenetwork.api.IConnectableItemAutoIO;
import com.lothrazar.storagenetwork.api.IConnectableLink;
import com.lothrazar.storagenetwork.api.IItemStackMatcher;
import com.lothrazar.storagenetwork.block.exchange.TileExchange;
import com.lothrazar.storagenetwork.capability.handler.ItemStackMatcher;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import com.lothrazar.storagenetwork.registry.StorageNetworkCapabilities;
import com.lothrazar.storagenetwork.util.UtilInventory;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.state.properties.ChestType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class TileMain extends TileEntity implements ITickableTileEntity {

  private Set<DimPos> connectables;
  private Map<String, DimPos> importCache = new HashMap<>();
  private boolean shouldRefresh = true;

  private DimPos getDimPos() {
    return new DimPos(world, pos);
  }

  public TileMain() {
    super(SsnRegistry.MAINTILEENTITY);
  }

  public List<ItemStack> getSortedStacks() {
    List<ItemStack> stacks = Lists.newArrayList();
    try {
      if (getConnectablePositions() == null) {
        refreshNetwork();
      }
    }
    catch (Exception e) {
      //since this has external mod connections, if they break then catch it
      //      for example, AE2 can break with  Ticking GridNode
      StorageNetwork.LOGGER.info("3rd party storage mod has an error", e);
    }
    try {
      for (IConnectableLink storage : getSortedConnectableStorage()) {
        for (ItemStack stack : storage.getStoredStacks(true)) {
          if (stack == null || stack.isEmpty()) {
            continue;
          }
          addOrMergeIntoList(stacks, stack);
        }
      }
    }
    catch (Exception e) {
      StorageNetwork.LOGGER.info("3rd party storage mod has an error", e);
    }
    return stacks;
  }

  public List<ItemStack> getStacks() {
    List<ItemStack> stacks = Lists.newArrayList();
    try {
      if (getConnectablePositions() == null) {
        refreshNetwork();
      }
    }
    catch (Exception e) {
      //since this has external mod connections, if they break then catch it
      //      for example, AE2 can break with  Ticking GridNode
      StorageNetwork.LOGGER.info("3rd party storage mod has an error", e);
    }
    try {
      for (IConnectableLink storage : getConnectableStorage()) {
        for (ItemStack stack : storage.getStoredStacks(true)) {
          if (stack == null || stack.isEmpty()) {
            continue;
          }
          addOrMergeIntoList(stacks, stack);
        }
      }
    }
    catch (Exception e) {
      StorageNetwork.LOGGER.info("3rd party storage mod has an error", e);
    }
    return stacks;
  }

  private static void addOrMergeIntoList(List<ItemStack> list, ItemStack stackToAdd) {
    boolean added = false;
    for (ItemStack stack : list) {
      if (ItemHandlerHelper.canItemStacksStack(stackToAdd, stack)) {
        stack.setCount(stack.getCount() + stackToAdd.getCount());
        added = true;
        break;
      }
    }
    if (!added) {
      list.add(stackToAdd);
    }
  }

  int emptySlots() {
    int countEmpty = 0;
    for (IConnectableLink storage : getSortedConnectableStorage()) {
      countEmpty += storage.getEmptySlots();
    }
    return countEmpty;
  }

  public int getAmount(ItemStackMatcher fil) {
    if (fil == null) {
      return 0;
    }
    int totalCount = 0;
    for (ItemStack stack : getStacks()) {
      if (!fil.match(stack)) {
        continue;
      }
      totalCount += stack.getCount();
    }
    return totalCount;
  }

  @Override
  public CompoundNBT getUpdateTag() {
    return write(new CompoundNBT());
  }

  /**
   * This is a recursively called method that traverses all connectable blocks and stores them in this tiles connectables list.
   *
   * @param sourcePos
   */
  private Set<DimPos> getConnectables(DimPos sourcePos) {
    HashSet<DimPos> result = new HashSet<>();
    addConnectables(sourcePos, result);
    return result;
  }

  private void addConnectables(DimPos sourcePos, Set<DimPos> set) {
    if (sourcePos == null || sourcePos.getWorld() == null || !sourcePos.isLoaded()) {
      return;
    }
    // Look in all directions
    for (Direction direction : Direction.values()) {
      DimPos lookPos = sourcePos.offset(direction);
      if (!lookPos.isLoaded()) {
        continue;
      }
      IChunk chunk = lookPos.getChunk();
      if (chunk == null) {
        continue;
      }
      // Prevent having multiple  on a network and break all others.
      TileMain maybeMain = lookPos.getTileEntity(TileMain.class);
      if (maybeMain != null && !lookPos.equals(world, pos)) {
        nukeAndDrop(lookPos);
        continue;
      }
      TileEntity tileHere = lookPos.getTileEntity(TileEntity.class);
      if (tileHere == null) {
        continue;
      }
      IConnectable capabilityConnectable = tileHere.getCapability(StorageNetworkCapabilities.CONNECTABLE_CAPABILITY, direction.getOpposite()).orElse(null);
      if (capabilityConnectable == null) {
        continue;
      }
      if (capabilityConnectable.getPos() == null) {
        //  1.15 hax ("1.15 HAX NULL POS !! " + lookPos + "has tile " + tileHere);
        capabilityConnectable.setPos(lookPos);
        capabilityConnectable.setMainPos(this.getDimPos());
      }
      if (capabilityConnectable != null) {
        capabilityConnectable.setMainPos(getDimPos());
        DimPos realConnectablePos = capabilityConnectable.getPos();
        boolean beenHereBefore = set.contains(realConnectablePos);
        if (beenHereBefore) {
          continue;
        }
        if (realConnectablePos.getWorld() == null) {
          // StorageNetwork.LOGGER.info("1.15 realConnectablePos HAX NULL WORLD  " + realConnectablePos);
          realConnectablePos.setWorld(sourcePos.getWorld());
        }
        set.add(realConnectablePos);
        addConnectables(realConnectablePos, set);
        tileHere.markDirty();
        chunk.setModified(true);
      }
    }
  }

  private static void nukeAndDrop(DimPos lookPos) {
    lookPos.getWorld().destroyBlock(lookPos.getBlockPos(), true);
    lookPos.getWorld().removeTileEntity(lookPos.getBlockPos());
  }

  public static boolean isTargetAllowed(BlockState state) {
    if (state.getBlock() == Blocks.AIR) {
      return false;
    }
    String blockId = state.getBlock().getRegistryName().toString();
    for (String s : StorageNetwork.CONFIG.ignorelist()) {
      if (blockId.equals(s)) {
        return false;
      }
    }
    return true;
  }

  public void refreshNetwork() {
    if (world.isRemote) {
      return;
    }
    shouldRefresh = true;
  }

  private boolean hasCachedSlot(ItemStack stack) {
    return importCache.containsKey(getStackKey(stack));
  }

  private DimPos getCachedSlot(ItemStack stack) {
    return importCache.get(getStackKey(stack));
  }

  /**
   * returns countUnmoved , the number of items NOT inserted.
   */
  public int insertStack(ItemStack stack, boolean simulate) {
    if (stack.isEmpty()) {
      return 0;
    }
    // 1. Try to insert into a recent slot for the same item.
    //    We do this to avoid having to search for the appropriate inventory repeatedly.
    String key = getStackKey(stack);
    if (hasCachedSlot(stack)) {
      DimPos cachedStoragePos = getCachedSlot(stack);
      IConnectableLink storage = cachedStoragePos.getCapability(StorageNetworkCapabilities.CONNECTABLE_ITEM_STORAGE_CAPABILITY, null);
      if (storage == null) {
        // The block at the cached position is not even an IConnectableLink anymore
        importCache.remove(key);
      }
      else {
        // But if it is, we test whether it can still import that particular stack and do so if it does.
        boolean canStillImport = storage.getSupportedTransferDirection().match(EnumStorageDirection.IN);
        if (canStillImport && storage.insertStack(stack, true).getCount() < stack.getCount()) {
          stack = storage.insertStack(stack, simulate);
        }
        else {
          importCache.remove(key);
        }
      }
    }
    // 2. If everything got transferred into the cached storage, end here
    if (stack.isEmpty()) {
      return 0;
    }
    // 3. Otherwise try to find a new inventory that can take the remainder of the itemstack
    List<IConnectableLink> storages = getSortedConnectableStorage();
    for (IConnectableLink storage : storages) {
      try {
        // Ignore storages that can not import
        if (!storage.getSupportedTransferDirection().match(EnumStorageDirection.IN)) {
          continue;
        }
        // The given import-capable storage can not import this particular stack
        if (storage.insertStack(stack, true).getCount() >= stack.getCount()) {
          continue;
        }
        // If it can we need to know, i.e. store the remainder
        stack = storage.insertStack(stack, simulate);
      }
      catch (Exception e) {
        StorageNetwork.LOGGER.error("insertStack container issue", e);
      }
    }
    return stack.getCount();
  }

  private static String getStackKey(ItemStack stackInCopy) {
    return stackInCopy.getItem().getRegistryName().toString();
  }

  /**
   * Pull into the network from the relevant linked cables
   */
  private void updateImports() {
    Set<IConnectable> conSet = getConnectables();
    for (IConnectable connectable : conSet) {
      if (connectable == null || connectable.getPos() == null) {
        continue;
      }
      IConnectableItemAutoIO storage = connectable.getPos().getCapability(StorageNetworkCapabilities.CONNECTABLE_AUTO_IO, null);
      if (storage == null) {
        continue;
      }
      //
      // We explicitely don't want to check whether this can do BOTH, because we don't
      // want to import what we've just exported in updateExports().
      if (storage.ioDirection() != EnumStorageDirection.IN) {
        continue;
      }
      // Give the storage a chance to have a cooldown or other conditions that prevent it from running
      if (!storage.runNow(connectable.getPos(), this)) {
        continue;
      }
      if (storage.needsRedstone()) {
        boolean power = world.isBlockPowered(connectable.getPos().getBlockPos());
        if (power == false) {
          continue;
        }
      }
      // Do a simulation first and abort if we got an empty stack,
      IItemHandler itemHandler = storage.getItemHandler();
      if (itemHandler == null) {
        continue;
      }
      for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
        ItemStack stack = itemHandler.getStackInSlot(slot);
        if (stack == null || stack.isEmpty()) {
          continue;
        }
        // Ignore stacks that are filtered
        if (storage.getFilters() == null || !storage.getFilters().isStackFiltered(stack)) {
          int extractSize = Math.min(storage.getTransferRate(), stack.getCount());
          ItemStack stackToImport = itemHandler.extractItem(slot, extractSize, true); //simulate to grab a reference
          if (stackToImport.isEmpty()) {
            continue; //continue back to itemHandler
          }
          // Then try to insert the stack into this masters network and store the number of remaining items in the stack
          int countUnmoved = this.insertStack(stackToImport, true);
          // Calculate how many items in the stack actually got moved
          int countMoved = stackToImport.getCount() - countUnmoved;
          if (countMoved <= 0) {
            continue; //continue back to itemHandler
          }
          // Alright, simulation says we're good, let's do it!
          // First extract from the storage
          ItemStack actuallyExtracted = itemHandler.extractItem(slot, countMoved, false); // storage.extractNextStack(countMoved, false);
          //          storage.getPos().getChunk().markDirty();
          // Then insert into our network
          this.insertStack(actuallyExtracted, false);
          break; // break out of itemHandler loop, done processing this cable, so move to next
        } //end of checking on filter for this stack
      }
      //
      // Then try to insert the stack into this network and store the number of remaining items in the stack
      //      int countUnmoved = insertStack(stack, true);
      //      // Calculate how many items in the stack actually got moved
      //      int countMoved = stack.getCount() - countUnmoved;
      //      if (countMoved <= 0) {
      //        continue;
      //      }
      //      // Alright, simulation says we're good, let's do it!
      //      // First extract from the storage
      //      ItemStack actuallyExtracted = storage.extractNextStack(countMoved, false);
      //      connectable.getPos().getWorld().getChunkAt(connectable.getPos().getBlockPos()).markDirty();
      //      // Then insert into our network
      //      insertStack(actuallyExtracted, false);
    }
  }

  private void updateProcess() {
    //    for (IConnectable connectable : getConnectables()) {
    //    if (connectable == null || connectable.getPos() == null) {
    //      //        StorageNetwork.log("null connectable or pos : updateProcess() ");
    //      continue;
    //    }
    //      TileCableProcess cableProcess = connectable.getPos().getTileEntity(TileCableProcess.class);
    //      if (cableProcess == null) {
    //        continue;
    //      }
    //      cableProcess.run();
    //    }
  }

  /**
   * push OUT of the network to attached export cables
   */
  private void updateExports() {
    Set<IConnectable> getCon = getConnectables();
    for (IConnectable connectable : getCon) {
      if (connectable == null || connectable.getPos() == null) {
        //        StorageNetwork.log("null connectable or pos : updateExports() ");
        continue;
      }
      IConnectableItemAutoIO storage = connectable.getPos().getCapability(StorageNetworkCapabilities.CONNECTABLE_AUTO_IO, null);
      if (storage == null) {
        continue;
      }
      // We explicitely don't want to check whether this can do BOTH, because we don't
      // want to import what we've just exported in updateExports().
      if (storage.ioDirection() != EnumStorageDirection.OUT) {
        continue;
      }
      // Give the storage a chance to have a cooldown
      if (!storage.runNow(connectable.getPos(), this)) {
        continue;
      }
      if (storage.needsRedstone()) {
        boolean power = world.isBlockPowered(connectable.getPos().getBlockPos());
        if (power == false) {
          continue;
        }
      }
      for (IItemStackMatcher matcher : storage.getAutoExportList()) {
        boolean stockMode = storage.isStockMode();
        int amtToRequest = storage.getTransferRate();
        if (stockMode) {
          try {
            TileEntity tileEntity = world.getTileEntity(connectable.getPos().getBlockPos().offset(storage.facingInventory()));
            IItemHandler targetInventory = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP).orElse(null);
            //request with false to see how many even exist in there.  
            int stillNeeds = UtilInventory.containsAtLeastHowManyNeeded(targetInventory, matcher.getStack(), matcher.getStack().getCount());
            if (stillNeeds == 0) {
              continue;
            }
            //  StorageNetwork.log("updateExports: amtToRequest " + amtToRequest);
            amtToRequest = Math.min(stillNeeds, amtToRequest);
          }
          catch (Throwable e) {
            StorageNetwork.LOGGER.error("Error thrown from a connected block" + e);
          }
        }
        if (matcher.getStack().isEmpty() || amtToRequest == 0) {
          continue;
        }
        ItemStack requestedStack = this.request((ItemStackMatcher) matcher, amtToRequest, true);
        if (requestedStack.isEmpty()) {
          continue;
        }
        // The stack is available in the network, let's simulate inserting it into the storage
        ItemStack insertedSim = storage.insertStack(requestedStack, true);
        // Determine the amount of items moved in the stack 
        if (!insertedSim.isEmpty()) {
          int movedItems = requestedStack.getCount() - insertedSim.getCount();
          if (movedItems <= 0) {
            continue;
          }
          requestedStack.setCount(movedItems);
        }
        // Alright, some items got moved in the simulation. Let's do it for real this time.
        ItemStack realExtractedStack = request(new ItemStackMatcher(requestedStack, false, true), requestedStack.getCount(), false);
        if (realExtractedStack.isEmpty()) {
          continue;
        }
        storage.insertStack(realExtractedStack, false);
        break;
      }
    }
  }

  public ItemStack request(ItemStackMatcher matcher, int size, boolean simulate) {
    if (size == 0 || matcher == null) {
      return ItemStack.EMPTY;
    }
    // TODO: Test against storage drawers. There was some issue with it: https://github.com/PrinceOfAmber/Storage-Network/issues/19
    IItemStackMatcher usedMatcher = matcher;
    int alreadyTransferred = 0;
    for (IConnectableLink storage : getSortedConnectableStorage()) {
      int req = size - alreadyTransferred;
      ItemStack simExtract = storage.extractStack(usedMatcher, req, simulate);
      if (simExtract.isEmpty()) {
        continue;
      }
      // Do not stack items of different types together, i.e. make the filter rules more strict for all further items
      usedMatcher = new ItemStackMatcher(simExtract, matcher.isOre(), matcher.isNbt());
      alreadyTransferred += simExtract.getCount();
      if (alreadyTransferred >= size) {
        break;
      }
    }
    if (alreadyTransferred <= 0) {
      return ItemStack.EMPTY;
    }
    return ItemHandlerHelper.copyStackWithSize(usedMatcher.getStack(), alreadyTransferred);
  }

  private Set<IConnectable> getConnectables() {
    Set<DimPos> positions = getConnectablePositions();
    if (positions == null) {
      return new HashSet<>();
    }
    Set<IConnectable> result = new HashSet<>();
    for (DimPos pos : positions) {
      if (!pos.isLoaded()) {
        continue;
      }
      TileEntity tileEntity = pos.getTileEntity(TileEntity.class);
      if (tileEntity == null) {
        continue;
      }
      IConnectable cap = tileEntity.getCapability(StorageNetworkCapabilities.CONNECTABLE_CAPABILITY, null).orElse(null);
      if (cap == null) {
        StorageNetwork.LOGGER.info("Somehow stored a dimpos that is not connectable... Skipping " + pos);
        continue;
      }
      result.add(cap);
    }
    return result;
  }

  private List<IConnectableLink> getSortedConnectableStorage() {
    try {
      Set<IConnectableLink> storage = getConnectableStorage();
      Stream<IConnectableLink> stream = storage.stream();
      List<IConnectableLink> sorted = stream.sorted(Comparator.comparingInt(IConnectableLink::getPriority)).collect(Collectors.toList());
      return sorted;
    }
    catch (Exception e) {
      //trying to avoid 
      //java.lang.StackOverflowError: Ticking block entity
      //and similar issues
      StorageNetwork.LOGGER.error("Error: network get sorted by priority error, some network components are disconnected ", e);
      return new ArrayList<>();
    }
  }

  private Set<IConnectableLink> getConnectableStorage() {
    // Deterministic order: sort by the connectable's own position so when two
    // link cables aim at the same inventory we always pick the same winner
    // (no flicker between refreshes).
    List<DimPos> ordered = new ArrayList<>(getConnectablePositions());
    ordered.sort(Comparator
        .comparing((DimPos d) -> d.getDimension() == null ? "" : d.getDimension())
        .thenComparingLong(d -> d.getBlockPos() == null ? 0L : d.getBlockPos().toLong()));
    Set<IConnectableLink> result = new HashSet<>();
    Set<DimPos> seenTargets = new HashSet<>();
    for (DimPos dimpos : ordered) {
      if (!dimpos.isLoaded()) {
        continue;
      }
      TileEntity tileEntity = dimpos.getTileEntity(TileEntity.class);
      if (tileEntity == null) {
        continue;
      }
      IConnectableLink capConnect = tileEntity.getCapability(StorageNetworkCapabilities.CONNECTABLE_ITEM_STORAGE_CAPABILITY, null).orElse(null);
      if (capConnect == null) {
        continue;
      }
      if (tileEntity instanceof TileExchange) {
        StorageNetwork.log("keep going??main tile exhchange bandaid");
        //        continue;
      }
      // Refuse a second storage cap that targets the same inventory another
      // already-accepted cap is targeting. Handles two link cables on one chest
      // and two link cables on the two halves of a double chest.
      DimPos targetKey = canonicalTargetKey(capConnect);
      if (targetKey != null && !seenTargets.add(targetKey)) {
        continue;
      }
      result.add(capConnect);
    }
    return result;
  }

  /**
   * Stable key identifying the underlying inventory a storage link reads/writes. Returns the target
   * DimPos, collapsing both halves of a vanilla double chest to the same canonical half. Returns null
   * when the link isn't bound to an external neighbor (processing, auto-io, etc) so it is never
   * deduped.
   */
  private static DimPos canonicalTargetKey(IConnectableLink link) {
    DimPos target = link.getTargetPos();
    if (target == null || target.getWorld() == null) {
      return target;
    }
    try {
      BlockState state = target.getBlockState();
      if (state.getBlock() instanceof ChestBlock && state.hasProperty(ChestBlock.TYPE)) {
        ChestType type = state.get(ChestBlock.TYPE);
        if (type != ChestType.SINGLE) {
          BlockPos here = target.getBlockPos();
          BlockPos other = here.offset(ChestBlock.getDirectionToAttached(state));
          BlockPos canonical = here.toLong() <= other.toLong() ? here : other;
          return new DimPos(target.getWorld(), canonical);
        }
      }
    }
    catch (Exception e) {
      // Defensive: chunk unloaded mid-refresh, etc. Fall through to raw target.
    }
    return target;
  }

  @Override
  public void tick() {
    if (world == null || world.isRemote) {
      return;
    }
    //refresh time in config, default 200 ticks aka 10 seconds
    if ((world.getGameTime() % StorageNetwork.CONFIG.refreshTicks() == 0)
        || shouldRefresh) {
      try {
        connectables = getConnectables(getDimPos());
        shouldRefresh = false;
        world.getChunk(pos).setModified(true);
      }
      catch (Throwable e) {
        StorageNetwork.LOGGER.info("Refresh network error ", e);
      }
    }
    updateImports();
    updateExports();
    updateProcess();
  }

  @Override
  public SUpdateTileEntityPacket getUpdatePacket() {
    CompoundNBT syncData = new CompoundNBT();
    write(syncData);
    return new SUpdateTileEntityPacket(pos, 1, syncData);
  }

  @Override
  public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
    read(this.getBlockState(), pkt.getNbtCompound());
  }

  public static boolean shouldRefresh(World world, BlockPos pos, BlockState oldState, BlockState newSate) {
    return oldState.getBlock() != newSate.getBlock();
  }

  /**
   * dont create an iterator over the original one that is being modified
   *
   * @return
   */
  public Set<DimPos> getConnectablePositions() {
    if (connectables == null) {
      connectables = new HashSet<>();
    }
    return new HashSet<>(connectables);
  }

  public void clearCache() {
    importCache = new HashMap<>();
  }
}
