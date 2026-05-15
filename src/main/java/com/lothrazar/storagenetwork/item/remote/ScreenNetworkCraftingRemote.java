package com.lothrazar.storagenetwork.item.remote;

import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.api.EnumSortType;
import com.lothrazar.storagenetwork.block.AbstractNetworkScreen;
import com.lothrazar.storagenetwork.gui.NetworkScreenSize;
import com.lothrazar.storagenetwork.gui.NetworkWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

public class ScreenNetworkCraftingRemote extends AbstractNetworkScreen<ContainerNetworkCraftingRemote> {

  private static final int HEIGHT = 256;
  private static final int WIDTH = 176;
  private static final ResourceLocation textureCraft = ResourceLocation.fromNamespaceAndPath(StorageNetworkMod.MODID, "textures/gui/request.png");
  private final NetworkWidget network;
  private final ItemStack remote;

  public ScreenNetworkCraftingRemote(ContainerNetworkCraftingRemote screenContainer, Inventory inv, Component titleIn) {
    super(screenContainer, inv, titleIn);
    //since the rightclick action forces only MAIN_HAND openings, is ok
    remote = screenContainer.getRemote();// inv.player.getItemInHand(InteractionHand.MAIN_HAND);
    network = new NetworkWidget(this, NetworkScreenSize.NORMAL);
    imageWidth = WIDTH;
    imageHeight = HEIGHT;
  }

  @Override
  public void init() {
    super.init();
    network.init(this.font);
    addRenderableWidget(network.directionBtn);
    addRenderableWidget(network.sortBtn);
    addRenderableWidget(network.focusBtn);
    if (this.network.getSize().isCrafting())
      addRenderableWidget(network.clearGridBtn);
    if (this.getAutoFocus()) {
      network.searchBar.setFocused(true);
    }
    if (ModList.get().isLoaded("jei")) {
      addRenderableWidget(network.jeiBtn);
    }
  }

  @Override
  protected void renderBg(GuiGraphics ms, float partialTicks, int mouseX, int mouseY) {
    final int xCenter = (width - imageWidth) / 2;
    final int yCenter = (height - imageHeight) / 2;
    ms.blit(textureCraft, xCenter, yCenter, 0, 0, this.imageWidth, this.imageHeight);
    //update network
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
  public NetworkWidget getNetwork() {
    return network;
  }
}
