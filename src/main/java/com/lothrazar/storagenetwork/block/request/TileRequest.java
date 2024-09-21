package com.lothrazar.storagenetwork.block.request;

import com.lothrazar.storagenetwork.StorageNetwork;
import com.lothrazar.storagenetwork.api.EnumSortType;
import com.lothrazar.storagenetwork.api.ITileNetworkSync;
import com.lothrazar.storagenetwork.block.TileConnectable;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import com.lothrazar.storagenetwork.util.UtilInventory;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Constants;

public class TileRequest extends TileConnectable implements INamedContainerProvider, ITileNetworkSync {

  public static final String NBT_JEI = StorageNetwork.MODID + "jei";
  private static final String NBT_DIR = StorageNetwork.MODID + "dir";
  private static final String NBT_SORT = StorageNetwork.MODID + "sort";
  private boolean downwards;
  private EnumSortType sort = EnumSortType.NAME;
  private boolean isJeiSearchSynced;
  @Deprecated
  private Map<Integer, ItemStack> matrix = new HashMap<>();

  public TileRequest() {
    super(SsnRegistry.REQUESTTILE);
  }

  @Override
  public void read(BlockState bs, CompoundNBT compound) {
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
      ListNBT invList = compound.getList("matrix", Constants.NBT.TAG_COMPOUND);
      for (int i = 0; i < invList.size(); i++) {
        CompoundNBT stackTag = invList.getCompound(i);
        int slot = stackTag.getByte("Slot");
        ItemStack s = ItemStack.read(stackTag);
        if (world != null) {
          StorageNetwork.LOGGER.info("world upgrade: item dropping onluy once so it doesnt get deleted; " + this.pos + ":" + s);
          UtilInventory.dropItem(world, this.pos, s);
          matrix.put(slot, ItemStack.EMPTY);
        }
        else {
          //i was not able to drop it in the world. save it so its not deleted. will be hidden from player
          matrix.put(slot, s);
        }
      }
    }
    super.read(bs, compound);
  }

  @Override
  public CompoundNBT write(CompoundNBT compound) {
    compound.putBoolean(NBT_DIR, isDownwards());
    compound.putInt(NBT_SORT, getSort().ordinal());
    compound.putBoolean(NBT_JEI, this.isJeiSearchSynced());
    //legacy : only used when converting old worlds to new worlds 
    if (matrix != null) {
      ListNBT invList = new ListNBT();
      for (int i = 0; i < 9; i++) {
        if (matrix.get(i) != null && matrix.get(i).isEmpty() == false) {
          if (world != null) {
            StorageNetwork.LOGGER.info("World Upgrade: item dropping only once so it doesnt get deleted; " + this.pos + ":" + matrix.get(i));
            UtilInventory.dropItem(world, this.pos, matrix.get(i));
            matrix.put(i, ItemStack.EMPTY);
          }
          else {
            //i was not able to drop it in the world. keep saving it and never delete items. will be hidden from player
            CompoundNBT stackTag = new CompoundNBT();
            stackTag.putByte("Slot", (byte) i);
            matrix.get(i).write(stackTag);
            invList.add(stackTag);
          }
        }
      }
      compound.put("matrix", invList);
    }
    return super.write(compound);
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
  public ITextComponent getDisplayName() {
    return new TranslationTextComponent(getType().getRegistryName().getPath());
  }

  @Override
  public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
    return new ContainerNetworkCraftingTable(i, world, pos, playerInventory, playerEntity);
  }

  public boolean isJeiSearchSynced() {
    return isJeiSearchSynced;
  }

  @Override
  public void setJeiSearchSynced(boolean val) {
    isJeiSearchSynced = val;
  }
}
