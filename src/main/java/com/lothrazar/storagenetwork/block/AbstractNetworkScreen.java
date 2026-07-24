package com.lothrazar.storagenetwork.block;

import java.util.List;

import com.lothrazar.library.gui.TileableTexture;
import com.lothrazar.storagenetwork.gui.DefaultGuiNetwork;
import com.lothrazar.storagenetwork.gui.components.TextboxInteger;
import com.lothrazar.storagenetwork.compat.jei.JeiHooks;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractNetworkScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> implements DefaultGuiNetwork {

  public static final Logger LOGGER = LogManager.getLogger();
  public AbstractNetworkScreen(T container, Inventory inv, Component name) {
    super(container, inv, name);
  }

  public AbstractNetworkScreen(T container, Inventory inv, Component name, int imageWidth, int imageHeight) {
    super(container, inv, name, imageWidth, imageHeight);
  }

  @Override
  public void setStacks(List<ItemStack> stacks) {
    getNetwork().setStacks(stacks);
  }

//  @Override
//  public void drawGradient(GuiGraphics ms, int x, int y, int x2, int y2, int u, int v) {
//    ms.fillGradient(x, y, x2, y2, u, v);
//  }

  @Override
  public boolean charTyped(CharacterEvent event) {
    if (getNetwork().charTyped((char) event.codepoint(), event.codepoint())) {
      return true;
    }
    return false;
  }

  @Override
  public boolean keyPressed(KeyEvent event) {
    int keyCode = event.key();
    int scanCode = event.scancode();
    int b = event.modifiers();
    InputConstants.Key mouseKey = InputConstants.getKey(event);
    if (keyCode == TextboxInteger.KEY_ESC) {
      minecraft.player.closeContainer();
      return true; // Forge MC-146650: Needs to return true when the key is handled.
    }
    if (getNetwork().getSearchBar().isFocused()) {
      if (keyCode == TextboxInteger.KEY_BACKSPACE) { // BACKSPACE
        getNetwork().syncTextToJei();
      }
      getNetwork().keyPressed(keyCode, scanCode, b);
      return true;
    }
    else if (!getNetwork().getStackUnderMouse().isEmpty()) {
      try {
        JeiHooks.testJeiKeybind(mouseKey, getNetwork().getStackUnderMouse());
      }
      catch (Throwable e) {
        LOGGER.error("Error thrown from JEI API ", e);
      }
    }
    //Regardless of above branch, also check this
    if (minecraft.options.keyInventory.isActiveAndMatches(mouseKey)) {
      minecraft.player.closeContainer();
      return true; // Forge MC-146650: Needs to return true when the key is handled.
    }
    return super.keyPressed(event);
  }

  // used by ItemSlotNetwork and NetworkWidget
  @Override
  public boolean isInRegion(int x, int y, int width, int height, double mouseX, double mouseY) {
    return super.isHovering(x, y, width, height, mouseX, mouseY);
  }

  public boolean isScrollable(double x, double y) {
    var n = getNetwork();
    return isHovering(n.getX(), n.getY(),
        this.width, n.getScrollHeight(),
        x, y);
  }

  @Override
  public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
    super.mouseScrolled(x, y, scrollX, scrollY);
    //<0 going down
    // >0 going up
    if (isScrollable(x, y) && scrollY != 0) {
      getNetwork().mouseScrolled(scrollY);
    }
    return true;
  }

  @Override
  public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
    super.mouseClicked(event, doubleClick);
    getNetwork().mouseClicked(event.x(), event.y(), event.button());
    return true;
  }

  @Deprecated
  protected void blitSegment(GuiGraphicsExtractor graphics, TileableTexture tt, int xpos, int ypos) {
    graphics.blit(RenderPipelines.GUI_TEXTURED, tt.texture(), xpos, ypos, 0, 0, tt.width(), tt.height(), 256, 256);
  }

  @Override
  public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
    super.extractRenderState(graphics, mouseX, mouseY, partialTicks);
    getNetwork().renderSearchBar(graphics, mouseX, mouseY, partialTicks);
    getNetwork().render();
  }

  @Override
  public void renderStackTooltip(GuiGraphicsExtractor graphics, ItemStack stack, int mousex, int mousey) {
    graphics.setTooltipForNextFrame(font, stack, mousex, mousey);
  }

  @Override
  protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    getNetwork().drawGuiContainerForegroundLayer(graphics, mouseX, mouseY, font);
  }
}
