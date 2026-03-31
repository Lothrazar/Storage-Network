package com.lothrazar.storagenetwork.item.remote;

import java.util.List;
import com.lothrazar.library.item.ItemFlib;
import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.api.EnumSortType;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.block.request.TileRequest;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import com.lothrazar.storagenetwork.util.UtilRemote;
import com.lothrazar.storagenetwork.util.UtilTileEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemRemote extends ItemFlib implements MenuProvider {

  public static final String NBT_JEI = TileRequest.NBT_JEI;
  public static final String NBT_SORT = "sort";
  public static final String NBT_DOWN = "down";

  public ItemRemote(Properties properties) {
    super(properties.stacksTo(1));
  }

  public static boolean isJeiSearchSynced(ItemStack stack) {
    CompoundTag tag = stack.getOrCreateTag();
    if (tag.contains(NBT_JEI)) {
      return tag.getBoolean(NBT_JEI);
    }
    return false;
  }

  public static void setJeiSearchSynced(ItemStack stack, boolean val) {
    stack.getOrCreateTag().putBoolean(NBT_JEI, val);
  }

  public static boolean getDownwards(ItemStack stack) {
    CompoundTag tag = stack.getOrCreateTag();
    if (tag.contains(NBT_DOWN)) {
      return tag.getBoolean(NBT_DOWN);
    }
    return false;
  }

  public static void setDownwards(ItemStack stack, boolean val) {
    stack.getOrCreateTag().putBoolean(NBT_DOWN, val);
  }

  public static EnumSortType getSort(ItemStack stack) {
    CompoundTag tag = stack.getOrCreateTag();
    if (tag.contains(NBT_SORT)) {
      int sort = tag.getInt(NBT_SORT);
      return EnumSortType.values()[sort];
    }
    return EnumSortType.NAME;
  }

  public static void setSort(ItemStack stack, EnumSortType val) {
    stack.getOrCreateTag().putInt(NBT_SORT, val.ordinal());
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    InteractionHand hand = context.getHand();
    Level world = context.getLevel();
    BlockPos pos = context.getClickedPos();
    Player player = context.getPlayer();
    if (world.getBlockEntity(pos) instanceof TileMain) {
      ItemStack stack = player.getItemInHand(hand);
      DimPos.putPos(stack, pos, world);
      UtilTileEntity.statusMessage(player, "item.remote.connected");
      return InteractionResult.SUCCESS;
    }
    return InteractionResult.PASS;
  }

  @Override
  @OnlyIn(Dist.CLIENT)
  public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
    tooltip.add(Component.translatable(getDescriptionId() + ".tooltip").withStyle(ChatFormatting.GRAY));
    if (stack.hasTag()) {
      DimPos dp = DimPos.getPosStored(stack);
      if (dp != null) {
        tooltip.add(dp.makeTooltip());
      }
    }
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
    ItemStack itemStackIn = player.getItemInHand(hand);
    if (UtilRemote.openRemote(world, player, itemStackIn, this)) {
      // ok great 
      return InteractionResultHolder.success(itemStackIn);
    }
    return super.use(world, player, hand);
  }

  @Override
  public Component getDisplayName() {
    return Component.translatable(this.getDescriptionId());
  }

  @Override
  public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
    boolean crafting = (this == SsnRegistry.Items.CRAFTING_REMOTE.get());
    boolean expanded = (this == SsnRegistry.Items.EXPANDED_REMOTE.get());
    if (expanded) {
      return new ContainerNetworkExpandedRemote(id, inv);
    }
    else if (crafting) {
      return new ContainerNetworkCraftingRemote(id, inv);
    }
    else {
      return new ContainerNetworkRemote(id, inv);
    }
  }

  public static void setAutoFocus(ItemStack stack, boolean autoFocus) {
    stack.getOrCreateTag().putBoolean("autoFocus", autoFocus);
  }

  public static boolean getAutoFocus(ItemStack stack) {
    return stack.getOrCreateTag().getBoolean("autoFocus");
  }
}
