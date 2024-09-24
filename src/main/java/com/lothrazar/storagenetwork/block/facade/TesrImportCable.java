package com.lothrazar.storagenetwork.block.facade;

import com.lothrazar.storagenetwork.block.cable.input.TileCableIO;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraftforge.client.model.data.ModelData;

public class TesrImportCable implements BlockEntityRenderer<TileCableIO> {

  private BlockRenderDispatcher brd;

  public TesrImportCable(BlockEntityRendererProvider.Context d) {
    this.brd = d.getBlockRenderDispatcher();
  }

  @Override
  public boolean shouldRenderOffScreen(TileCableIO te) {
    return true;
  }

  @Override
  public void render(TileCableIO te, float v, PoseStack matrixStack, MultiBufferSource ibuffer, int partialTicks, int destroyStage) {
    if (te.getFacadeState() != null) {
      brd.renderSingleBlock(te.getFacadeState(), matrixStack, ibuffer, partialTicks, destroyStage, ModelData.EMPTY, RenderType.solid());
    }
  }
}
