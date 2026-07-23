package com.lothrazar.storagenetwork.block.expand;

import com.lothrazar.storagenetwork.api.EnumSortType;
import com.lothrazar.storagenetwork.block.AbstractNetworkScreen;
import com.lothrazar.storagenetwork.api.gui.NetworkScreenSize;
import com.lothrazar.storagenetwork.gui.DefaultNetworkWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.fml.ModList;

public class ScreenNetworkInventoryExpanded extends AbstractNetworkScreen<ContainerNetworkInventoryExpanded> {

  protected final DefaultNetworkWidget network;
  private TileInventoryExpanded tile;

  public ScreenNetworkInventoryExpanded(ContainerNetworkInventoryExpanded container, Inventory inv, Component name) {
    //TODO: refactor this calculation
    super(container, inv, name, computeWidth(), computeHeight());
    tile = container.tile;
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
    if (this.network.getSize().isCrafting())
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
    //get center points from screen size
    final int xCenter = (width - imageWidth) / 2;
    final int yCenter = (height - imageHeight) / 2;
    network.renderBgExpanded(ms,  xCenter, yCenter);
    //update network
    network.applySearchTextToSlots();
    network.renderItemSlots(ms, mouseX, mouseY, font);
  }
  // all the IGUINETWORK implementations

  @Override
  public boolean getDownwards() {
    return tile.isDownwards();
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

  @Override
  public BlockPos getPos() {
    return tile.getBlockPos();
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
  public boolean getAutoFocus() {
    return tile.getAutoFocus();
  }

  @Override
  public void setAutoFocus(boolean b) {
    tile.setAutoFocus(b);
  }

  @Override
  public DefaultNetworkWidget getNetwork() {
    return network;
  }
}
