package com.lothrazar.storagenetwork.block.cable.export;

import com.lothrazar.storagenetwork.api.EnumStorageDirection;
import com.lothrazar.storagenetwork.block.TileCableWithFacing;
import com.lothrazar.storagenetwork.capability.CapabilityConnectableAutoIO;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import com.lothrazar.storagenetwork.registry.StorageNetworkCapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class TileCableExport extends TileCableWithFacing implements MenuProvider {

  protected CapabilityConnectableAutoIO ioStorage;

  public TileCableExport(BlockPos pos, BlockState state) {
    super(SsnRegistry.Tiles.EXPORT_KABEL.get(), pos, state);
    this.ioStorage = new CapabilityConnectableAutoIO(this, EnumStorageDirection.OUT);
    this.ioStorage.getFilter().isAllowList = true; // default only; let NBT override
  }

  @Override
  public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
    return new ContainerCableExportFilter(i, level, worldPosition, playerInventory, playerEntity);
  }

  @Override
  public Component getDisplayName() {
    return Component.translatable("block.storagenetwork.export_kabel");
  }

  @Override
  public void setDirection(Direction direction) {
    super.setDirection(direction);
    this.ioStorage.setInventoryFace(direction);
  }

  @Override
  public void load(CompoundTag compound) {
    super.load(compound);
    this.ioStorage.deserializeNBT(compound.getCompound("ioStorage"));
    ioStorage.upgrades.deserializeNBT(compound.getCompound("upgrades"));
  }

  @Override
  public void saveAdditional(CompoundTag compound) {
    super.saveAdditional(compound);
    compound.put("ioStorage", this.ioStorage.serializeNBT());
    compound.put("upgrades", ioStorage.upgrades.serializeNBT());
  }

  @Override
  public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
    if (capability == StorageNetworkCapabilities.CONNECTABLE_AUTO_IO) {
      LazyOptional<CapabilityConnectableAutoIO> cap = LazyOptional.of(() -> ioStorage);
      return cap.cast();
    }
    return super.getCapability(capability, facing);
  }

  public static void clientTick(Level level, BlockPos blockPos, BlockState blockState, TileCableExport tile) {}

  public static <E extends BlockEntity> void serverTick(Level level, BlockPos blockPos, BlockState blockState, TileCableExport tile) {
    tile.refreshInventoryDirection();
  }
}
