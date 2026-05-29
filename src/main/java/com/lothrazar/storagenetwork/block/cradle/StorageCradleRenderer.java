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

public class StorageCradleRenderer implements BlockEntityRenderer<TileStorageCradle> {

  public StorageCradleRenderer(BlockEntityRendererProvider.Context ctx) {}

  @Override
  public void render(TileStorageCradle te, float partial, PoseStack stack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
    ItemRenderer ir = Minecraft.getInstance().getItemRenderer();
    // Render each non-empty held stack lined up across the block's top face.
    // 9 stacks fit into the 1-block width: step = 1/9, first stack centered at 1/18.
    final float step = 1F / TileStorageCradle.HOLDER_SIZE;
    final float scale = 0.18F;
    for (int i = 0; i < TileStorageCradle.HOLDER_SIZE; i++) {
      ItemStack held = te.getHeldStack(i);
      if (held.isEmpty()) {
        continue;
      }
      stack.pushPose();
      stack.translate(step * (i + 0.5F), 0.5F, 0.5F);
      stack.scale(scale, scale, scale);
      ir.renderStatic(held, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY, stack, buffer, te.getLevel(), 0);
      stack.popPose();
    }
  }
}
