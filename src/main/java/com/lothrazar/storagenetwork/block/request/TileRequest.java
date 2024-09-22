package com.lothrazar.storagenetwork.block.request;

import java.util.HashMap;
import java.util.Map;
import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.api.EnumSortType;
import com.lothrazar.storagenetwork.api.ITileNetworkSync;
import com.lothrazar.storagenetwork.block.TileConnectable;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import com.lothrazar.storagenetwork.util.UtilInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class TileRequest extends TileConnectable implements MenuProvider, ITileNetworkSync {

  public static final String NBT_JEI = StorageNetworkMod.MODID + "jei";
  private static final String NBT_DIR = StorageNetworkMod.MODID + "dir";
  private static final String NBT_SORT = StorageNetworkMod.MODID + "sort";
  private boolean downwards;
  private EnumSortType sort = EnumSortType.NAME;
  private boolean isJeiSearchSynced;
  private boolean autoFocus = true;
  @Deprecated
  private Map<Integer, ItemStack> matrix = new HashMap<>();

  public TileRequest(BlockPos pos, BlockState state) {
    super(SsnRegistry.Tiles.REQUEST.get(), pos, state);
  }

  @Override
  public void load(CompoundTag compound) {
    autoFocus = compound.getBoolean("autoFocus");
    setDownwards(compound.getBoolean(NBT_DIR));
    if (compound.contains(NBT_SORT)) {
      setSort(EnumSortType.values()[compound.getInt(NBT_SORT)]);
    }
    if (compound.contains(NBT_JEI)) {
      this.setJeiSearchSynced(compound.getBoolean(NBT_JEI));
    }
    //legacy support: instead of deleting items, in this one-off world upgrade transition
    //drop them on the ground
    //then forever more it will not be saved to this data location
    if (compound.contains("matrix")) {
      ListTag invList = compound.getList("matrix", Tag.TAG_COMPOUND);
      for (int i = 0; i < invList.size(); i++) {
        CompoundTag stackTag = invList.getCompound(i);
        int slot = stackTag.getByte("Slot");
        ItemStack s = ItemStack.of(stackTag);
        if (level != null) {
          StorageNetworkMod.LOGGER.info("world upgrade: item dropping onluy once so it doesnt get deleted; " + this.worldPosition + ":" + s);
          UtilInventory.dropItem(level, this.worldPosition, s);
          matrix.put(slot, ItemStack.EMPTY);
        }
        else {
          //i was not able to drop it in the world. save it so its not deleted. will be hidden from player
          matrix.put(slot, s);
        }
      }
    }
    super.load(compound);
  }

  @Override
  public void saveAdditional(CompoundTag compound) {
    compound.putBoolean("autoFocus", autoFocus);
    compound.putBoolean(NBT_DIR, isDownwards());
    compound.putInt(NBT_SORT, getSort().ordinal());
    compound.putBoolean(NBT_JEI, this.isJeiSearchSynced());
    if (matrix != null) {
      ListTag invList = new ListTag();
      for (int i = 0; i < 9; i++) {
        if (matrix.get(i) != null && matrix.get(i).isEmpty() == false) {
          if (level != null) {
            StorageNetworkMod.LOGGER.info("World Upgrade: item dropping only once so it doesnt get deleted; " + this.worldPosition + ":" + matrix.get(i));
            UtilInventory.dropItem(level, this.worldPosition, matrix.get(i));
            matrix.put(i, ItemStack.EMPTY);
          }
          else {
            //i was not able to drop it in the world. keep saving it and never delete items. will be hidden from player
            CompoundTag stackTag = new CompoundTag();
            stackTag.putByte("Slot", (byte) i);
            matrix.get(i).save(stackTag);
            invList.add(stackTag);
          }
        }
      }
      compound.put("matrix", invList);
    }
  }

  @Override
  public boolean isDownwards() {
    return downwards;
  }

  @Override
  public void setDownwards(boolean downwards) {
    this.downwards = downwards;
  }

  @Override
  public EnumSortType getSort() {
    return sort;
  }

  @Override
  public void setSort(EnumSortType sort) {
    this.sort = sort;
  }

  @Override
  public Component getDisplayName() {
    return new TranslatableComponent(getType().getRegistryName().getPath());
  }

  @Override
  public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
    return new ContainerNetworkCraftingTable(i, level, worldPosition, playerInventory, playerEntity);
  }

  public boolean isJeiSearchSynced() {
    return isJeiSearchSynced;
  }

  @Override
  public void setJeiSearchSynced(boolean val) {
    isJeiSearchSynced = val;
  }

  public boolean getAutoFocus() {
    return this.autoFocus;
  }

  @Override
  public void setAutoFocus(boolean autoFocus) {
    this.autoFocus = autoFocus;
  }
}
