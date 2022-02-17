package mrriegel.storagenetwork.gui.fb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import it.unimi.dsi.fastutil.ints.Int2IntMap.Entry;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.block.master.TileMaster;
import mrriegel.storagenetwork.data.ItemStackMatcher;
import mrriegel.storagenetwork.gui.IStorageContainer;
import mrriegel.storagenetwork.network.StackRefreshClientMessage;
import mrriegel.storagenetwork.registry.PacketRegistry;
import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;
import shadows.fastbench.gui.ContainerFastBench;
import shadows.fastbench.gui.SlotCraftingSucks;

public abstract class ContainerFastNetworkCrafter extends ContainerFastBench implements IStorageContainer {

  protected boolean forceSync = true;
  protected final World world;
  protected final EntityPlayer player;

  public ContainerFastNetworkCrafter(EntityPlayer player, World world, BlockPos pos) {
    super(player, world, pos);
    if (player.world.isRemote != world.isRemote) throw new RuntimeException("Player and World remoteness are not the same!");
    this.world = world;
    this.player = player;
    this.useNormalTransfer = true;
  }

  @Override
  public abstract TileMaster getTileMaster();

  public abstract void bindHotbar(EntityPlayer player);

  protected void bindPlayerInvo(final InventoryPlayer playerInv) {
    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 9; ++j) {
        this.addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 174 + i * 18));
      }
    }
  }

  protected void bindGrid() {
    int index = 0;
    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 3; ++j) {
        this.addSlotToContainer(new Slot(this.craftMatrix, index++, 8 + j * 18, 110 + i * 18));
      }
    }
  }

  @SuppressWarnings("unused")
  @Override
  public ItemStack transferStackInSlot(EntityPlayer player, int index) {
    if (world.isRemote) {
      return ItemStack.EMPTY;
    }
    ItemStack slotCopy = ItemStack.EMPTY;
    Slot slot = this.inventorySlots.get(index);
    if (slot != null && slot.getHasStack()) {
      ItemStack slotStack = slot.getStack();
      slotCopy = slotStack.copy();
      TileMaster tileMaster = this.getTileMaster();
      if (tileMaster == null) {
        return ItemStack.EMPTY;
      }
      if (index == 0) {
        int num = slotCopy.getMaxStackSize() / slotCopy.getCount();
        IRecipe rec = lastRecipe;
        for (int i = 0; i < num; i++) {
          if (rec != lastRecipe) {
            break;
          }
          try {
            super.transferStackInSlot(player, index);
          }
          catch (Throwable e) {
            //could be sponge or vanilla or something else all together
            //ex https://pastebin.com/3D4ccqrK
            StorageNetwork.instance.logger.error("Caught third party error trying to transfer stack", e);
          }
        }
        return ItemStack.EMPTY; //new ItemStack(Items.STICK);//code is never used 
      }
      else if (tileMaster != null) {
        int rest = tileMaster.insertStack(slotStack, false);
        ItemStack stack = rest == 0 ? ItemStack.EMPTY : ItemHandlerHelper.copyStackWithSize(slotStack, rest);
        slot.putStack(stack);
        detectAndSendChanges();
        List<ItemStack> list = tileMaster.getStacks();
        PacketRegistry.INSTANCE.sendTo(new StackRefreshClientMessage(list, new ArrayList<>()), (EntityPlayerMP) player);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        slot.onTake(player, slotStack);
        return ItemStack.EMPTY;
      }
      slot.onSlotChanged();
      if (slotStack.getCount() == slotCopy.getCount()) {
        return ItemStack.EMPTY;
      }
      slot.onTake(player, slotStack);
    }
    return super.transferStackInSlot(player, index);
  }

  @Override
  public InventoryCrafting getCraftMatrix() {
    return craftMatrix;
  }

  @Override
  public void slotChanged() {}

  protected final class SlotCraftingNetwork extends SlotCraftingSucks {

    protected TileMaster tileMaster;

    public SlotCraftingNetwork(EntityPlayer player, InventoryCrafting matrix, InventoryCraftResult result, int index, int x, int y) {
      super(ContainerFastNetworkCrafter.this, player, matrix, result, index, x, y);
    }

    @Override
    public ItemStack onTake(EntityPlayer player, ItemStack stack) {
      if (!world.isRemote) {
        ItemStack[] lastItems = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
          lastItems[i] = craftMatrix.getStackInSlot(i).copy();
        }
        IRecipe rec = lastRecipe;
        ItemStack take = super.onTake(player, stack);
        if (!world.isRemote) tryRestockGridEntirely(craftMatrix, this, rec, lastItems);
        onCraftMatrixChanged(craftMatrix);
        detectAndSendChanges();
        if (lastRecipe != null && player instanceof EntityPlayerMP) {
          EntityPlayerMP mp = ((EntityPlayerMP) player);
          mp.connection.sendPacket(new SPacketSetSlot(windowId, 0, lastRecipe.getRecipeOutput()));
        }
        forceSync = true;
        return take;
      }
      return stack;
    }

    public TileMaster getTileMaster() {
      return tileMaster;
    }

    public void setTileMaster(TileMaster tileMaster) {
      this.tileMaster = tileMaster;
    }
  }

  public static final void tryRestockGridEntirely(InventoryCrafting matrix, SlotCraftingNetwork slot, IRecipe recipe, ItemStack[] requests) {
    //Can't restock from nowhere.
    if (slot.getTileMaster() == null) return;
    //If ingredients are complex, matching may fail, so we use the slow grabbing process.  This does not restock entirely.
    boolean simple = true;
    for (Ingredient ing : recipe.getIngredients()) {
      if (!ing.isSimple()) {
        simple = false;
        break;
      }
    }
    boolean one = false;
    boolean two = false;
    for (ItemStack i : requests) {
      if (!i.isEmpty()) {
        one = true;
        continue;
      }
      if (one && !i.isEmpty()) {
        two = true;
        break;
      }
    }
    if (!simple) {
      for (int i = 0; i < 9; i++) {
        if (matrix.getStackInSlot(i).isEmpty()) {
          ItemStack cached = requests[i];
          if (!cached.isEmpty()) matrix.setInventorySlotContents(i, slot.getTileMaster().request(new ItemStackMatcher(cached, true, false, cached.hasTagCompound()), two ? 1 : cached.getMaxStackSize(), false));
        }
      }
      return;
    }
    //If not, these requested stacks must meet an item/meta pair.
    /**
     * Grid Layout originally is preserved in the ItemStack[] as 0 1 2 3 4 5 6 7 8
     *
     * We need to grab as much as we can and sort into the original pattern equally.
     */
    ItemStack[] requested = new ItemStack[9];
    Arrays.fill(requested, ItemStack.EMPTY);
    boolean matrixFull = true;
    //Grab as much as possible
    for (int i = 0; i < 9; i++) {
      if (matrix.getStackInSlot(i).isEmpty()) {
        ItemStack cached = requests[i];
        if (!cached.isEmpty()) requested[i] = slot.getTileMaster().request(new ItemStackMatcher(cached, true, false, cached.hasTagCompound()), cached.getMaxStackSize(), false);
        matrixFull = false;
      }
    }
    if (matrixFull) return; //Early return if we don't need to request anything.
    //How much of each stack we have
    Int2IntOpenHashMap collected = new Int2IntOpenHashMap();
    for (ItemStack s : requested) {
      if (!s.isEmpty()) {
        int stack = RecipeItemHelper.pack(s);
        int num = collected.get(stack);
        collected.put(stack, num + s.getCount());
      }
    }
    //How many stacks of each kind we need
    Int2IntOpenHashMap types = new Int2IntOpenHashMap();
    for (ItemStack s : requests) {
      if (!s.isEmpty()) {
        int stack = RecipeItemHelper.pack(s);
        int num = types.get(stack);
        types.put(stack, ++num);
      }
    }
    Int2IntOpenHashMap taken = new Int2IntOpenHashMap();
    //Sort the collected stacks into slots as necessary
    for (int i = 0; i < 9; i++) {
      ItemStack oldStack = requests[i];
      ItemStack curStack = matrix.getStackInSlot(i);
      if (!curStack.isEmpty()) {
        int curStackPack = RecipeItemHelper.pack(curStack);
        float available = collected.get(curStackPack);
        int desired = Math.max(curStack.getCount(), Math.min((int) Math.floor(available / types.get(curStackPack)), curStack.getMaxStackSize()));
        taken.put(curStackPack, taken.get(curStackPack) + desired - curStack.getCount());
        curStack.setCount(desired);
      }
      else if (!oldStack.isEmpty()) {
        int oldStackPack = RecipeItemHelper.pack(oldStack);
        float available = collected.get(oldStackPack);
        int desired = Math.min((int) Math.floor(available / types.get(oldStackPack)), oldStack.getMaxStackSize());
        taken.put(oldStackPack, taken.get(oldStackPack) + desired);
        ItemStack stack = oldStack.copy();
        stack.setCount(desired);
        matrix.setInventorySlotContents(i, stack);
      }
    }
    //Deal with the remainder
    for (Entry ent : taken.int2IntEntrySet()) {
      int collect = collected.get(ent.getIntKey());
      collect -= ent.getIntValue();
      collected.put(ent.getIntKey(), collect);
    }
    TileMaster master = slot.getTileMaster();
    for (Entry ent : collected.int2IntEntrySet()) {
      ItemStack s = RecipeItemHelper.unpack(ent.getIntKey());
      int num = ent.getIntValue();
      while (num > 0) {
        if (num > s.getMaxStackSize()) {
          ItemStack n = s.copy();
          n.setCount(s.getMaxStackSize());
          num -= n.getCount();
          master.insertStack(n, false);
        }
        s.setCount(num);
        num -= s.getCount();
        master.insertStack(s, false);
      }
    }
  }
}
