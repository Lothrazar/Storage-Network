package com.lothrazar.storagenetwork.block.drawer;

import java.util.List;
import com.lothrazar.library.block.EntityBlockFlib;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class BlockDrawer extends EntityBlockFlib {

  private static final Logger LOGGER = LogManager.getLogger();
  public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
  // Imprint is stored in the BlockItem's CUSTOM_DATA under this key (Item registry name).
  public static final String NBT_LOCKED_ITEM = "drawer_locked_item";
  // Minimum ticks between left-click extractions per player, roughly one swing cycle.
  private static final long LEFT_CLICK_COOLDOWN_TICKS = 6L;

  public BlockDrawer(Identifier id) {
    super(Block.Properties.of().strength(5.0F, 1200.0F).sound(SoundType.STONE).setId(ResourceKey.create(Registries.BLOCK, id)));
    this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(FACING);
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext ctx) {
    return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
  }

  @Override
  public RenderShape getRenderShape(BlockState bs) {
    return RenderShape.MODEL;
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new TileDrawer(pos, state);
  }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
    return createTickerHelper(type, SsnRegistry.Tiles.DRAWER.get(),
        world.isClientSide() ? TileDrawer::clientTick : TileDrawer::serverTick);
  }

  @Override
  public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
    super.setPlacedBy(world, pos, state, placer, stack);
    if (world.isClientSide()) {
      return;
    }
    BlockEntity be = world.getBlockEntity(pos);
    if (!(be instanceof TileDrawer drawer)) {
      return;
    }
    CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
    if (cd == null) {
      return;
    }
    CompoundTag tag = cd.copyTag();
    if (!tag.contains(NBT_LOCKED_ITEM)) {
      return;
    }
    Identifier rl = Identifier.tryParse(tag.getStringOr(NBT_LOCKED_ITEM, ""));
    if (rl == null) {
      return;
    }
    Item item = BuiltInRegistries.ITEM.get(rl).map(net.minecraft.core.Holder::value).orElse(null);
    if (item == null) {
      return;
    }
    drawer.setLockedStack(new ItemStack(item));
  }

  // Right-click holding an item.
  //   shift + item              -> explicit imprint (does not consume)
  //   matching item             -> insert held stack (single-click) or drain inventory (double-click within 10t)
  //   non-matching, drawer empty -> auto-imprint + insert
  //   non-matching, locked      -> pass
  @Override
  public InteractionResult useItemOn(ItemStack held, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
    if (world.isClientSide()) {
      return InteractionResult.SUCCESS;
    }
    BlockEntity be = world.getBlockEntity(pos);
    if (!(be instanceof TileDrawer drawer)) {
      return InteractionResult.PASS;
    }
    // Shift + item -> explicit imprint (matches Storage Drawers "lock to this item" feel).
    if (player.isSecondaryUseActive()) {
      if (!canImprint(held)) {
        return InteractionResult.CONSUME;
      }
      drawer.setLockedStack(held.copyWithCount(1));
      playImprintSound(world, pos);
      return InteractionResult.CONSUME;
    }
    ItemStack locked = drawer.getLockedStack();
    // Auto-imprint: first right-click with an item on an empty drawer.
    if (locked.isEmpty()) {
      if (!canImprint(held)) {
        return InteractionResult.PASS;
      }
      drawer.setLockedStack(held.copyWithCount(1));
      playImprintSound(world, pos);
      locked = drawer.getLockedStack();
    }
    if (locked.getItem() != held.getItem()) {
      return InteractionResult.PASS;
    }
    if (!drawer.isConnected()) {
//      LOGGER.debug("[drawer] insert skipped, not connected at {}", pos);
      return InteractionResult.CONSUME;
    }
    // Double-right-click within 10t with matching item -> drain ALL matching from inventory.
    long now = world.getGameTime();
    Long last = drawer.getLastRightClickTick(player.getUUID());
    drawer.setLastRightClickTick(player.getUUID(), now);
    if (last != null && (now - last) <= 10L) {
      drainMatchingFromInventory(player, drawer);
    }
    else {
      int heldBefore = held.getCount();
      ItemStack toInsert = held.copy();
      int remainder = drawer.insertIntoNetwork(toInsert);
      int consumed = heldBefore - remainder;
//      LOGGER.debug("[drawer] insert: item={} heldBefore={} remainder={} consumed={}",
//          held.getItem(), heldBefore, remainder, consumed);
      if (consumed > 0) {
        ItemStack newHeld = remainder == 0 ? ItemStack.EMPTY : held.copyWithCount(remainder);
        player.setItemInHand(hand, newHeld);
      }
    }
    world.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.25F, 1.0F);
    return InteractionResult.CONSUME;
  }

  // Right-click empty hand.
  //   shift -> clear imprint
  //   non-shift -> no-op (left-click is the extract gesture)
  @Override
  public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
    if (world.isClientSide()) {
      return InteractionResult.SUCCESS;
    }
    BlockEntity be = world.getBlockEntity(pos);
    if (!(be instanceof TileDrawer drawer)) {
      return InteractionResult.PASS;
    }
    if (player.isSecondaryUseActive()) {
      drawer.setLockedStack(ItemStack.EMPTY);
      playImprintSound(world, pos);
      return InteractionResult.CONSUME;
    }
    return InteractionResult.CONSUME;
  }

  // Left-click handler is routed through PlayerInteractEvent.LeftClickBlock so we can extract
  // on a tap. Only creative mode needs the vanilla break itself canceled: a single creative
  // click would otherwise insta-delete the whole node before extraction has a chance to run.
  // Survival mining is slow enough (hardness-based) that we leave it completely alone here -
  // extraction happens per-tap alongside it, and holding the click through still breaks the
  // block normally, same as 1.21.1.
  public static void handleLeftClick(PlayerInteractEvent.LeftClickBlock event) {
    Level world = event.getLevel();
    BlockPos pos = event.getPos();
    BlockState state = world.getBlockState(pos);
    if (!(state.getBlock() instanceof BlockDrawer)) {
      return;
    }
    BlockEntity be = world.getBlockEntity(pos);
    if (!(be instanceof TileDrawer drawer)) {
      return;
    }
    // Empty drawer behaves like a normal block: vanilla break path applies.
    if (drawer.getLockedStack().isEmpty()) {
      return;
    }
    Player player = event.getEntity();
    if (player.getAbilities().instabuild) {
      event.setCanceled(true);
    }
    boolean shift = player.isSecondaryUseActive();
    if (world.isClientSide()) {
      // Purely cosmetic: only swing if we're not already mid-animation, so held clicks
      // don't spam-restart it. Not used for gating - see server-side cooldown below.
      if (player.swingTime == 0) {
        player.swing(event.getHand());
      }
      return;
    }
    // Rate-limit extraction ourselves (~6 ticks, one swing cycle) using our own per-player
    // clock. We used to gate on vanilla player.swingTime, but that field is also mutated by
    // the separate arm-swing network packet and proved unreliable here - it could read
    // nonzero (even -1) on essentially every attempt, blocking extraction outright.
    long now = world.getGameTime();
    Long lastExtract = drawer.getLastLeftClickTick(player.getUUID());
    if (lastExtract != null && (now - lastExtract) < LEFT_CLICK_COOLDOWN_TICKS) {
      return;
    }
    drawer.setLastLeftClickTick(player.getUUID(), now);
    if (!drawer.isConnected()) {
//      LOGGER.debug("[drawer] extract skipped, not connected at {}", pos);
      return;
    }
    int extractCount = shift
        ? drawer.getLockedStack().getMaxStackSize()
        : 1;
    ItemStack got = drawer.extractFromNetwork(extractCount);
//    LOGGER.debug("[drawer] extract: locked={} extractCount={} got={}", drawer.getLockedStack().getItem(), extractCount, got);
    if (got.isEmpty()) {
      return;
    }
    giveOrDrop(player, got);
    world.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.25F, 1.0F);
  }

  private void drainMatchingFromInventory(Player player, TileDrawer drawer) {
    Inventory inv = player.getInventory();
    Item lockedItem = drawer.getLockedStack().getItem();
    var items = inv.getNonEquipmentItems();
    for (int i = 0; i < items.size(); i++) {
      ItemStack s = items.get(i);
      if (s.isEmpty() || s.getItem() != lockedItem) {
        continue;
      }
      ItemStack toInsert = s.copy();
      int remainder = drawer.insertIntoNetwork(toInsert);
      items.set(i, remainder == 0 ? ItemStack.EMPTY : s.copyWithCount(remainder));
    }
  }

  private static boolean canImprint(ItemStack stack) {
    // For v1: only items that stack to more than 1 can be imprinted.
    return !stack.isEmpty() && stack.getMaxStackSize() > 1;
  }

  static void giveOrDrop(Player player, ItemStack stack) {
    if (!player.getInventory().add(stack)) {
      player.drop(stack, false);
    }
  }

  private static void playImprintSound(Level world, BlockPos pos) {
    world.playSound(null, pos, SoundEvents.NOTE_BLOCK_BIT.value(), SoundSource.BLOCKS, 0.4F, 1.6F);
  }

  // Silk-touch preserves the locked imprint by dropping a stamped BlockItem with CUSTOM_DATA.
  @Override
  public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
    if (!level.isClientSide() && !player.isCreative()) {
      BlockEntity be = level.getBlockEntity(pos);
      if (be instanceof TileDrawer drawer
          && !drawer.getLockedStack().isEmpty()
          && hasSilkTouch(level, player.getMainHandItem())) {
        ItemStack stamped = new ItemStack(SsnRegistry.Items.DRAWER.get());
        CompoundTag tag = new CompoundTag();
        Identifier rl = BuiltInRegistries.ITEM.getKey(drawer.getLockedStack().getItem());
        tag.putString(NBT_LOCKED_ITEM, rl.toString());
        stamped.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        Block.popResource(level, pos, stamped);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 35);
        return state;
      }
    }
    return super.playerWillDestroy(level, pos, state, player);
  }

  private static boolean hasSilkTouch(Level level, ItemStack tool) {
    if (tool.isEmpty()) {
      return false;
    }
    Holder<Enchantment> silk = level.registryAccess()
        .lookupOrThrow(Registries.ENCHANTMENT)
        .getOrThrow(Enchantments.SILK_TOUCH);
    return EnchantmentHelper.getItemEnchantmentLevel(silk, tool) > 0;
  }

  @Override
  public BlockState rotate(BlockState state, Rotation rot) {
    return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
  }

  @Override
  public BlockState mirror(BlockState state, Mirror mir) {
    return state.rotate(mir.getRotation(state.getValue(FACING)));
  }

  @Override
  public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    super.appendHoverText(stack, context, tooltip, flag);

    CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
    if (cd != null) {
      CompoundTag tag = cd.copyTag();
      if (tag.contains(NBT_LOCKED_ITEM)) {
        Identifier rl = Identifier.tryParse(tag.getStringOr(NBT_LOCKED_ITEM, ""));
        if (rl != null) {
          Item item = BuiltInRegistries.ITEM.get(rl).map(net.minecraft.core.Holder::value).orElse(null);
          if (item != null) {
            tooltip.add(Component.translatable("block.storagenetwork.drawer.imprint", new ItemStack(item).getHoverName()).withStyle(ChatFormatting.AQUA));
          }
        }
      }
    }
  }

  // Helper available to BlockItem subclasses; we use the vanilla BlockItem.
//  public static ItemStack stampedItem(ItemStack baseDrawer, ItemStack locked) {
//    if (locked.isEmpty()) {
//      return baseDrawer;
//    }
//    CompoundTag tag = new CompoundTag();
//    tag.putString(NBT_LOCKED_ITEM, BuiltInRegistries.ITEM.getKey(locked.getItem()).toString());
//    baseDrawer.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
//    return baseDrawer;
//  }

  // Suppress unused-warning for BlockItem import in case the helper is dropped later.
  @SuppressWarnings("unused")
  private static final Class<?> KEEP_BLOCKITEM_IMPORT = BlockItem.class;
}
