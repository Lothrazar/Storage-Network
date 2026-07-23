package com.lothrazar.storagenetwork.block.cable.link;

import com.lothrazar.storagenetwork.api.capabilities.CapabilityConnectable;
import com.lothrazar.storagenetwork.api.capabilities.CapabilityConnectableDefault;
import com.lothrazar.storagenetwork.block.TileCableWithFacing;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileCableLink extends TileCableWithFacing {

  protected CapabilityConnectableDefault itemStorage;

  public TileCableLink(BlockPos pos, BlockState state) {
    super(SsnRegistry.Tiles.STORAGE_KABEL.get(), pos, state);
    this.itemStorage = new CapabilityConnectableDefault(this);
  }

  public CapabilityConnectable getItemStorage() {
    return itemStorage;
  }

  @Override
  protected void loadAdditional(ValueInput input) {
    super.loadAdditional(input);
    this.itemStorage.deserialize(input.childOrEmpty("capability"));
  }

  @Override
  protected void saveAdditional(ValueOutput output) {
    super.saveAdditional(output);
    itemStorage.serialize(output.child("capability"));
  }

  @Override
  public void setDirection(Direction direction) {
    super.setDirection(direction);
    this.itemStorage.setInventoryFace(direction);
  }

  public static void clientTick(Level level, BlockPos blockPos, BlockState blockState, TileCableLink tile) {}

  public static <E extends BlockEntity> void serverTick(Level level, BlockPos blockPos, BlockState blockState, TileCableLink tile) {
    tile.refreshInventoryDirection();
  }
}
