package com.lothrazar.storagenetwork.block.cable;

import com.lothrazar.storagenetwork.api.EnumConnectType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ShapeBuilder {

  private static final double top = 16;
  private static final double bot = 0;
  private static final double C = 8;
  private static final double w = 2;
  private static final double sm = C - w;
  private static final double lg = C + w;
  //(double x1, double y1, double z1, double x2, double y2, double z2)
  private static final VoxelShape AABB = Block.box(sm, sm, sm, lg, lg, lg);
  //Y for updown
  private static final VoxelShape AABB_UP = Block.box(sm, sm, sm, lg, top, lg);
  private static final VoxelShape AABB_DOWN = Block.box(sm, bot, sm, lg, lg, lg);
  //Z for n-s
  private static final VoxelShape AABB_NORTH = Block.box(sm, sm, bot, lg, lg, lg);
  private static final VoxelShape AABB_SOUTH = Block.box(sm, sm, sm, lg, lg, top);
  //X for e-w
  private static final VoxelShape AABB_WEST = Block.box(bot, sm, sm, lg, lg, lg);
  private static final VoxelShape AABB_EAST = Block.box(sm, sm, sm, top, lg, lg);

  public static boolean shapeConnects(BlockState state, EnumProperty<EnumConnectType> dirctionProperty) {
    return state.getValue(dirctionProperty).equals(EnumConnectType.CABLE)
        || state.getValue(dirctionProperty).equals(EnumConnectType.INVENTORY);
  }

  public static VoxelShape createShape(BlockState state) {
    VoxelShape shape = AABB;
    if (shapeConnects(state, BlockCable.UP)) {
      shape = Shapes.joinUnoptimized(shape, AABB_UP, BooleanOp.OR);
    }
    if (shapeConnects(state, BlockCable.DOWN)) {
      shape = Shapes.joinUnoptimized(shape, AABB_DOWN, BooleanOp.OR);
    }
    if (shapeConnects(state, BlockCable.WEST)) {
      shape = Shapes.joinUnoptimized(shape, AABB_WEST, BooleanOp.OR);
    }
    if (shapeConnects(state, BlockCable.EAST)) {
      shape = Shapes.joinUnoptimized(shape, AABB_EAST, BooleanOp.OR);
    }
    if (shapeConnects(state, BlockCable.NORTH)) {
      shape = Shapes.joinUnoptimized(shape, AABB_NORTH, BooleanOp.OR);
    }
    if (shapeConnects(state, BlockCable.SOUTH)) {
      shape = Shapes.joinUnoptimized(shape, AABB_SOUTH, BooleanOp.OR);
    }
    return shape;
  }
}
