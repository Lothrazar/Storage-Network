package com.lothrazar.storagenetwork.block.cable;

import com.lothrazar.library.util.FacadeUtil;
import com.lothrazar.storagenetwork.registry.ConfigRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class CableFacadeRenderer implements BlockEntityRenderer<TileCable, CableFacadeRenderer.State> {

  public CableFacadeRenderer(BlockEntityRendererProvider.Context ctx) {}

  @Override
  public boolean shouldRenderOffScreen() {
    return true;
  }

  @Override
  public State createRenderState() {
    return new State();
  }

  @Override
  public void extractRenderState(TileCable te, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
    BlockEntityRenderState.extractBase(te, state, breakProgress);
    state.level = te.getLevel();
    state.pos = te.getBlockPos();
    state.facadeState = ConfigRegistry.enableFacades.get() ? te.getFacadeState() : null;
  }

  @Override
  public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
    if (state.facadeState == null || state.level == null) {
      return;
    }
    // FacadeUtil.renderBlockState (FLib) still draws synchronously against a MultiBufferSource;
    // the shared immediate buffer source is the standard escape hatch for ad-hoc rendering from
    // inside the new deferred submit() phase (see 26.1 rendering-pipeline migration notes).
    MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
    FacadeUtil.renderBlockState(state.level, state.pos, buffers, poseStack, state.facadeState, state.lightCoords, OverlayTexture.NO_OVERLAY);
    buffers.endBatch();
  }

  public static class State extends BlockEntityRenderState {
    Level level;
    BlockPos pos;
    BlockState facadeState;
  }
}
