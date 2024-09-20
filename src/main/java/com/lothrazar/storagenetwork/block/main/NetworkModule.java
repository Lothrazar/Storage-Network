package com.lothrazar.storagenetwork.block.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.google.common.collect.Lists;
import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.api.EnumStorageDirection;
import com.lothrazar.storagenetwork.api.IConnectable;
import com.lothrazar.storagenetwork.api.IConnectableLink;
import com.lothrazar.storagenetwork.api.IItemStackMatcher;
import com.lothrazar.storagenetwork.capability.handler.ItemStackMatcher;
import com.lothrazar.storagenetwork.registry.StorageNetworkCapabilities;
import com.lothrazar.storagenetwork.util.Batch;
import com.lothrazar.storagenetwork.util.RequestBatch;
import com.lothrazar.storagenetwork.util.StackProvider;
import com.lothrazar.storagenetwork.util.UtilInventory;
import com.lothrazar.storagenetwork.util.UtilTileEntity;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraftforge.items.ItemHandlerHelper;

/**
 * Responsible for network connection list, cache, requests and single inserts.
 * 
 * Not responsible for processing, import cables, or export cables
 * 
 * @author lothr
 *
 */
public class NetworkModule {

  NetworkCache ch = new NetworkCache();
  private Set<DimPos> connectables = new HashSet<>();
  private boolean shouldRefresh = true;

  /**
   * gets list of validated connected nodes, ignoring content or subtype
   * 
   * used by main block -> Import and Export cable
   * 
   * @return
   */
  public Set<IConnectable> getConnectables() {
    Set<DimPos> positions = new HashSet<>(connectables);
    Set<IConnectable> result = new HashSet<>();
    for (DimPos pos : positions) {
      if (!pos.isLoaded()) {
        continue;
      }
      BlockEntity tileEntity = pos.getTileEntity(BlockEntity.class);
      if (tileEntity == null) {
        continue;
      }
      IConnectable cap = tileEntity.getCapability(StorageNetworkCapabilities.CONNECTABLE_CAPABILITY, null).orElse(null);
      if (cap == null) {
        StorageNetworkMod.LOGGER.info("Somehow stored a dimpos that is not connectable... Skipping " + pos);
        continue;
      }
      result.add(cap);
    }
    return result;
  }

  public List<ItemStack> getStacks() {
    return getStacks(true);
  }

  /**
   * returns huge unsorted list of stacks
   * 
   * respects local node filter settings
   * 
   * @return
   */
  public List<ItemStack> getStacks(boolean isFiltered) {
    selfValidate();
    List<ItemStack> stacks = Lists.newArrayList();
    try {
      for (IConnectableLink storage : getConnectableStorage()) {
        //TODO: get Unsorted? 
        for (ItemStack stack : storage.getStoredStacks(isFiltered)) {
          if (stack == null || stack.isEmpty()) {
            continue;
          }
          UtilTileEntity.addOrMergeIntoList(stacks, stack);
        }
      }
    }
    catch (Exception e) {
      StorageNetworkMod.LOGGER.info("3rd party storage mod has an error", e);
    }
    return stacks;
  }

  /**
   * get sorted list of network contents. used by screen containers.
   * 
   * Nodes are sorted by priority, as well as relying on the capability, see IConenctableLink::getStoredStacks
   * 
   * respects local node filter settings
   * 
   * use getStacks() instead if no sort is needed
   * 
   * @return
   */
  public List<ItemStack> getSortedStacks() {
    selfValidate();
    boolean isFiltered = true;
    List<ItemStack> stacks = Lists.newArrayList();
    try {
      for (IConnectableLink storage : getSortedConnectableStorage()) {
        for (ItemStack stack : storage.getStoredStacks(isFiltered)) {
          if (stack == null || stack.isEmpty()) {
            continue;
          }
          UtilTileEntity.addOrMergeIntoList(stacks, stack);
        }
      }
    }
    catch (Exception e) {
      StorageNetworkMod.LOGGER.info("3rd party storage mod has an error", e);
    }
    return stacks;
  }

  /**
   * 
   * @param filter
   *          for the itemstack request
   * @return totalCount of how much the network contains that match this filter
   */
  public int getAmount(ItemStackMatcher filter) {
    if (filter == null) {
      return 0;
    }
    int totalCount = 0;
    for (ItemStack stack : getStacks()) {
      if (!filter.match(stack)) {
        continue;
      }
      totalCount += stack.getCount();
    }
    return totalCount;
  }

  /**
   * Perform refresh (regardless of shouldRefresh flag) and reset the flag back to off
   * 
   * @param masterPos
   */
  public void doRefresh(DimPos masterPos) {
    try {
      this.connectables = this.getConnectables(masterPos);
      this.shouldRefresh = false;
      masterPos.getWorld().getChunk(masterPos.getBlockPos()).setUnsaved(true);
    }
    catch (Throwable e) {
      StorageNetworkMod.LOGGER.info("Refresh network error ", e);
    }
  }

  /**
   * @return true if a refresh was requested
   */
  public boolean shouldRefresh() {
    return this.shouldRefresh;
  }

  /**
   * Set it to need an update at the next possible time. Use when nodes get added/removed/updated
   * 
   */
  public void setShouldRefresh() {
    shouldRefresh = true;
  }

  public int getConnectableSize() {
    return connectables == null ? 0 : connectables.size();
  }

  /**
   * 
   * @return total empty count for entire network
   */
  public int emptySlots() {
    int countEmpty = 0;
    for (IConnectableLink storage : getSortedConnectableStorage()) {
      countEmpty += storage.getEmptySlots();
    }
    return countEmpty;
  }

  /**
   * Insert contents into the network. Used by players and import cables.
   * 
   * Only able to use nodes with EnumStorageDirection.IN
   * 
   * always attempts to use the NetworkCache first, and keeps it updated
   * 
   * @param stack
   *          held by your mouse or whatever. object reference will be updated regardless of simulation
   * @param simulate
   *          true to test, false to execute
   * @return count of how many were inserted successfuly
   */
  public int insertStack(ItemStack stack, boolean simulate) {
    if (stack.isEmpty()) {
      return 0;
    }
    // 1. Try to insert into a recent slot for the same item.
    //    We do this to avoid having to search for the appropriate inventory repeatedly.
    String key = UtilInventory.getStackKey(stack);
    if (ch.hasCachedSlot(stack)) {
      DimPos cachedStoragePos = ch.getCachedSlot(stack);
      IConnectableLink storage = cachedStoragePos.getCapability(StorageNetworkCapabilities.CONNECTABLE_ITEM_STORAGE_CAPABILITY, null);
      if (storage == null) {
        // The block at the cached position is not even an IConnectableLink anymore
        ch.remove(key);
      }
      else {
        // But if it is, we test whether it can still import that particular stack and do so if it does.
        boolean canStillImport = storage.getSupportedTransferDirection().match(EnumStorageDirection.IN);
        if (canStillImport &&
            storage.insertStack(stack, true).getCount() < stack.getCount()) {
          stack = storage.insertStack(stack, simulate);
          StorageNetworkMod.log("cache success used on a insertStack " + key);
        }
        else {
          ch.remove(key);
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
        ch.put(key, storage.getPos());
      }
      catch (Exception e) {
        StorageNetworkMod.LOGGER.error("insertStack container issue", e);
      }
    }
    return stack.getCount();
  }

  /**
   * Request an item out of the network
   * 
   * @param matcher
   *          to filter for what you want exactly
   * @param size
   *          requested
   * @param simulate
   *          true for capability simulation, false to execute transaction
   * @return stack copy if simulated, the real stack if executed
   */
  public ItemStack request(ItemStackMatcher matcher, int size, boolean simulate) {
    if (size == 0 || matcher == null) {
      return ItemStack.EMPTY;
    }
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

  public void executeRequestBatch(RequestBatch batch) {
    Batch<StackProvider> availableItems = new Batch<StackProvider>();
    for (IConnectableLink storage : getSortedConnectableStorage()) {
      storage.addToStackProviderBatch(availableItems);
    }
    for (Item item : batch.keySet()) {
      List<StackProvider> availableStacks = availableItems.get(item);
      if (availableStacks != null) {
        for (StackProvider provider : availableStacks) {
          batch.extractStacks(provider.getStorage(), provider.getSlot(), item);
        }
      }
    }
  }

  /**
   * TODO: move or refactor?
   */
  public List<Entry<String, Integer>> getDisplayStrings() {
    Map<String, Integer> mapNamesToCount = new HashMap<>();
    Iterator<DimPos> iter = new HashSet<>(this.connectables).iterator();
    Block bl;
    DimPos p;
    String blockName;
    while (iter.hasNext()) {
      p = iter.next();
      bl = p.getBlockState().getBlock();
      //getTranslatedName client only thanks mojang lol
      blockName = (Component.literal(bl.getDescriptionId())).getString();
      int count = mapNamesToCount.get(blockName) != null ? (mapNamesToCount.get(blockName) + 1) : 1;
      mapNamesToCount.put(blockName, count);
    }
    List<Entry<String, Integer>> listDisplayStrings = Lists.newArrayList();
    for (Entry<String, Integer> e : mapNamesToCount.entrySet()) {
      listDisplayStrings.add(e);
    }
    Collections.sort(listDisplayStrings, new Comparator<Entry<String, Integer>>() {

      @Override
      public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
        return Integer.compare(o2.getValue(), o1.getValue());
      }
    });
    return listDisplayStrings;
  }

  /***
   * scary recursive stuff. the big mess that keeps the network connected and updated in real time
   */
  private void addConnectables(DimPos sourcePos, Set<DimPos> set, DimPos masterPos) {
    if (sourcePos == null || sourcePos.getWorld() == null || !sourcePos.isLoaded()) {
      return;
    }
    // Look in all directions
    for (Direction direction : Direction.values()) {
      DimPos lookPos = sourcePos.offset(direction);
      if (!lookPos.isLoaded()) {
        continue;
      }
      ChunkAccess chunk = lookPos.getChunk();
      if (chunk == null) {
        continue;
      }
      // Prevent having multiple  on a network and break all others.
      TileMain maybeMain = lookPos.getTileEntity(TileMain.class);
      if (maybeMain != null && !lookPos.equals(masterPos.getWorld(), masterPos.getBlockPos())) {
        UtilInventory.nukeAndDrop(lookPos);
        continue;
      }
      BlockEntity tileHere = lookPos.getTileEntity(BlockEntity.class);
      if (tileHere == null) {
        continue;
      }
      IConnectable capabilityConnectable = tileHere.getCapability(StorageNetworkCapabilities.CONNECTABLE_CAPABILITY, direction.getOpposite()).orElse(null);
      if (capabilityConnectable == null) {
        continue;
      }
      if (capabilityConnectable.getPos() == null) {
        capabilityConnectable.setPos(lookPos);
        capabilityConnectable.setMainPos(masterPos);
      }
      if (capabilityConnectable != null) {
        capabilityConnectable.setMainPos(masterPos);
        DimPos realConnectablePos = capabilityConnectable.getPos();
        boolean beenHereBefore = set.contains(realConnectablePos);
        if (beenHereBefore) {
          continue;
        }
        if (realConnectablePos.getWorld() == null) {
          realConnectablePos.setWorld(sourcePos.getWorld());
        }
        set.add(realConnectablePos);
        addConnectables(realConnectablePos, set, masterPos);
        tileHere.setChanged();
        chunk.setUnsaved(true);
      }
    }
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
      StorageNetworkMod.LOGGER.error("Error: network get sorted by priority error, some network components are disconnected ", e);
      return new ArrayList<>();
    }
  }

  private Set<IConnectableLink> getConnectableStorage() {
    Set<DimPos> conSet = new HashSet<>(connectables);
    Set<IConnectableLink> result = new HashSet<>();
    for (DimPos dimpos : conSet) {
      if (!dimpos.isLoaded()) {
        continue;
      }
      BlockEntity tileEntity = dimpos.getTileEntity(BlockEntity.class);
      if (tileEntity == null) {
        continue;
      }
      IConnectableLink capConnect = tileEntity.getCapability(StorageNetworkCapabilities.CONNECTABLE_ITEM_STORAGE_CAPABILITY, null).orElse(null);
      if (capConnect == null) {
        continue;
      }
      result.add(capConnect);
    }
    return result;
  }

  /**
   * This is a recursively called method that traverses all connectable blocks and stores them in this tiles connectables list.
   *
   * used by refresh
   *
   * @param sourcePos
   *          of main tile
   */
  private Set<DimPos> getConnectables(DimPos masterPos) {
    HashSet<DimPos> result = new HashSet<>();
    addConnectables(masterPos, result, masterPos);
    return result;
  }

  /**
   * TODO: is this needed anymore? isnt it always non-null
   */
  private void selfValidate() {
    if (connectables == null) {
      setShouldRefresh();
    }
  }
}
