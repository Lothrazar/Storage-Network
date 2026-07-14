package com.lothrazar.storagenetwork.block.cable;

import com.lothrazar.library.util.FacadeUtil;
import com.lothrazar.storagenetwork.registry.ConfigRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class CableFacadeRenderer implements BlockEntityRenderer<TileCable> {

  private BlockRenderDispatcher brd;
  private ModelBlockRenderer renderer;

  public CableFacadeRenderer(BlockEntityRendererProvider.Context d) {
    this.brd = d.getBlockRenderDispatcher();
    this.renderer = brd.getModelRenderer();
  }

  @Override
  public boolean shouldRenderOffScreen(TileCable te) {
    return true;
  }

  @Override
  public void render(TileCable te, float v, PoseStack matrixStack, MultiBufferSource ibuffer, int packedLight, int packedOverlay) {
    if (ConfigRegistry.enableFacades.get()) {
      BlockState facadeState = te.getFacadeState();
      if (facadeState != null) {
        FacadeUtil.renderBlockState(te.getLevel(), te.getBlockPos(), brd, renderer, ibuffer,
            matrixStack, facadeState, packedLight, packedOverlay);
      }
    }
  }
}
