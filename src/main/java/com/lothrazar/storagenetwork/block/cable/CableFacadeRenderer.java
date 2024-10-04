package com.lothrazar.storagenetwork.block.cable;

import com.lothrazar.storagenetwork.registry.ConfigRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

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
    if (ConfigRegistry.enableFacades.get() && te.getFacadeState() != null) {
      BlockState facadeState = te.getFacadeState();
      BakedModel model = this.brd.getBlockModel(facadeState);
      VertexConsumer vertexConsumer = VertexMultiConsumer.create(ibuffer.getBuffer(RenderType.solid()));
      renderer.tesselateBlock(te.getLevel(), model, facadeState, te.getBlockPos(),
          matrixStack, vertexConsumer, false, te.getLevel().random, packedLight, packedOverlay,
          ModelData.EMPTY, RenderType.solid());
    }
  }
}
