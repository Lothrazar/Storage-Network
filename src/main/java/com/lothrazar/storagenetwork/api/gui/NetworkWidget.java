package com.lothrazar.storagenetwork.api.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface NetworkWidget {

  NetworkScreenSize getSize();

  void init(Font font);

  List<ItemStack> getStacks();

  void setStacks(List<ItemStack> stacks);

  void applySearchTextToSlots();

  void clearSearch();

  boolean charTyped(char typedChar, int keyCode);

  void mouseClicked(double mouseX, double mouseY, int mouseButton);

  void mouseScrolled(double mouseButton);

  void renderSearchBar(GuiGraphicsExtractor ms, int mouseX, int mouseY, float partialTicks);

  void render();
  void syncTextToJei();
  void renderItemSlots(GuiGraphicsExtractor ms, int mouseX, int mouseY, Font font);

  void drawGuiContainerForegroundLayer(GuiGraphicsExtractor ms, int mouseX, int mouseY, Font font);

  void renderBgExpanded(GuiGraphicsExtractor ms, int xCenter, int yCenter);

  void keyPressed(int keyCode, int scanCode, int b);

  AbstractWidget getSearchBar();
  ItemStack getStackUnderMouse();
  int getX();
  int getY();
  int getScrollHeight();
}




