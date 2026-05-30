package com.lothrazar.storagenetwork.api.network;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.api.batch.RequestBatch;
import com.lothrazar.storagenetwork.api.capabilities.ItemStackMatcherDefault;
import net.minecraft.world.item.ItemStack;

/**
 * Contract for a network module - the brain of a main network tile.
 *
 * Responsible for connection list, cache, requests and single inserts.
 * Not responsible for processing, import cables, or export cables.
 */
public interface NetworkModule {

  // --- connection list / refresh ---

  /**
   * @return validated connected nodes, ignoring content or subtype.
   */
  Set<ConnectableNode> getConnectables();

  /**
   * @return how many connectable positions are currently tracked.
   */
  int getConnectableSize();

  /**
   * @return true if a refresh has been requested.
   */
  boolean shouldRefresh();

  /**
   * Flag the module to refresh at the next available tick. Use when nodes get
   * added/removed/updated.
   */
  void setShouldRefresh();

  /**
   * Perform refresh now (regardless of the shouldRefresh flag) and clear the
   * flag.
   *
   * @param masterPos position of the main tile
   */
  void doRefresh(DimPos masterPos);

  // --- queries ---

  /**
   * @return unsorted list of stacks across the network. Respects local node
   *         filter settings.
   */
  List<ItemStack> getStacks();

  /**
   * @param isFiltered if true respects local node filter settings
   * @return unsorted list of stacks across the network.
   */
  List<ItemStack> getStacks(boolean isFiltered);

  /**
   * @return list of network contents sorted by priority. Use for screens.
   *         Use {@link #getStacks()} when no sort is needed.
   */
  List<ItemStack> getSortedStacks();

  /**
   * @param filter for the itemstack request
   * @return total count of items across the network matching this filter
   */
  int getAmount(ItemStackMatcherDefault filter);

  /**
   * @return total number of empty slots across the entire network.
   */
  int emptySlots();

  /**
   * Vanilla-chest-style comparator signal: 0 when empty, otherwise
   * floor(filled / total * 14) + 1.
   */
  int getComparatorSignal();

  /**
   * Display strings for connected node types, used by tooltip/diagnostics.
   */
  List<Entry<String, Integer>> getDisplayStrings();

  // --- transfer ---

  /**
   * Insert contents into the network. Used by players and import cables.
   * Only uses nodes with EnumStorageDirection.IN. Uses NetworkCache first
   * and keeps it updated.
   *
   * @param stack    held stack; reference may be mutated regardless of simulate
   * @param simulate true to test, false to execute
   * @return count of how many items were inserted successfully
   */
  int insertStack(ItemStack stack, boolean simulate);

  /**
   * Request an item out of the network.
   *
   * @param matcher  filter for what you want
   * @param size     amount requested
   * @param simulate true for simulation, false to execute
   * @return stack copy if simulated, the real stack if executed; EMPTY on miss
   */
  ItemStack request(ItemStackMatcherDefault matcher, int size, boolean simulate);

  /**
   * Execute a batched request across all connected storages.
   */
  void executeRequestBatch(RequestBatch batch);
}
