package com.lothrazar.storagenetwork.block.cable;

import java.util.Map;
import com.google.common.collect.Maps;
import com.lothrazar.library.block.EntityBlockFlib;
import com.lothrazar.library.core.IBlockFacade;
import com.lothrazar.library.data.ShapeCache;
import com.lothrazar.storagenetwork.api.EnumConnectType;
import com.lothrazar.storagenetwork.api.network.ConnectableNode;
import com.lothrazar.storagenetwork.block.TileConnectable;
import com.lothrazar.storagenetwork.api.capabilities.CapabilityImportExport;
import com.lothrazar.storagenetwork.api.capabilities.CapabilityImportExportDefault;
import com.lothrazar.storagenetwork.registry.ConfigRegistry;
import com.lothrazar.storagenetwork.registry.StorageNetworkCapabilities;
import net.minecraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BlockCable extends EntityBlockFlib implements SimpleWaterloggedBlock, IBlockFacade {
  public static final Logger LOGGER = LogManager.getLogger();

  public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

  public BlockCable(Identifier id) {
    super(Block.Properties.of().strength(0.2F).setId(ResourceKey.create(Registries.BLOCK, id)));
    registerDefaultState(stateDefinition.any()
        .setValue(NORTH, EnumConnectType.NONE).setValue(EAST, EnumConnectType.NONE)
        .setValue(SOUTH, EnumConnectType.NONE).setValue(WEST, EnumConnectType.NONE)
        .setValue(UP, EnumConnectType.NONE).setValue(DOWN, EnumConnectType.NONE).setValue(WATERLOGGED, false)
        .setValue(IBlockFacade.HAS_FACADE, false));
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
    boolean flag = fluidstate.getType() == Fluids.WATER;
    return super.getStateForPlacement(context).setValue(WATERLOGGED, Boolean.valueOf(flag));
  }

  @SuppressWarnings("deprecation")
  @Override
  public FluidState getFluidState(BlockState state) {
    return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
  }

  @Deprecated
  @Override
  public boolean isPathfindable(BlockState bs, PathComputationType path) {
    return false;
  }

  // Block#onRemove is gone in 26.1; this cleanup now lives in TileCable#preRemoveSideEffects,
  // which the game calls at the equivalent point in the block-removal sequence.

  public static BlockState cleanBlockState(BlockState state) {
    for (Direction d : Direction.values()) {
      EnumProperty<EnumConnectType> prop = FACING_TO_PROPERTY_MAP.get(d);
      if (state.getValue(prop) == EnumConnectType.INVENTORY) {
        //dont replace cable types only inv types
        state = state.setValue(prop, EnumConnectType.NONE);
      }
    }
    return state;
  }

  public static final EnumProperty<EnumConnectType> DOWN = EnumProperty.create("down", EnumConnectType.class);
  public static final EnumProperty<EnumConnectType> UP = EnumProperty.create("up", EnumConnectType.class);
  public static final EnumProperty<EnumConnectType> NORTH = EnumProperty.create("north", EnumConnectType.class);
  public static final EnumProperty<EnumConnectType> SOUTH = EnumProperty.create("south", EnumConnectType.class);
  public static final EnumProperty<EnumConnectType> WEST = EnumProperty.create("west", EnumConnectType.class);
  public static final EnumProperty<EnumConnectType> EAST = EnumProperty.create("east", EnumConnectType.class);
  public static final Map<Direction, EnumProperty<EnumConnectType>> FACING_TO_PROPERTY_MAP = Util.make(Maps.newEnumMap(Direction.class), (p) -> {
    p.put(Direction.NORTH, NORTH);
    p.put(Direction.EAST, EAST);
    p.put(Direction.SOUTH, SOUTH);
    p.put(Direction.WEST, WEST);
    p.put(Direction.UP, UP);
    p.put(Direction.DOWN, DOWN);
  });

  @Override
  public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
    if (ConfigRegistry.COMMON_CONFIG.isLoaded() && ConfigRegistry.enableFacades.get()) {
      VoxelShape facadeShape = getFacadeShape(state, worldIn, pos, context);
      if (facadeShape != null) {
        return facadeShape;
      }
    }
    return ShapeCache.getOrCreate(state, ShapeBuilder::createShape);
  }

  @Override
  public RenderShape getRenderShape(BlockState bs) {
    // RenderShape.ENTITYBLOCK_ANIMATED is gone in 26.1 (enum is now just INVISIBLE/MODEL); the
    // block-entity renderer now runs independently of this value whenever one is registered, so
    // the facade case no longer needs a distinct render shape - the static model always renders.
    return RenderShape.MODEL;
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new TileCable(pos, state);
  }

  @Override
  public void setPlacedBy(Level worldIn, BlockPos pos, BlockState stateIn, LivingEntity placer, ItemStack stack) {
    super.setPlacedBy(worldIn, pos, stateIn, placer, stack);
    //    StorageNetwork.log("blockcable updateconnection  ");
    //    this.updateConnection(worldIn, pos, stateIn);
    BlockState facingState;
    for (Direction d : Direction.values()) {
      BlockPos posoff = pos.relative(d);
      facingState = worldIn.getBlockState(posoff);
      //      BlockEntity tileOffset = worldIn.getBlockEntity(posoff);
      if (CableHelpers.isCableOverride(facingState)) {
        LOGGER.debug("Main override setplacedby " + facingState);
        stateIn = stateIn.setValue(FACING_TO_PROPERTY_MAP.get(d), EnumConnectType.CABLE);
        worldIn.setBlockAndUpdate(pos, stateIn);
      }
      //      IConnectable cap = null;
      //      if (tileOffset != null) {
      //        cap = tileOffset.getCapability(StorageNetworkCapabilities.CONNECTABLE_CAPABILITY).orElse(null);
      //      }
      //      if (cap != null
      //          || facingState.getBlock() == SsnRegistry.MAIN) {
      //        stateIn = stateIn.setValue(FACING_TO_PROPERTY_MAP.get(d), EnumConnectType.CABLE);
      //        worldIn.setBlockAndUpdate(pos, stateIn);
      //      }
    }
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    super.createBlockStateDefinition(builder);
    builder.add(UP, DOWN, NORTH, EAST, SOUTH, WEST, WATERLOGGED, IBlockFacade.HAS_FACADE);
  }

  @Override
  public BlockState updateShape(BlockState stateIn, LevelReader world, ScheduledTickAccess ticks, BlockPos currentPos,
      Direction facing, BlockPos facingPos, BlockState facingState, RandomSource random) {
    EnumProperty<EnumConnectType> property = FACING_TO_PROPERTY_MAP.get(facing);
    if (CableHelpers.isCableOverride(facingState)) {
      return stateIn.setValue(property, EnumConnectType.CABLE);
    }
    //based on capability you have, edit connection type
    BlockEntity tileOffset = world.getBlockEntity(facingPos); //if i have zero other inventories, and this is one now, ok go invo
    if (!hasInventoryAlready(stateIn, facing) && CableHelpers.isInventory(facing, world, facingPos)) {
      return stateIn.setValue(property, EnumConnectType.INVENTORY);
    }
    if (tileOffset != null && world instanceof Level levelInstance) {
      ConnectableNode cap = levelInstance.getCapability(StorageNetworkCapabilities.CONNECTABLE, facingPos, null);
      if (cap != null) {
        return stateIn.setValue(property, EnumConnectType.CABLE);
      }
    }
    return stateIn.setValue(property, EnumConnectType.NONE);
  }

  //only one inventory allowed per link cable eh
  private static boolean hasInventoryAlready(BlockState stateIn, Direction exclude) {
    for (Direction d : Direction.values()) {
      if (d == exclude) {
        continue;  // !! important visual only fix
      }
      if (stateIn.getValue(FACING_TO_PROPERTY_MAP.get(d)).isInventory()) {
        return true;
      }
    }
    return false;
  }
}
