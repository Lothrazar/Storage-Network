package com.lothrazar.storagenetwork.block.cradle;

import java.util.ArrayList;
import java.util.List;
import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.api.capabilities.CapabilityConnectable;
import com.lothrazar.storagenetwork.api.network.BlockEntityCradle;
import com.lothrazar.storagenetwork.block.TileConnectable;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.api.capabilities.CapabilityConnectableCradle;
import com.lothrazar.storagenetwork.registry.CradleAdapterRegistry;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public class TileCradle extends TileConnectable implements MenuProvider, BlockEntityCradle {

  public static final int HOLDER_SIZE = 9;
  private final ItemStackHandler holder = new ItemStackHandler(HOLDER_SIZE) {

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
      return CradleAdapterRegistry.accepts(stack);
    }

    @Override
    public int getSlotLimit(int slot) {
      return 1;
    }

    @Override
    protected void onContentsChanged(int slot) {
      setChanged();
      if (level != null && !level.isClientSide) {
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        DimPos m = getMain();
        if (m != null) {
          TileMain main = m.getTileEntity(TileMain.class);
          if (main != null) {
            main.getNetwork().setShouldRefresh();
          }
        }
      }
    }
  };
  private final CapabilityConnectableCradle linkCap = new CapabilityConnectableCradle(this);

  public TileCradle(BlockPos pos, BlockState state) {
    super(SsnRegistry.Tiles.CRADLE.get(), pos, state);
  }

  public ItemStackHandler getHolder() {
    return holder;
  }

  public ItemStack getHeldStack(int slot) {
    return holder.getStackInSlot(slot);
  }

  @Override
  public List<CapabilityConnectable> getHeldLinks() {
    int n = holder.getSlots();
    List<CapabilityConnectable> out = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      ItemStack s = holder.getStackInSlot(i);
      if (s.isEmpty()) {
        continue;
      }
      CapabilityConnectable link = CradleAdapterRegistry.wrap(s, this);
      if (link != null) {
        out.add(link);
      }
    }
    return out;
  }

  public CapabilityConnectable getLinkCapability() {
    return linkCap;
  }

  @Override
  protected void loadAdditional(CompoundTag compound, HolderLookup.Provider registries) {
    super.loadAdditional(compound, registries);
    if (compound.contains("holder")) {
      // Force the saved Size up to HOLDER_SIZE so old worlds (which saved Size=1) still load
      // and we don't end up with a 1-slot handler at runtime.
      CompoundTag holderTag = compound.getCompound("holder").copy();
      holderTag.putInt("Size", HOLDER_SIZE);
      holder.deserializeNBT(registries, holderTag);
    }
  }

  @Override
  protected void saveAdditional(CompoundTag compound, HolderLookup.Provider registries) {
    super.saveAdditional(compound, registries);
    compound.put("holder", holder.serializeNBT(registries));
  }

  @Override
  public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
    return new ContainerCradle(i, level, worldPosition, playerInventory, playerEntity);
  }

  @Override
  public Component getDisplayName() {
    return Component.translatable("block.storagenetwork.cradle");
  }
}
