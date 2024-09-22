package com.lothrazar.storagenetwork.item.remote;

import java.util.List;
import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.api.EnumSortType;
import com.lothrazar.storagenetwork.api.IGuiNetwork;
import com.lothrazar.storagenetwork.gui.NetworkWidget;
import com.lothrazar.storagenetwork.gui.NetworkWidget.NetworkScreenSize;
import com.lothrazar.storagenetwork.jei.JeiHooks;
import com.lothrazar.storagenetwork.network.SettingsSyncMessage;
import com.lothrazar.storagenetwork.registry.PacketRegistry;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

public class ScreenNetworkRemote extends AbstractContainerScreen<ContainerNetworkRemote> implements IGuiNetwork {

  private static final int HEIGHT = 256;
  private static final int WIDTH = 176;
  private static final ResourceLocation texture = new ResourceLocation(StorageNetworkMod.MODID,
      "textures/gui/inventory.png");
  private final NetworkWidget network;
  private final ItemStack remote;
  private int topOffset;

  public ScreenNetworkRemote(ContainerNetworkRemote screenContainer, Inventory inv, Component titleIn) {
    super(screenContainer, inv, titleIn);
    //since the rightclick action forces only MAIN_HAND openings, is ok
    this.remote = screenContainer.getRemote();
    network = new NetworkWidget(this, NetworkScreenSize.LARGE);
    this.imageWidth = WIDTH;
    this.imageHeight = HEIGHT;
  }

  @Override
  public void renderStackTooltip(PoseStack ms, ItemStack stack, int mousex, int mousey) {
    super.renderTooltip(ms, stack, mousex, mousey);
  }

  @Override
  public void setStacks(List<ItemStack> stacks) {
    network.stacks = stacks;
  }

  @Override
  public boolean getDownwards() {
    return ItemStorageCraftingRemote.getDownwards(remote);
  }

  @Override
  public void setDownwards(boolean val) {
    ItemStorageCraftingRemote.setDownwards(remote, val);
  }

  @Override
  public EnumSortType getSort() {
    return ItemStorageCraftingRemote.getSort(remote);
  }

  @Override
  public void setSort(EnumSortType val) {
    ItemStorageCraftingRemote.setSort(remote, val);
  }

  @Override
  public void setAutoFocus(boolean b) {
    ItemStorageCraftingRemote.setAutoFocus(remote, b);
  }

  @Override
  public boolean isJeiSearchSynced() {
    return ItemStorageCraftingRemote.isJeiSearchSynced(remote);
  }

  @Override
  public void setJeiSearchSynced(boolean val) {
    ItemStorageCraftingRemote.setJeiSearchSynced(remote, val);
  }

  @Override
  public int getGuiTopFixJei() {
    return super.getGuiTop() + topOffset;
  }

  @Override
  public void init() {
    super.init();
    int searchLeft = leftPos + 81, searchTop = getGuiTopFixJei() + 160, width = 85;
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
  public void render(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
    this.renderBackground(ms);
    super.render(ms, mouseX, mouseY, partialTicks);
    this.renderTooltip(ms, mouseX, mouseY);
    network.searchBar.render(ms, mouseX, mouseY, partialTicks);
    network.render();
  }

  @Override
  protected void renderBg(PoseStack ms, float partialTicks, int mouseX, int mouseY) {
    //    this.minecraft.getTextureManager().bind(texture);
    RenderSystem.setShader(GameRenderer::getPositionTexShader);
    RenderSystem.setShaderTexture(0, texture);
    int k = (this.width - this.imageWidth) / 2;
    int l = (this.height - this.imageHeight) / 2;
    blit(ms, k, l, 0, 0, this.imageWidth, this.imageHeight);
    network.applySearchTextToSlots();
    network.renderItemSlots(ms, mouseX, mouseY, font);
  }

  @Override
  public void renderLabels(PoseStack ms, int mouseX, int mouseY) {
    network.drawGuiContainerForegroundLayer(ms, mouseX, mouseY, font);
  }

  boolean isScrollable(double x, double y) {
    int scrollHeight = 152;
    return isHovering(0, 0,
        this.width - 8, scrollHeight,
        x, y);
  }

  @Override
  public boolean mouseScrolled(double x, double y, double mouseButton) {
    super.mouseScrolled(x, y, mouseButton);
    //<0 going down
    // >0 going up
    if (isScrollable(x, y) && mouseButton != 0) {
      network.mouseScrolled(mouseButton);
    }
    return true;
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
    super.mouseClicked(mouseX, mouseY, mouseButton);
    network.mouseClicked(mouseX, mouseY, mouseButton);
    return true;
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int b) {
    InputConstants.Key mouseKey = InputConstants.getKey(keyCode, scanCode);
    if (keyCode == 256) {
      minecraft.player.closeContainer();
      return true; // Forge MC-146650: Needs to return true when the key is handled.
    }
    if (network.searchBar.isFocused()) {
      if (keyCode == 259) { // BACKSPACE
        network.syncTextToJei();
      }
      network.searchBar.keyPressed(keyCode, scanCode, b);
      return true;
    }
    else if (!network.stackUnderMouse.isEmpty()) {
      try {
        JeiHooks.testJeiKeybind(mouseKey, network.stackUnderMouse);
      }
      catch (Throwable e) {
        StorageNetworkMod.LOGGER.error("JEI compat issue ", e);
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
  public void drawGradient(PoseStack ms, int x, int y, int x2, int y2, int u, int v) {
    super.fillGradient(ms, x, y, x2, y2, u, v);
  }

  @Override
  public boolean isInRegion(int x, int y, int width, int height, double mouseX, double mouseY) {
    return super.isHovering(x, y, width, height, mouseX, mouseY);
  }

  @Override
  public boolean getAutoFocus() {
    return ItemStorageCraftingRemote.getAutoFocus(remote);
  }

  @Override
  public void syncDataToServer() {
    PacketRegistry.INSTANCE.sendToServer(new SettingsSyncMessage(null, getDownwards(), getSort(), this.isJeiSearchSynced(), this.getAutoFocus()));
  }
}
