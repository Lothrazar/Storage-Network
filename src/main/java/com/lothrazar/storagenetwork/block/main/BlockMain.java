package com.lothrazar.storagenetwork.block.main;

import java.util.List;
import java.util.Map.Entry;
import com.lothrazar.library.block.EntityBlockFlib;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import com.lothrazar.storagenetwork.util.UtilTileEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class BlockMain extends EntityBlockFlib {

  public BlockMain() {
    super(Block.Properties.of().strength(0.5F).sound(SoundType.STONE));
  }

  @Override
  public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
    super.setPlacedBy(worldIn, pos, state, placer, stack);
    if (!worldIn.isClientSide) {
      BlockEntity tileAtPos = worldIn.getBlockEntity(pos);
      if (tileAtPos instanceof TileMain main) {
        main.getNetwork().setShouldRefresh();
      }
    }
  }

  @Override
  public InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos,
      Player playerIn, BlockHitResult result) {
    if (worldIn.isClientSide) {
      return InteractionResult.SUCCESS;
    }
    BlockEntity tileHere = worldIn.getBlockEntity(pos);
    if (!(tileHere instanceof TileMain)) {
      return InteractionResult.PASS;
    }
    //    float hitX, float hitY, float hitZ;
    if (playerIn.getMainHandItem().isEmpty()) {
      displayConnections(playerIn, tileHere);
      return InteractionResult.SUCCESS;
    }
    return InteractionResult.PASS;
  }

  private void displayConnections(Player playerIn, BlockEntity tileHere) {
    TileMain tileMain = (TileMain) tileHere;
    int total = tileMain.getNetwork().getConnectableSize();
    if (total == 0) {
      return;
    }
    playerIn.sendSystemMessage(
        Component.translatable(ChatFormatting.LIGHT_PURPLE +
            UtilTileEntity.lang("chat.main.emptyslots") + tileMain.getNetwork().emptySlots()));
    playerIn.sendSystemMessage(Component.translatable(ChatFormatting.DARK_AQUA +
        UtilTileEntity.lang("chat.main.connectables") + total));
    List<Entry<String, Integer>> listDisplayStrings = tileMain.getNetwork().getDisplayStrings();
    for (Entry<String, Integer> e : listDisplayStrings) {
      playerIn.sendSystemMessage(Component.translatable(ChatFormatting.AQUA + "    " + e.getValue() + ": " + e.getKey()));
    }
  }

  @Override
  public RenderShape getRenderShape(BlockState bs) {
    return RenderShape.MODEL;
  }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
    return createTickerHelper(type, SsnRegistry.Tiles.MASTER.get(), world.isClientSide ? TileMain::clientTick : TileMain::serverTick);
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new TileMain(pos, state);
  }
}
