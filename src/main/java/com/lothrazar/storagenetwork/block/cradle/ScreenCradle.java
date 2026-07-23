package com.lothrazar.storagenetwork.block.cradle;

import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.registry.ClientEventRegistry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class ScreenCradle extends AbstractContainerScreen<ContainerCradle> {

  private final Identifier texture = Identifier.fromNamespaceAndPath(StorageNetworkMod.MODID, "textures/gui/cradle.png");

  public ScreenCradle(ContainerCradle container, Inventory inv, Component name) {
    super(container, inv, name, 176, 166);
    this.inventoryLabelY = this.imageHeight - 94;
  }

  @Override
  public void extractBackground(GuiGraphicsExtractor ms, int mouseX, int mouseY, float partialTicks) {
    super.extractBackground(ms, mouseX, mouseY, partialTicks);
    int xCenter = (width - imageWidth) / 2;
    int yCenter = (height - imageHeight) / 2;
    ms.blit(RenderPipelines.GUI_TEXTURED, texture, xCenter, yCenter, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
    // slot frames behind the held-item 3x3 grid - draw 18x18 frame around each
    for (int i = 0; i < TileCradle.HOLDER_SIZE; i++) {
      int col = i % ContainerCradle.HELD_GRID_COLS;
      int row = i / ContainerCradle.HELD_GRID_COLS;
      ms.blit(RenderPipelines.GUI_TEXTURED, ClientEventRegistry.SLOT,
          leftPos + ContainerCradle.HELD_GRID_X - 1 + col * 18,
          topPos + ContainerCradle.HELD_GRID_Y - 1 + row * 18,
          0, 0, 18, 18, 18, 18);
    }
  }
}
