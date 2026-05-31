package com.lothrazar.storagenetwork.block.cradle;

import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.registry.ClientEventRegistry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ScreenCradle extends AbstractContainerScreen<ContainerCradle> {

  private final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(StorageNetworkMod.MODID, "textures/gui/cradle.png");

  public ScreenCradle(ContainerCradle container, Inventory inv, Component name) {
    super(container, inv, name);
    this.imageWidth = 176;
    this.imageHeight = 166;
    this.inventoryLabelY = this.imageHeight - 94;
  }

  @Override
  public void render(GuiGraphics ms, int mouseX, int mouseY, float partialTicks) {
    renderBackground(ms, mouseX, mouseY, partialTicks);
    super.render(ms, mouseX, mouseY, partialTicks);
    this.renderTooltip(ms, mouseX, mouseY);
  }

  @Override
  protected void renderBg(GuiGraphics ms, float partialTicks, int mouseX, int mouseY) {
    int xCenter = (width - imageWidth) / 2;
    int yCenter = (height - imageHeight) / 2;
    ms.blit(texture, xCenter, yCenter, 0, 0, imageWidth, imageHeight);
    // slot frames behind the held-item 3x3 grid - draw 18x18 frame around each
    for (int i = 0; i < TileCradle.HOLDER_SIZE; i++) {
      int col = i % ContainerCradle.HELD_GRID_COLS;
      int row = i / ContainerCradle.HELD_GRID_COLS;
      ms.blit(ClientEventRegistry.SLOT,
          leftPos + ContainerCradle.HELD_GRID_X - 1 + col * 18,
          topPos + ContainerCradle.HELD_GRID_Y - 1 + row * 18,
          0, 0, 18, 18, 18, 18);
    }
  }
}
