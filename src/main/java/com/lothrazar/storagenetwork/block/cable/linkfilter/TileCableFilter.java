package com.lothrazar.storagenetwork.block.cable.linkfilter;

import com.lothrazar.storagenetwork.api.capabilities.CapabilityConnectable;
import com.lothrazar.storagenetwork.api.capabilities.CapabilityConnectableDefault;
import com.lothrazar.storagenetwork.block.TileCableWithFacing;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileCableFilter extends TileCableWithFacing implements MenuProvider {

  protected CapabilityConnectableDefault capability;

  public TileCableFilter(BlockPos pos, BlockState state) {
    super(SsnRegistry.Tiles.FILTER_KABEL.get(), pos, state);
    this.capability = new CapabilityConnectableDefault(this);
  }

  public CapabilityConnectable getCapabilityLink() {
    return capability;
  }

  @Override
  public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
    return new ContainerCableFilter(i, level, worldPosition, playerInventory, playerEntity);
  }

  @Override
  public Component getDisplayName() {
    return Component.translatable("block.storagenetwork.filter_kabel");
  }

  @Override
  public void setDirection(Direction direction) {
    super.setDirection(direction);
    this.capability.setInventoryFace(direction);
  }

  @Override
  protected void loadAdditional(ValueInput input) {
    super.loadAdditional(input);
    this.capability.deserialize(input.childOrEmpty("capability"));
  }

  @Override
  protected void saveAdditional(ValueOutput output) {
    super.saveAdditional(output);
    capability.serialize(output.child("capability"));
  }

  public static void clientTick(Level level, BlockPos blockPos, BlockState blockState, TileCableFilter tile) {}

  public static <E extends BlockEntity> void serverTick(Level level, BlockPos blockPos, BlockState blockState, TileCableFilter tile) {
    tile.refreshInventoryDirection();
  }
}
