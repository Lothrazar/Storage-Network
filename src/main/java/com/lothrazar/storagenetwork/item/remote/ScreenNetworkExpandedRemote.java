package com.lothrazar.storagenetwork.item.remote;

import com.lothrazar.storagenetwork.api.EnumSortType;
import com.lothrazar.storagenetwork.block.AbstractNetworkScreen;
import com.lothrazar.storagenetwork.api.gui.NetworkScreenSize;
import com.lothrazar.storagenetwork.gui.DefaultNetworkWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

public class ScreenNetworkExpandedRemote extends AbstractNetworkScreen<ContainerNetworkExpandedRemote> {

  private final DefaultNetworkWidget network;
  private final ItemStack remote;

  public ScreenNetworkExpandedRemote(ContainerNetworkExpandedRemote screenContainer, Inventory inv, Component titleIn) {
    super(screenContainer, inv, titleIn, computeWidth(), computeHeight());
    //since the rightclick action forces only MAIN_HAND openings, is ok
    this.remote = screenContainer.getRemote();
    network = new DefaultNetworkWidget(this, NetworkScreenSize.EXPANDED);
  }

  private static int computeWidth() {
    return 256 + 12 * 18; //scrollWidth
  }

  private static int computeHeight() {
    return DefaultNetworkWidget.player.height() + DefaultNetworkWidget.crafting.height()
        + DefaultNetworkWidget.row.height() * NetworkScreenSize.EXPANDED.lines()
        + DefaultNetworkWidget.head.height();
  }

  @Override
  public void init() {
    super.init();
    network.init(this.font);
    addRenderableWidget(network.directionBtn);
    addRenderableWidget(network.sortBtn);
    addRenderableWidget(network.focusBtn);
    if (network.clearGridBtn != null)
      addRenderableWidget(network.clearGridBtn);
    if (this.getAutoFocus()) {
      network.getSearchBar().setFocused(true);
    }
    if (ModList.get().isLoaded("jei")) {
      addRenderableWidget(network.jeiBtn);
    }
  }

  @Override
  public void extractBackground(GuiGraphicsExtractor ms, int mouseX, int mouseY, float partialTicks) {
    super.extractBackground(ms, mouseX, mouseY, partialTicks);
    final int xCenter = (this.width - this.imageWidth) / 2;
    final int yCenter = (this.height - this.imageHeight) / 2;
    network.renderBgExpanded(ms, xCenter, yCenter);
    network.applySearchTextToSlots();
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
  public DefaultNetworkWidget getNetwork() {
    return network;
  }
}
