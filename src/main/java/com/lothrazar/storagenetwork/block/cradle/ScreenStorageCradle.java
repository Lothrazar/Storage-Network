package com.lothrazar.storagenetwork.block.cradle;

import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.registry.ClientEventRegistry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ScreenStorageCradle extends AbstractContainerScreen<ContainerStorageCradle> {

  private final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(StorageNetworkMod.MODID, "textures/gui/storage_cradle.png");

  public ScreenStorageCradle(ContainerStorageCradle container, Inventory inv, Component name) {
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
    // slot frames behind the held-item row (slots start at 8,34 - draw 18x18 frame around each)
    for (int i = 0; i < ContainerStorageCradle.HELD_ROW_X_COUNT; i++) {
      ms.blit(ClientEventRegistry.SLOT, leftPos + ContainerStorageCradle.HELD_ROW_X - 1 + i * 18, topPos + ContainerStorageCradle.HELD_ROW_Y - 1, 0, 0, 18, 18, 18, 18);
    }
  }
}
