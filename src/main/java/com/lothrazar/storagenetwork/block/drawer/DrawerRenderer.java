package com.lothrazar.storagenetwork.block.drawer;

import com.lothrazar.storagenetwork.api.util.UtilInventory;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class DrawerRenderer implements BlockEntityRenderer<TileDrawer> {

  private static final float ITEM_SCALE = 0.5F;
  private static final float TEXT_SCALE = 1F / 64F;
  private static final float FRONT_OFFSET = 0.51F;

  public DrawerRenderer(BlockEntityRendererProvider.Context ctx) {}

  @Override
  public void render(TileDrawer te, float partial, PoseStack ps, MultiBufferSource buffer, int packedLight, int packedOverlay) {
    ItemStack locked = te.getLockedStack();
    if (locked.isEmpty()) {
      return;
    }
    BlockState state = te.getBlockState();
    if (!state.hasProperty(BlockDrawer.FACING)) {
      return;
    }
    Direction facing = state.getValue(BlockDrawer.FACING);
    Level level = te.getLevel();
    int litLight = level == null
        ? packedLight
        : LevelRenderer.getLightColor(level, te.getBlockPos().relative(facing));
    ps.pushPose();
    // Move to center of front face.
    ps.translate(0.5D, 0.5D, 0.5D);
    ps.translate(facing.getStepX() * FRONT_OFFSET, 0, facing.getStepZ() * FRONT_OFFSET);
    // Rotate so item faces outward.
    float yRot = -facing.toYRot();
    ps.mulPose(Axis.YP.rotationDegrees(yRot));
    // Item.
    ps.pushPose();
    ps.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
    ItemRenderer ir = Minecraft.getInstance().getItemRenderer();
    ir.renderStatic(locked, ItemDisplayContext.FIXED, litLight, OverlayTexture.NO_OVERLAY, ps, buffer, level, 0);
    ps.popPose();
    // Count text below item.
    ps.pushPose();
    ps.translate(0, -0.30D, 0.001D);
    ps.scale(TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);
    Font font = Minecraft.getInstance().font;
    String formatted = UtilInventory.formatLargeNumber(te.getCachedCount());
    float halfW = font.width(formatted) / 2.0F;
    font.drawInBatch(Component.literal(formatted), -halfW, 0,
        0xFFFFFF, false, ps.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, packedLight);
    ps.popPose();
    ps.popPose();
  }

}
