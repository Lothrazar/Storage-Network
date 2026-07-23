package com.lothrazar.storagenetwork.block.drawer;

import com.lothrazar.storagenetwork.api.util.UtilInventory;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class DrawerRenderer implements BlockEntityRenderer<TileDrawer, DrawerRenderer.State> {

  private static final float ITEM_SCALE = 0.5F;
  private static final float TEXT_SCALE = 1F / 64F;
  private static final float FRONT_OFFSET = 0.51F;

  public DrawerRenderer(BlockEntityRendererProvider.Context ctx) {}

  @Override
  public State createRenderState() {
    return new State();
  }

  @Override
  public void extractRenderState(TileDrawer te, State state, float partial, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
    BlockEntityRenderState.extractBase(te, state, breakProgress);
    state.facing = null;
    state.item = null;
    ItemStack locked = te.getLockedStack();
    if (locked.isEmpty()) {
      return;
    }
    BlockState blockState = te.getBlockState();
    if (!blockState.hasProperty(BlockDrawer.FACING)) {
      return;
    }
    Direction facing = blockState.getValue(BlockDrawer.FACING);
    Level level = te.getLevel();
    state.facing = facing;
    state.light = level == null
        ? state.lightCoords
        : LevelRenderer.getLightCoords(level, te.getBlockPos().relative(facing));
    ItemStackRenderState renderState = new ItemStackRenderState();
    Minecraft.getInstance().getItemModelResolver().updateForTopItem(renderState, locked, ItemDisplayContext.FIXED, level, null, 0);
    state.item = renderState;
    state.countText = UtilInventory.formatLargeNumber(te.getCachedCount());
  }

  @Override
  public void submit(State state, PoseStack ps, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
    if (state.item == null || state.facing == null) {
      return;
    }
    Direction facing = state.facing;
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
    state.item.submit(ps, submitNodeCollector, state.light, OverlayTexture.NO_OVERLAY, -1);
    ps.popPose();
    // Count text below item.
    ps.pushPose();
    ps.translate(0, -0.30D, 0.001D);
    ps.scale(TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);
    Font font = Minecraft.getInstance().font;
    float halfW = font.width(state.countText) / 2.0F;
    submitNodeCollector.submitText(ps, -halfW, 0, Component.literal(state.countText).getVisualOrderText(),
        false, Font.DisplayMode.NORMAL, state.light, 0xFFFFFF, 0, 0);
    ps.popPose();
    ps.popPose();
  }

  public static class State extends BlockEntityRenderState {
    Direction facing;
    ItemStackRenderState item;
    int light;
    String countText = "";
  }
}
