package com.lothrazar.storagenetwork.block.facade;

import com.lothrazar.storagenetwork.block.cable.TileCable;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraftforge.client.model.data.ModelData;

public class TesrCable implements BlockEntityRenderer<TileCable> {

  private BlockRenderDispatcher brd;

  public TesrCable(BlockEntityRendererProvider.Context d) {
    this.brd = d.getBlockRenderDispatcher();
  }

  @Override
  public boolean shouldRenderOffScreen(TileCable te) {
    return true;
  }

  @Override
  public void render(TileCable te, float v, PoseStack matrixStack, MultiBufferSource ibuffer, int partialTicks, int destroyStage) {
    if (te.getFacadeState() != null) {
      brd.renderSingleBlock(te.getFacadeState(), matrixStack, ibuffer, partialTicks, destroyStage, ModelData.EMPTY, RenderType.solid());
    }
  }
}
