package mrriegel.storagenetwork.gui.fb;

import java.util.ArrayList;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.block.master.TileMaster;
import mrriegel.storagenetwork.item.remote.ItemRemote;
import mrriegel.storagenetwork.network.StackRefreshClientMessage;
import mrriegel.storagenetwork.registry.PacketRegistry;
import mrriegel.storagenetwork.util.NBTHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ContainerFastRemote extends ContainerFastNetworkCrafter {

  final ItemStack remoteItemStack;
  private final int remoteSlot;

  public ContainerFastRemote(EntityPlayer player, World world, int remoteSlot) {
    super(player, world, BlockPos.ORIGIN);
    this.remoteSlot = remoteSlot;
    remoteItemStack = player.inventory.getStackInSlot(remoteSlot);
    this.inventorySlots.clear();
    this.inventoryItemStacks.clear();
    for (int i = 0; i < 9; i++) {
      if (i != 8) this.craftMatrix.stackList.set(i, NBTHelper.getItemStack(remoteItemStack, "c" + i));
      else this.craftMatrix.setInventorySlotContents(i, NBTHelper.getItemStack(remoteItemStack, "c" + i));
    }
    SlotCraftingNetwork slotCraftOutput = new SlotCraftingNetwork(player, craftMatrix, craftResult, 0, 101, 128);
    slotCraftOutput.setTileMaster(this.getTileMaster());
    this.addSlotToContainer(slotCraftOutput);
    bindGrid();
    bindPlayerInvo(player.inventory);
    bindHotbar(player);
  }

  @Override
  public boolean canInteractWith(EntityPlayer playerIn) {
    TileMaster tileMaster = this.getTileMaster();
    if (tileMaster == null) {
      StorageNetwork.log("ContainerFastRemote closing, master tile not found " + remoteItemStack);
      return false;
    }
    if (!playerIn.world.isRemote && (forceSync || playerIn.world.getTotalWorldTime() % 40 == 0)) {
      forceSync = false;
      PacketRegistry.INSTANCE.sendTo(new StackRefreshClientMessage(tileMaster.getStacks(), new ArrayList<>()), (EntityPlayerMP) playerIn);
    }
    return playerIn.inventory.getStackInSlot(this.remoteSlot) == remoteItemStack;
  }

  @Override
  public void slotChanged() {
    if (craftMatrix != null) {
      for (int i = 0; i < 9; i++) {
        NBTHelper.setItemStack(remoteItemStack, "c" + i, craftMatrix.getStackInSlot(i));
      }
    }
  }

  @Override
  public void onContainerClosed(EntityPlayer player) {
    if (!remoteItemStack.isEmpty())
      for (int i = 0; i < 9; i++) {
        NBTHelper.setItemStack(remoteItemStack, "c" + i, craftMatrix.getStackInSlot(i));
      }
  }

  @Override
  public TileMaster getTileMaster() {
    return ItemRemote.getTile(remoteItemStack);
  }

  @Override
  protected void bindPlayerInvo(final InventoryPlayer playerInv) {
    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 9; ++j) {
        int slot = j + i * 9 + 9;
        if (slot == remoteSlot)
          this.addSlotToContainer(new Slot(playerInv, slot, 8 + j * 18, 174 + i * 18) {

            @Override
            public boolean isItemValid(ItemStack stack) {
              return false;
            }

            @Override
            public boolean canTakeStack(EntityPlayer playerIn) {
              return false;
            }
          });
        else
          this.addSlotToContainer(new Slot(playerInv, slot, 8 + j * 18, 174 + i * 18));
      }
    }
  }

  @Override
  public void bindHotbar(EntityPlayer player) {
    for (int i = 0; i < 9; ++i) {
      if (i == remoteSlot)
        this.addSlotToContainer(new Slot(player.inventory, i, 8 + i * 18, 232) {

          @Override
          public boolean isItemValid(ItemStack stack) {
            return false;
          }

          @Override
          public boolean canTakeStack(EntityPlayer playerIn) {
            return false;
          }
        });
      else
        this.addSlotToContainer(new Slot(player.inventory, i, 8 + i * 18, 232));
    }
  }

  @Override
  public boolean isRequest() {
    return false;
  }

  public static class Client extends ContainerFastRemote {

    public Client(EntityPlayer player, World world, int remoteSlot) {
      super(player, world, remoteSlot);
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventoryIn) {}
  }
}