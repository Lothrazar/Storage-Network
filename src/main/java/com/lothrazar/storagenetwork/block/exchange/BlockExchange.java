package com.lothrazar.storagenetwork.block.exchange;

import java.util.List;
import com.lothrazar.library.block.EntityBlockFlib;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BlockExchange extends EntityBlockFlib {

  public BlockExchange(Identifier id) {
    super(Block.Properties.of().strength(0.5F).sound(SoundType.STONE).setId(ResourceKey.create(Registries.BLOCK, id)));
  }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
    return createTickerHelper(type, SsnRegistry.Tiles.EXCHANGE.get(), world.isClientSide() ? TileExchange::clientTick : TileExchange::serverTick);
  }

  @Override
  public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag advanced) {
    super.appendHoverText(stack, context, tooltip, advanced);
    tooltip.add(Component.translatable("[WARNING: only use on small Storage Networks. A large AE2 network interfaced with a large Storage Network will cause lag] ").withStyle(ChatFormatting.DARK_GRAY));
  }

  @Override
  public RenderShape getRenderShape(BlockState bs) {
    return RenderShape.MODEL;
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new TileExchange(pos, state);
  }
}
