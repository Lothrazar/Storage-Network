package com.lothrazar.storagenetwork.block.inventory;

import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.api.EnumSortType;
import com.lothrazar.storagenetwork.block.AbstractNetworkScreen;
import com.lothrazar.storagenetwork.api.gui.NetworkScreenSize;
import com.lothrazar.storagenetwork.gui.DefaultNetworkWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.fml.ModList;

public class ScreenNetworkInventory extends AbstractNetworkScreen<ContainerNetworkInventory> {

  protected static final int HEIGHT = 256;
  public static final int WIDTH = 176;
  private final Identifier texture = Identifier.fromNamespaceAndPath(StorageNetworkMod.MODID, "textures/gui/inventory.png");
  protected final DefaultNetworkWidget network;
  private TileInventory tile;

  public ScreenNetworkInventory(ContainerNetworkInventory container, Inventory inv, Component name) {
    super(container, inv, name, WIDTH, HEIGHT);
    tile = container.tile;
    network = new DefaultNetworkWidget(this, NetworkScreenSize.LARGE);
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
  public void extractBackground(GuiGraphicsExtractor ms, int mouseX, int mouseY, float partialTicks) {
    super.extractBackground(ms, mouseX, mouseY, partialTicks);
    int xCenter = (width - imageWidth) / 2;
    int yCenter = (height - imageHeight) / 2;
    ms.blit(RenderPipelines.GUI_TEXTURED, texture, xCenter, yCenter, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
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
