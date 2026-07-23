package com.lothrazar.storagenetwork.api.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

public interface GuiPrivate {
  //  void renderStackToolTip(ItemStack stack, int x, int y);
  //  void renderTooltip(List<String> t, int x, int y);
  //  void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor);
  //  void drawGradient(GuiGraphicsExtractor ms, int j1, int k1, int i, int j, int k, int l);

  int getGuiTop();

  int getGuiLeft();

  boolean isInRegion(int x, int y, int width, int height, double mouseX, double mouseY);

  void renderStackTooltip(GuiGraphicsExtractor ms, ItemStack stack, int i, int j);
}
