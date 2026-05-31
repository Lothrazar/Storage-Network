package com.lothrazar.storagenetwork.block.cradle;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.level.Level;
import com.mojang.math.Axis;

public class CradleRenderer implements BlockEntityRenderer<TileCradle> {

  private static final int GRID = 3;
  private static final float CELL = 1F / GRID;
  private static final float SCALE = 0.28F;
  private static final float Y_OFFSET = 1.001F;

  public CradleRenderer(BlockEntityRendererProvider.Context ctx) {}

  @Override
  public void render(TileCradle te, float partial, PoseStack stack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
    ItemRenderer ir = Minecraft.getInstance().getItemRenderer();
    Level level = te.getLevel();
    int litLight = level == null
        ? packedLight
        : LevelRenderer.getLightColor(level, te.getBlockPos().above());
    // Lay each non-empty held stack flat on the top face in a 3x3 grid.
    for (int i = 0; i < TileCradle.HOLDER_SIZE; i++) {
      ItemStack held = te.getHeldStack(i);
      if (held.isEmpty()) {
        continue;
      }
      int row = i / GRID;
      int col = i % GRID;
      float x = CELL * (col + 0.5F);
      float z = CELL * (row + 0.5F);
      stack.pushPose();
      stack.translate(x, Y_OFFSET, z);
      stack.mulPose(Axis.XP.rotationDegrees(90F));
      stack.scale(SCALE, SCALE, SCALE);
      ir.renderStatic(held, ItemDisplayContext.FIXED, litLight, OverlayTexture.NO_OVERLAY, stack, buffer, level, 0);
      stack.popPose();
    }
  }
}
