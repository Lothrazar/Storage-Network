package com.lothrazar.storagenetwork.block.request;

import java.util.List;
import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.api.EnumSortType;
import com.lothrazar.storagenetwork.api.IGuiNetwork;
import com.lothrazar.storagenetwork.gui.NetworkWidget;
import com.lothrazar.storagenetwork.gui.NetworkWidget.NetworkScreenSize;
import com.lothrazar.storagenetwork.gui.TextboxInteger;
import com.lothrazar.storagenetwork.jei.JeiHooks;
import com.lothrazar.storagenetwork.network.ClearRecipeMessage;
import com.lothrazar.storagenetwork.network.RequestMessage;
import com.lothrazar.storagenetwork.network.SettingsSyncMessage;
import com.lothrazar.storagenetwork.registry.PacketRegistry;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

/**
 * Base class for Request table inventory and Remote inventory
 */
public class ScreenNetworkTable extends AbstractContainerScreen<ContainerNetworkCraftingTable> implements IGuiNetwork {

  private static final int HEIGHT = 256;
  public static final int WIDTH = 176;
  private final ResourceLocation texture = new ResourceLocation(StorageNetworkMod.MODID, "textures/gui/request.png");
  private final NetworkWidget network;
  private TileRequest tile;
  private int topOffset;

  public ScreenNetworkTable(ContainerNetworkCraftingTable container, Inventory inv, Component name) {
    super(container, inv, name);
    tile = container.getTileRequest();
    network = new NetworkWidget(this, NetworkScreenSize.NORMAL);
    imageWidth = WIDTH;
    imageHeight = HEIGHT;
  }

  @Override
  public void drawGradient(GuiGraphics ms, int x, int y, int x2, int y2, int u, int v) {
    ms.fillGradient(x, y, x2, y2, u, v);
  }

  @Override
  public void setStacks(List<ItemStack> stacks) {
    network.stacks = stacks;
  }

  @Override
  public void renderStackTooltip(GuiGraphics ms, ItemStack stack, int mousex, int mousey) {
    ms.renderTooltip(font, stack, mousex, mousey);
  }

  @Override
  public int getGuiTopFixJei() {
    return super.getGuiTop() + topOffset;
  }

  @Override
  public void init() {
    super.init();
    int searchLeft = leftPos + 81, searchTop = getGuiTopFixJei() + 96, width = 85;
    network.searchBar = new EditBox(font,
        searchLeft, searchTop,
        width, font.lineHeight, null);
    network.searchBar.setMaxLength(30);
    network.initSearchbar();
    network.initButtons();
    addRenderableWidget(network.directionBtn);
    addRenderableWidget(network.sortBtn);
    addRenderableWidget(network.focusBtn);
    if (this.getAutoFocus()) {
      network.searchBar.setFocused(true);
    }
    if (ModList.get().isLoaded("jei")) {
      addRenderableWidget(network.jeiBtn);
    }
  }

  @Override
  public void render(GuiGraphics ms, int mouseX, int mouseY, float partialTicks) {
    renderBackground(ms);
    super.render(ms, mouseX, mouseY, partialTicks);
    this.renderTooltip(ms, mouseX, mouseY);
    network.searchBar.render(ms, mouseX, mouseY, partialTicks);
    network.render();
  }

  @Override
  public void syncDataToServer() {
    PacketRegistry.INSTANCE.sendToServer(new SettingsSyncMessage(getPos(), getDownwards(), getSort(), this.isJeiSearchSynced(), tile.getAutoFocus()));
  }

  @Override
  public boolean getDownwards() {
    return tile.isDownwards();
  }

  @Override
  public boolean isJeiSearchSynced() {
    return tile.isJeiSearchSynced();
  }

  @Override
  public void setJeiSearchSynced(boolean val) {
    tile.setJeiSearchSynced(val);
  }

  @Override
  public void setDownwards(boolean d) {
    tile.setDownwards(d);
  }

  @Override
  public EnumSortType getSort() {
    return tile.getSort();
  }

  @Override
  public void setSort(EnumSortType s) {
    tile.setSort(s);
  }

  public BlockPos getPos() {
    return tile.getBlockPos();
  }

  @Override
  public void renderBg(GuiGraphics ms, float partialTicks, int mouseX, int mouseY) {
    //    minecraft.getTextureManager().bind(texture);
    //    RenderSystem.setShader(GameRenderer::getPositionTexShader);
    //    RenderSystem.setShaderTexture(0, texture);
    int xCenter = (width - imageWidth) / 2;
    int yCenter = (height - imageHeight) / 2;
    ms.blit(texture, xCenter, yCenter, 0, 0, imageWidth, imageHeight);
    //good stuff
    network.applySearchTextToSlots();
    network.renderItemSlots(ms, mouseX, mouseY, font);
  }

  @Override
  public void renderLabels(GuiGraphics ms, int mouseX, int mouseY) {
    network.drawGuiContainerForegroundLayer(ms, mouseX, mouseY, font);
  }

  boolean isScrollable(double x, double y) {
    int scrollHeight = 135;
    return this.isHovering(0, 0,
        this.width - 8, scrollHeight,
        x, y);
  }

  /**
   * Negative is down; positive is up.
   *
   * @param x
   * @param y
   * @param mouseButton
   * @return
   */
  @Override
  public boolean mouseScrolled(double x, double y, double mouseButton) {
    super.mouseScrolled(x, y, mouseButton);
    if (isScrollable(x, y) && mouseButton != 0) {
      network.mouseScrolled(mouseButton);
    }
    return true;
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
    super.mouseClicked(mouseX, mouseY, mouseButton);
    network.mouseClicked(mouseX, mouseY, mouseButton);
    //recipe clear thingy
    //TODO: network needs isCrafting and isPointInRegion access to refactor
    // OR make real button lol
    int rectX = 63;
    int rectY = 110;
    if (isHovering(rectX, rectY, 7, 7, mouseX, mouseY)) {
      PacketRegistry.INSTANCE.sendToServer(new ClearRecipeMessage());
      PacketRegistry.INSTANCE.sendToServer(new RequestMessage(0, ItemStack.EMPTY, false, false));
      return true;
    }
    return true;
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int b) {
    InputConstants.Key mouseKey = InputConstants.getKey(keyCode, scanCode);
    if (keyCode == TextboxInteger.KEY_ESC) { //ESCAPE
      minecraft.player.closeContainer();
      return true; // Forge MC-146650: Needs to return true when the key is handled.
    }
    if (network.searchBar.isFocused()) {
      network.searchBar.keyPressed(keyCode, scanCode, b);
      if (keyCode == TextboxInteger.KEY_BACKSPACE) {
        network.syncTextToJei();
      }
      return true;
    }
    else if (!network.stackUnderMouse.isEmpty()) {
      try {
        JeiHooks.testJeiKeybind(mouseKey, network.stackUnderMouse);
      }
      catch (Throwable e) {
        StorageNetworkMod.LOGGER.error("JEI compat issue ", e);
        //its ok JEI not installed for maybe an addon mod is ok
      }
    }
    //regardles of above branch, also check this
    if (minecraft.options.keyInventory.isActiveAndMatches(mouseKey)) {

      minecraft.player.closeContainer();
      return true; // Forge MC-146650: Needs to return true when the key is handled.
    }
    return super.keyPressed(keyCode, scanCode, b);
  }

  @Override
  public boolean charTyped(char typedChar, int keyCode) {
    if (network.charTyped(typedChar, keyCode)) {
      return true;
    }
    return false;
  }

  @Override
  public boolean isInRegion(int x, int y, int width, int height, double mouseX, double mouseY) {
    // because its protected and apparently sometimes abstract when compiled
    return super.isHovering(x, y, width, height, mouseX, mouseY);
  }

  @Override
  public boolean getAutoFocus() {
    return tile.getAutoFocus();
  }

  @Override
  public void setAutoFocus(boolean b) {
    tile.setAutoFocus(b);
  }

  @Override
  public NetworkWidget getNetworkWidget() {
    return network;
  }
}
