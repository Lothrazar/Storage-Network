package com.lothrazar.storagenetwork.block.cradle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class CradleRenderer implements BlockEntityRenderer<TileCradle, CradleRenderer.State> {

  private static final int GRID = 3;
  private static final float CELL = 1F / GRID;
  private static final float SCALE = 0.28F;
  private static final float Y_OFFSET = 1.001F;

  public CradleRenderer(BlockEntityRendererProvider.Context ctx) {}

  @Override
  public State createRenderState() {
    return new State();
  }

  @Override
  public void extractRenderState(TileCradle te, State state, float partial, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
    BlockEntityRenderState.extractBase(te, state, breakProgress);
    Level level = te.getLevel();
    int litLight = level == null
        ? state.lightCoords
        : LevelRenderer.getLightCoords(level, te.getBlockPos().above());
    var resolver = Minecraft.getInstance().getItemModelResolver();
    // Lay each non-empty held stack flat on the top face in a 3x3 grid.
    for (int i = 0; i < TileCradle.HOLDER_SIZE; i++) {
      ItemStack held = te.getHeldStack(i);
      if (held.isEmpty()) {
        state.items[i] = null;
        continue;
      }
      ItemStackRenderState renderState = new ItemStackRenderState();
      resolver.updateForTopItem(renderState, held, ItemDisplayContext.FIXED, level, null, i);
      state.items[i] = renderState;
      state.lightForSlot[i] = litLight;
    }
  }

  @Override
  public void submit(State state, PoseStack stack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
    for (int i = 0; i < TileCradle.HOLDER_SIZE; i++) {
      ItemStackRenderState renderState = state.items[i];
      if (renderState == null) {
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
      renderState.submit(stack, submitNodeCollector, state.lightForSlot[i], OverlayTexture.NO_OVERLAY, -1);
      stack.popPose();
    }
  }

  public static class State extends BlockEntityRenderState {
    final ItemStackRenderState[] items = new ItemStackRenderState[TileCradle.HOLDER_SIZE];
    final int[] lightForSlot = new int[TileCradle.HOLDER_SIZE];
  }
}
