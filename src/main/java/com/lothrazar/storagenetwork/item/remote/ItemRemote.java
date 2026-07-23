package com.lothrazar.storagenetwork.item.remote;

import java.util.function.Consumer;
import com.lothrazar.library.item.ItemFlib;
import com.lothrazar.library.util.ChatUtil;
import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.api.EnumSortType;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.block.request.TileRequest;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class ItemRemote extends ItemFlib implements MenuProvider {

  public static final String NBT_JEI = TileRequest.NBT_JEI;
  public static final String NBT_BOUND = "bound";
  public static final String NBT_SORT = "sort";
  public static final String NBT_DOWN = "down";
  public static final String NBT_FULLSTACK = "fullstack";

  public ItemRemote(Properties properties) {
    super(properties.stacksTo(1));
  }

  private static CompoundTag readTag(ItemStack stack) {
    CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
    return customData != null ? customData.copyTag() : new CompoundTag();
  }

  public static boolean isJeiSearchSynced(ItemStack stack) {
    CompoundTag tag = readTag(stack);
    return tag.getBooleanOr(NBT_JEI, false);
  }

  public static void setJeiSearchSynced(ItemStack stack, boolean val) {
    stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data.update(tag -> tag.putBoolean(NBT_JEI, val)));
  }

  public static boolean getDownwards(ItemStack stack) {
    CompoundTag tag = readTag(stack);
    return tag.getBooleanOr(NBT_DOWN, false);
  }

  public static void setDownwards(ItemStack stack, boolean val) {
    stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data.update(tag -> tag.putBoolean(NBT_DOWN, val)));
  }

  public static EnumSortType getSort(ItemStack stack) {
    CompoundTag tag = readTag(stack);
    return EnumSortType.values()[tag.getIntOr(NBT_SORT, EnumSortType.NAME.ordinal())];
  }

  public static void setSort(ItemStack stack, EnumSortType val) {
    stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data.update(tag -> tag.putInt(NBT_SORT, val.ordinal())));
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
      ChatUtil.sendStatusMessage(player, "item.remote.connected");
      return InteractionResult.SUCCESS;
    }
    return InteractionResult.PASS;
  }

  @Override
  public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flagIn) {
    tooltip.accept(Component.translatable(getDescriptionId() + ".tooltip").withStyle(ChatFormatting.GRAY));
    if (stack.has(DataComponents.CUSTOM_DATA)) {
      DimPos dp = DimPos.getPosStored(stack);
      if (dp != null) {
        tooltip.accept(dp.makeTooltip());
      }
    }
  }

  @Override
  public InteractionResult use(Level world, Player player, InteractionHand hand) {
    ItemStack itemStackIn = player.getItemInHand(hand);
    if (RemoteHelpers.openRemote(world, player, itemStackIn, this)) {
      return InteractionResult.SUCCESS;
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
    stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data.update(tag -> tag.putBoolean("autoFocus", autoFocus)));
  }

  public static boolean getAutoFocus(ItemStack stack) {
    return readTag(stack).getBooleanOr("autoFocus", false);
  }

  public static boolean isFullStackCraft(ItemStack stack) {
    CompoundTag tag = readTag(stack);
    //default true so new remotes match the post-bugfix behavior
    return tag.getBooleanOr(NBT_FULLSTACK, true);
  }

  public static void setFullStackCraft(ItemStack stack, boolean val) {
    stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data.update(tag -> tag.putBoolean(NBT_FULLSTACK, val)));
  }
}
