package com.lothrazar.storagenetwork.item.remote;

import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.api.EnumSortType;
import com.lothrazar.storagenetwork.block.AbstractNetworkScreen;
import com.lothrazar.storagenetwork.api.gui.NetworkScreenSize;
import com.lothrazar.storagenetwork.gui.DefaultNetworkWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

public class ScreenNetworkRemote extends AbstractNetworkScreen<ContainerNetworkRemote> {

  private static final int HEIGHT = 256;
  private static final int WIDTH = 176;
  private static final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(StorageNetworkMod.MODID, "textures/gui/inventory.png");
  private final DefaultNetworkWidget network;
  private final ItemStack remote;

  public ScreenNetworkRemote(ContainerNetworkRemote screenContainer, Inventory inv, Component titleIn) {
    super(screenContainer, inv, titleIn);
    //since the rightclick action forces only MAIN_HAND openings, is ok
    this.remote = screenContainer.getRemote();
    network = new DefaultNetworkWidget(this, NetworkScreenSize.LARGE);
    this.imageWidth = WIDTH;
    this.imageHeight = HEIGHT;
  }

  @Override
  public void init() {
    super.init();
    network.init(this.font);
    addRenderableWidget(network.directionBtn);
    addRenderableWidget(network.sortBtn);
    addRenderableWidget(network.focusBtn);
    if (this.getAutoFocus()) {
      network.getSearchBar().setFocused(true);
    }
    if (ModList.get().isLoaded("jei")) {
      addRenderableWidget(network.jeiBtn);
    }
  }

  @Override
  protected void renderBg(GuiGraphics ms, float partialTicks, int mouseX, int mouseY) {
    int xCenter = (this.width - this.imageWidth) / 2;
    int yCenter = (this.height - this.imageHeight) / 2;
    ms.blit(texture, xCenter, yCenter, 0, 0, this.imageWidth, this.imageHeight);
    network.applySearchTextToSlots();
    network.renderItemSlots(ms, mouseX, mouseY, font);
  }

  @Override
  public boolean getDownwards() {
    return ItemRemote.getDownwards(remote);
  }

  @Override
  public void setDownwards(boolean val) {
    ItemRemote.setDownwards(remote, val);
  }

  @Override
  public EnumSortType getSort() {
    return ItemRemote.getSort(remote);
  }

  @Override
  public void setSort(EnumSortType val) {
    ItemRemote.setSort(remote, val);
  }

  @Override
  public boolean isJeiSearchSynced() {
    return ItemRemote.isJeiSearchSynced(remote);
  }

  @Override
  public void setJeiSearchSynced(boolean val) {
    ItemRemote.setJeiSearchSynced(remote, val);
  }

  @Override
  public boolean getAutoFocus() {
    return ItemRemote.getAutoFocus(remote);
  }

  @Override
  public void setAutoFocus(boolean b) {
    ItemRemote.setAutoFocus(remote, b);
  }

  @Override
  public DefaultNetworkWidget getNetwork() {
    return network;
  }
}
