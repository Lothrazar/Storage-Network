package com.lothrazar.storagenetwork.block.cradle;

import java.util.ArrayList;
import java.util.List;
import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.api.IConnectableLink;
import com.lothrazar.storagenetwork.block.TileConnectable;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.capability.CapabilityCradleLink;
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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public class TileStorageCradle extends TileConnectable implements MenuProvider {

  public static final int HOLDER_SIZE = 9;
  private final ItemStackHandler holder = new ItemStackHandler(HOLDER_SIZE) {

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
      return !stack.isEmpty()
          && stack.getCapability(Capabilities.ItemHandler.ITEM) != null;
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
  private final CapabilityCradleLink linkCap = new CapabilityCradleLink(this);

  public TileStorageCradle(BlockPos pos, BlockState state) {
    super(SsnRegistry.Tiles.STORAGE_CRADLE.get(), pos, state);
  }

  public ItemStackHandler getHolder() {
    return holder;
  }

  public ItemStack getHeldStack(int slot) {
    return holder.getStackInSlot(slot);
  }

  public List<IItemHandler> getHeldHandlers() {
    List<IItemHandler> out = new ArrayList<>(HOLDER_SIZE);
    for (int i = 0; i < HOLDER_SIZE; i++) {
      ItemStack s = holder.getStackInSlot(i);
      if (s.isEmpty()) {
        continue;
      }
      IItemHandler h = s.getCapability(Capabilities.ItemHandler.ITEM);
      if (h != null) {
        out.add(h);
      }
    }
    return out;
  }

  public IConnectableLink getLinkCapability() {
    return linkCap;
  }

  @Override
  protected void loadAdditional(CompoundTag compound, HolderLookup.Provider registries) {
    super.loadAdditional(compound, registries);
    if (compound.contains("holder")) {
      holder.deserializeNBT(registries, compound.getCompound("holder"));
    }
  }

  @Override
  protected void saveAdditional(CompoundTag compound, HolderLookup.Provider registries) {
    super.saveAdditional(compound, registries);
    compound.put("holder", holder.serializeNBT(registries));
  }

  @Override
  public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
    return new ContainerStorageCradle(i, level, worldPosition, playerInventory, playerEntity);
  }

  @Override
  public Component getDisplayName() {
    return Component.translatable("block.storagenetwork.storage_cradle");
  }
}
