package com.lothrazar.storagenetwork.api;

import com.google.common.base.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.items.IItemHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DimPos implements INBTSerializable<CompoundTag> {  // NOPMD

  public static final Logger LOGGER = LogManager.getLogger();
  private String dimension;
  private BlockPos pos = new BlockPos(0, 0, 0);
  private Level world;

  public DimPos(CompoundTag tag) {
    if (tag.contains(NBT_X)) {
      pos = new BlockPos(tag.getInt(NBT_X), tag.getInt(NBT_Y), tag.getInt(NBT_Z));
    }
    dimension = tag.getString(NBT_DIM);
  }

  public DimPos(Level world, BlockPos pos) {
    this.pos = pos;
    this.setWorld(world);
    if (world != null) {
      dimension = dimensionToString(world);
    }
  }

  public static DimPos getPosStored(ItemStack itemStackIn) {
    CustomData data = itemStackIn.get(DataComponents.CUSTOM_DATA);
    if (data == null || !data.copyTag().getBoolean(NBT_BOUND)) {
      return null;
    }
    return new DimPos(data.copyTag());
  }

  public Level getWorld() {
    return world;
  }

  public BlockPos getBlockPos() {
    return pos;
  }

  public BlockState getBlockState() {
    return getWorld().getBlockState(getBlockPos());
  }

  public <V> V getTileEntity(Class<V> tileEntityClassOrInterface) {
    return getTileEntity(tileEntityClassOrInterface, getWorld());
  }

  public static String dimensionToString(Level w) {
    //example: returns "minecraft:overworld" resource location
    return w.dimension().location().toString();
  }

  public static final String NBT_Z = "Z";
  public static final String NBT_Y = "Y";
  public static final String NBT_X = "X";
  public static final String NBT_DIM = "dimension";
  public static final String NBT_BOUND = "bound";

  public static void putPos(ItemStack stack, BlockPos pos, Level world) {
    stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data.update(tag -> {
      tag.putInt(NBT_X, pos.getX());
      tag.putInt(NBT_Y, pos.getY());
      tag.putInt(NBT_Z, pos.getZ());
      tag.putString(NBT_DIM, DimPos.dimensionToString(world));
      tag.putBoolean(NBT_BOUND, true);
    }));
  }

  public static String getDim(ItemStack stack) {
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    return data != null ? data.copyTag().getString(NBT_DIM) : "";
  }

  public static void putDim(ItemStack stack, Level world) {
    stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data.update(tag ->
        tag.putString(NBT_DIM, DimPos.dimensionToString(world))));
  }

  public static ServerLevel stringDimensionLookup(String s, MinecraftServer serv) {
    return stringDimensionLookup(ResourceLocation.tryParse(s), serv);
  }

  public static ServerLevel stringDimensionLookup(ResourceLocation s, MinecraftServer serv) {
    ResourceKey<Level> worldKey = ResourceKey.create(Registries.DIMENSION, s);
    if (worldKey == null) {
      return null;
    }
    return serv.getLevel(worldKey);
  }

  @SuppressWarnings("unchecked")
  public <V> V getTileEntity(Class<V> tileEntityClassOrInterface, Level world) {
    BlockPos tilePos = getBlockPos();
    if (world == null || tilePos == null) {
      return null;
    }
    //refresh server world 
    if (dimension != null && world.getServer() != null
        && dimension.isEmpty() == false) {
      ServerLevel dimWorld = stringDimensionLookup(this.dimension, world.getServer());
      //reach across to the other dimension
      if (dimWorld != null) {
        world = dimWorld.getLevel();
      }
      else {
        LOGGER.error(" Dimworld NOT FOUND for " + dimension);
      }
    }
    //end refresh srever world
    BlockEntity tileEntity = world.getBlockEntity(tilePos);
    if (tileEntity == null) {
      return null;
    }
    if (!tileEntityClassOrInterface.isAssignableFrom(tileEntity.getClass())) {
      //      StorageNetwork.log(tilePos + " network not found ");
      return null;
    }
    return (V) tileEntity;
  }

  /**
   * Resolve the actual Level for this DimPos's stored dimension string when possible.
   * Falls back to the cached `world` field if no server is available (eg. on the client).
   */
  public Level resolveLevel() {
    Level world = getWorld();
    if (world != null && world.getServer() != null && dimension != null && !dimension.isEmpty()) {
      ServerLevel dimWorld = stringDimensionLookup(this.dimension, world.getServer());
      if (dimWorld != null) {
        return dimWorld;
      }
    }
    return world;
  }

  public <V> V getCapability(BlockCapability<V, Direction> capability, Direction side) {
    Level world = resolveLevel();
    if (world == null || getBlockPos() == null) {
      return null;
    }
    return world.getCapability(capability, getBlockPos(), side);
  }

  public IItemHandler getItemHandler(Direction side) {
    Level world = resolveLevel();
    if (world == null || getBlockPos() == null) {
      return null;
    }
    return world.getCapability(Capabilities.ItemHandler.BLOCK, getBlockPos(), side);
  }

  @SuppressWarnings("deprecation")
  public boolean isLoaded() {
    Level resolved = resolveLevel();
    return resolved == null ? false : resolved.hasChunkAt(pos);
  }

  public boolean equals(Level world, BlockPos pos) {
    //    world.dimension
    //    return dimension == world.provider.getDimension() &&
    // TODO dimension testing stuff? is it needed ^^
    return pos.equals(this.pos);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DimPos dimPos = (DimPos) o;
    return dimension.equals(dimPos.dimension) &&
        Objects.equal(pos, dimPos.pos);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(dimension, pos);
  }

  @Override
  public String toString() {
    return "[" +
        "dimension=" + dimension +
        ", pos=" + pos +
        ", world=" + getWorld() +
        ']';
  }

  @Override
  public CompoundTag serializeNBT(net.minecraft.core.HolderLookup.Provider registries) {
    if (pos == null) {
      pos = new BlockPos(0, 0, 0);
    }
    CompoundTag result = new CompoundTag();
    result.putInt(NBT_X, pos.getX());
    result.putInt(NBT_Y, pos.getY());
    result.putInt(NBT_Z, pos.getZ());
    result.putString(NBT_DIM, dimension != null ? dimension : "");
    return result;
  }

  @Override
  public void deserializeNBT(net.minecraft.core.HolderLookup.Provider registries, CompoundTag nbt) {
    if (nbt.contains(NBT_X)) {
      pos = new BlockPos(nbt.getInt(NBT_X), nbt.getInt(NBT_Y), nbt.getInt(NBT_Z));
    }
    dimension = nbt.getString(NBT_DIM);
  }

  public DimPos offset(Direction direction) {
    if (pos == null || direction == null || pos == null) {
      LOGGER.info("Error: null offset in DimPos " + direction);
      return null;
    }
    return new DimPos(world, pos.relative(direction));
  }

  public ChunkAccess getChunk() {
    return world == null ? null : world.getChunk(pos);
  }

  public void setWorld(Level world) {
    this.world = world;
  }

  public String getDimension() {
    return dimension;
  }

  public Component makeTooltip() {
    if (pos == null) {
      return null;
    }
    return Component.literal("[" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ", " + dimension + "]").withStyle(ChatFormatting.DARK_GRAY);
  }
}
