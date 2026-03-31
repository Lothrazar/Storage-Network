package com.lothrazar.storagenetwork.block;

import java.util.List;
import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.api.IGuiNetwork;
import com.lothrazar.storagenetwork.gui.TileableTexture;
import com.lothrazar.storagenetwork.gui.components.TextboxInteger;
import com.lothrazar.storagenetwork.jei.JeiHooks;
import com.lothrazar.storagenetwork.network.RequestMessage;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;
import com.lothrazar.storagenetwork.registry.PacketRegistry;

public abstract class AbstractNetworkScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> implements IGuiNetwork {

  public AbstractNetworkScreen(T container, Inventory inv, Component name) {
    super(container, inv, name);
  }

  @Override
  public void setStacks(List<ItemStack> stacks) {
    getNetwork().setStacks(stacks);
  }

  @Override
  public void drawGradient(GuiGraphics ms, int x, int y, int x2, int y2, int u, int v) {
    ms.fillGradient(x, y, x2, y2, u, v);
  }

  @Override
  public boolean charTyped(char typedChar, int keyCode) {
    if (getNetwork().charTyped(typedChar, keyCode)) {
      return true;
    }
    return false;
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int mods) {
    InputConstants.Key key = InputConstants.getKey(keyCode, scanCode);
    
    // 1) ESC always closes
    if (keyCode == TextboxInteger.KEY_ESC) {
      minecraft.player.closeContainer();
      return true;
    }

    // SEARCH BAR HANDLES FIRST
    if (getNetwork().searchBar.isFocused()) {
      // swallow inventory key
      if (minecraft.options.keyInventory.isActiveAndMatches(key)) {
        return true;
      }

      boolean handled = getNetwork().searchBar.keyPressed(keyCode, scanCode, mods);

      // catch paste
      if (Screen.hasControlDown() && keyCode == GLFW.GLFW_KEY_V) {
        handled = true;
      }

      if (handled) {
        PacketRegistry.INSTANCE.sendToServer(
            new RequestMessage(0, ItemStack.EMPTY, false, false)
                .withQuery(getNetwork().searchBar.getValue()));
        getNetwork().syncTextToJei();
      }
      return true; 
    }

    // Not focused: inventory key closes
    if (minecraft.options.keyInventory.isActiveAndMatches(key)) {
      minecraft.player.closeContainer();
      return true;
    }

    // JEI Keybinds
    if (!getNetwork().stackUnderMouse.isEmpty()) {
      try {
        JeiHooks.testJeiKeybind(key, getNetwork().stackUnderMouse);
      } catch (Throwable e) {
        StorageNetworkMod.LOGGER.error("Error thrown from JEI API ", e);
      }
    }

    return super.keyPressed(keyCode, scanCode, mods);
  }

  // used by ItemSlotNetwork and NetworkWidget
  @Override
  public boolean isInRegion(int x, int y, int width, int height, double mouseX, double mouseY) {
    return super.isHovering(x, y, width, height, mouseX, mouseY);
  }

  public boolean isScrollable(double x, double y) {
    return isHovering(getNetwork().xNetwork, getNetwork().yNetwork,
        this.width, getNetwork().scrollHeight,
        x, y);
  }

  @Override
  public boolean mouseScrolled(double x, double y, double mouseButton) {
    super.mouseScrolled(x, y, mouseButton);
    //<0 going down
    // >0 going up
    if (isScrollable(x, y) && mouseButton != 0) {
      getNetwork().mouseScrolled(mouseButton);
    }
    return true;
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
    super.mouseClicked(mouseX, mouseY, mouseButton);
    getNetwork().mouseClicked(mouseX, mouseY, mouseButton);

  // SHIFT quick-move from player inventory -> force refresh with current query
  if (Screen.hasShiftDown()
      && (mouseButton == 0 || mouseButton == 1)
      && !getNetwork().inSearchBar(mouseX, mouseY)
      && !isScrollable(mouseX, mouseY)) {
    PacketRegistry.INSTANCE.sendToServer(
        new RequestMessage(0, ItemStack.EMPTY, false, false)
            .withQuery(getNetwork().searchBar.getValue())
    );
  }
    return true;
  }

  @Deprecated
  protected void blitSegment(GuiGraphics ms, TileableTexture tt, int xpos, int ypos) {
    ms.blit(tt.texture(), xpos, ypos, 0, 0, tt.width(), tt.height());
  }

  @Override
  public void render(GuiGraphics ms, int mouseX, int mouseY, float partialTicks) {
    this.renderBackground(ms);
    super.render(ms, mouseX, mouseY, partialTicks);
    this.renderTooltip(ms, mouseX, mouseY);
    getNetwork().searchBar.render(ms, mouseX, mouseY, partialTicks);
    getNetwork().render();
  }

  @Override
  public void renderStackTooltip(GuiGraphics ms, ItemStack stack, int mousex, int mousey) {
    ms.renderTooltip(font, stack, mousex, mousey);
  }

  @Override
  public void renderLabels(GuiGraphics ms, int mouseX, int mouseY) {
    getNetwork().drawGuiContainerForegroundLayer(ms, mouseX, mouseY, font);
  }
}
