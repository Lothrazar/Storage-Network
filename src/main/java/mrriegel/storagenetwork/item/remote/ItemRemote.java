package mrriegel.storagenetwork.item.remote;

import java.util.List;
import javax.annotation.Nullable;
import mrriegel.storagenetwork.CreativeTab;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.block.master.TileMaster;
import mrriegel.storagenetwork.config.ConfigHandler;
import mrriegel.storagenetwork.data.EnumSortType;
import mrriegel.storagenetwork.gui.GuiHandler;
import mrriegel.storagenetwork.util.NBTHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemRemote extends Item {

  public ItemRemote() {
    super();
    this.setCreativeTab(CreativeTab.tab);
    this.setRegistryName("remote");
    this.setTranslationKey(getRegistryName().toString());
    this.setHasSubtypes(true);
    this.setMaxStackSize(1);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {
    if (isInCreativeTab(tab)) {
      for (int i = 0; i < RemoteType.values().length; i++) {
        list.add(new ItemStack(this, 1, i));
      }
    }
  }

  @Override
  public String getTranslationKey(ItemStack stack) {
    return this.getTranslationKey() + "_" + stack.getItemDamage();
  }

  @Override
  public void addInformation(ItemStack stack, @Nullable World playerIn, List<String> tooltip, ITooltipFlag advanced) {
    tooltip.add(I18n.format("tooltip.storagenetwork.remote_" + stack.getItemDamage()));
    if (stack.hasTagCompound() && NBTHelper.getBoolean(stack, "bound")) {
      // "Dimension: " +
      tooltip.add(NBTHelper.getInteger(stack, "dim") + ", x: " + NBTHelper.getInteger(stack, "x") + ", y: " + NBTHelper.getInteger(stack, "y") + ", z: " + NBTHelper.getInteger(stack, "z"));
    }
  }

  @Override
  public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
    ItemStack itemStackIn = player.getHeldItem(hand);
    int itemDamage = itemStackIn.getItemDamage();
    //skip on client??
    if (world.isRemote || itemDamage < 0 || itemDamage >= RemoteType.values().length || !NBTHelper.getBoolean(itemStackIn, "bound")) {
      //unbound or invalid data
      return super.onItemRightClick(world, player, hand);
    }
    int slot = hand == EnumHand.OFF_HAND ? player.inventory.getSizeInventory() - 1 : player.inventory.currentItem;
    if (tryOpenGui(world, player, itemStackIn, slot))
      return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
    return super.onItemRightClick(world, player, hand);
  }

  @Override
  public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
    ItemStack stack = playerIn.getHeldItem(hand);
    if (worldIn.getTileEntity(pos) instanceof TileMaster) {
      NBTHelper.setInteger(stack, "x", pos.getX());
      NBTHelper.setInteger(stack, "y", pos.getY());
      NBTHelper.setInteger(stack, "z", pos.getZ());
      NBTHelper.setBoolean(stack, "bound", true);
      NBTHelper.setInteger(stack, "dim", worldIn.provider.getDimension());
      NBTHelper.setString(stack, "sort", EnumSortType.NAME.toString());
      return EnumActionResult.SUCCESS;
    }
    return super.onItemUse(playerIn, worldIn, pos, hand, side, hitX, hitY, hitZ);
  }

  public static boolean tryOpenGui(World world, EntityPlayer player, ItemStack remote, int inventorySlot) {
    int itemDamage = remote.getItemDamage();
    World serverTargetWorld;
    int x, y, z, itemStackDim;
    BlockPos targetPos;
    try {
      x = NBTHelper.getInteger(remote, "x");
      y = NBTHelper.getInteger(remote, "y");
      z = NBTHelper.getInteger(remote, "z");
      itemStackDim = NBTHelper.getInteger(remote, "dim");
      // validate possible missing data
      if (NBTHelper.getString(remote, "sort") == null) {
        NBTHelper.setString(remote, "sort", EnumSortType.NAME.toString());
      }
      targetPos = new BlockPos(x, y, z);
      serverTargetWorld = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(itemStackDim);
    }
    catch (Throwable e) {
      //cant tell if this is NBT error or statistics usage recording issue
      //https://github.com/PrinceOfAmber/Storage-Network/issues/93
      StorageNetwork.instance.logger.error("Invalid remote data " + remote.getTagCompound(), e);
      return false;
    }
    if (!serverTargetWorld.getChunk(targetPos).isLoaded()) {
      StorageNetwork.chatMessage(player, "item.remote.notloaded");
      return false;
    }
    RemoteType remoteType = RemoteType.values()[itemDamage];
    // first make sure area is loaded, BEFORE getting TE
    if (serverTargetWorld.getTileEntity(targetPos) instanceof TileMaster) {
      boolean isSameDimension = (itemStackDim == world.provider.getDimension());
      boolean isWithinRange = (player.getDistance(x, y, z) <= ConfigHandler.rangeWirelessAccessor);
      boolean canOpenGUI = false;
      switch (remoteType) {
        case DIMENSIONAL:
          //all dimensions, unlimited range
          canOpenGUI = true;
        break;
        case LIMITED:
          //same dimension AND limited range
          canOpenGUI = isSameDimension && isWithinRange;
        break;
        case UNLIMITED:
        case SIMPLE:// simple is same
          //unlimited range, but MUST be same dimension
          canOpenGUI = isSameDimension;
        break;
      }
      // ok we found a target
      if (canOpenGUI) {
        player.openGui(StorageNetwork.instance, getGui(), world, inventorySlot, y, z);
        return true;
      }
      else {
        StorageNetwork.statusMessage(player, "item.remote.outofrange");
      }
    }
    return false;
  }

  protected static int getGui() {
    return GuiHandler.GuiIDs.REMOTE.ordinal();
  }

  public static TileMaster getTile(ItemStack stack) {
    if (stack == null || stack.isEmpty()
        || FMLCommonHandler.instance() == null
        || FMLCommonHandler.instance().getMinecraftServerInstance() == null) {
      return null;
    }
    BlockPos posTag = new BlockPos(NBTHelper.getInteger(stack, "x"), NBTHelper.getInteger(stack, "y"), NBTHelper.getInteger(stack, "z"));
    WorldServer world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(NBTHelper.getInteger(stack, "dim"));
    TileEntity t = world.getTileEntity(posTag);
    return t instanceof TileMaster ? (TileMaster) t : null;
  }

  public static void copyTag(ItemStack from, ItemStack to) {
    NBTHelper.setInteger(to, "x", NBTHelper.getInteger(from, "x"));
    NBTHelper.setInteger(to, "y", NBTHelper.getInteger(from, "y"));
    NBTHelper.setInteger(to, "z", NBTHelper.getInteger(from, "z"));
    NBTHelper.setBoolean(to, "bound", NBTHelper.getBoolean(from, "bound"));
    NBTHelper.setInteger(to, "dim", NBTHelper.getInteger(from, "dim"));
    NBTHelper.setString(to, "sort", NBTHelper.getString(from, "sort"));
  }
}
