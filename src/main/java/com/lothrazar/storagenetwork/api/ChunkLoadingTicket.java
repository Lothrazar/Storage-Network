package com.lothrazar.storagenetwork.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

/**
 * Reusable holder for a single vanilla FORCED chunk ticket. Encapsulates the
 * three pieces of state we need to safely release a ticket later (was-held,
 * which chunk, which dimension) and the idempotent assert/release operations
 * against the target level.
 *
 * Used by TileConnectable for the own-chunk ticket and TileNetworkReceiver
 * for the cross-dim master-chunk ticket. Any future tile that needs to hold
 * a forced-chunk ticket should reuse this instead of re-inventing the triple.
 */
public class ChunkLoadingTicket {

  private boolean held;
  private ChunkPos chunkPos;
  private String dim; // resource location string, eg. "minecraft:overworld"

  public boolean isHeld() {
    return held;
  }

  public ChunkPos chunkPos() {
    return chunkPos;
  }

  public String dim() {
    return dim;
  }

  /**
   * Idempotently force-load the given chunk on the given level. Vanilla
   * updateChunkForced is a LongSet add internally, so re-asserting every tick
   * is cheap and self-heals if the persisted state desyncs from
   * ForcedChunksSavedData.
   *
   * @return true if this assert transitioned from not-held to held (caller
   *         can use this for one-shot logging on a real transition).
   */
  public boolean assertOn(ServerLevel sl, ChunkPos cp) {
    sl.getChunkSource().updateChunkForced(cp, true);
    boolean isNewlyHeld = !held || chunkPos == null || dim == null;
    if (isNewlyHeld) {
      held = true;
      chunkPos = cp;
      dim = sl.dimension().location().toString();
    }
    return isNewlyHeld;
  }

  /**
   * Release the ticket if held. The dim string is used to look up the right
   * level via the passed server, so this works cross-dimensionally - the
   * caller doesn't need to know which level owns the ticket.
   *
   * @return true if a held ticket was actually released.
   */
  public boolean release(MinecraftServer server) {
    if (!held) {
      return false;
    }
    if (dim != null && chunkPos != null && server != null) {
      ServerLevel sl = DimPos.stringDimensionLookup(dim, server);
      if (sl != null) {
        sl.getChunkSource().updateChunkForced(chunkPos, false);
      }
    }
    clear();
    return true;
  }

  public void clear() {
    held = false;
    chunkPos = null;
    dim = null;
  }

  public void save(CompoundTag tag, String heldKey, String dimKey, String chunkXKey, String chunkZKey) {
    tag.putBoolean(heldKey, held);
    if (dim != null) {
      tag.putString(dimKey, dim);
    }
    if (chunkPos != null) {
      tag.putInt(chunkXKey, chunkPos.x);
      tag.putInt(chunkZKey, chunkPos.z);
    }
  }

  public void load(CompoundTag tag, String heldKey, String dimKey, String chunkXKey, String chunkZKey) {
    held = tag.getBoolean(heldKey);
    dim = tag.contains(dimKey) ? tag.getString(dimKey) : null;
    if (tag.contains(chunkXKey) && tag.contains(chunkZKey)) {
      chunkPos = new ChunkPos(tag.getInt(chunkXKey), tag.getInt(chunkZKey));
    }
    else {
      chunkPos = null;
    }
  }
}
