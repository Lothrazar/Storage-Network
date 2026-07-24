package com.lothrazar.storagenetwork.block.receiver;

import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.registry.ClientEventRegistry;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class ScreenNetworkReceiver extends AbstractContainerScreen<ContainerNetworkReceiver> {

  private final Identifier texture = Identifier.fromNamespaceAndPath(StorageNetworkMod.MODID, "textures/gui/receiver.png");

  public ScreenNetworkReceiver(ContainerNetworkReceiver container, Inventory inv, Component name) {
    super(container, inv, name, 176, 166);
    this.inventoryLabelY = this.imageHeight - 94;
  }

  @Override
  public void extractRenderState(GuiGraphicsExtractor ms, int mouseX, int mouseY, float partialTicks) {
    super.extractRenderState(ms, mouseX, mouseY, partialTicks);
    drawStatus(ms);
    drawGhostHintTooltip(ms, mouseX, mouseY);
  }

  private void drawGhostHintTooltip(GuiGraphicsExtractor ms, int mouseX, int mouseY) {
    if (menu.getSlot(0).hasItem()) {
      return;
    }
    int sx = leftPos + ContainerNetworkReceiver.UPGRADE_X;
    int sy = topPos + ContainerNetworkReceiver.UPGRADE_Y;
    if (mouseX >= sx && mouseX < sx + 16 && mouseY >= sy && mouseY < sy + 16) {
      ms.setTooltipForNextFrame(font, Component.translatable("screen.storagenetwork.receiver.slothint").withStyle(ChatFormatting.GRAY), mouseX, mouseY);
    }
  }

  private void drawStatus(GuiGraphicsExtractor ms) {
    TileNetworkReceiver tile = menu.tile;
    if (tile == null) {
      return;
    }
    int x = leftPos + 8;
    int y = topPos + 18;
    if (!tile.isBound()) {
      ms.text(font, Component.translatable("screen.storagenetwork.receiver.unbound").withStyle(ChatFormatting.GRAY), x, y, 0xFFFFFF, false);
      return;
    }
    DimPos m = tile.getBoundMaster();
    Component status = menu.isActiveSynced()
        ? Component.translatable("screen.storagenetwork.receiver.active").withStyle(ChatFormatting.GREEN)
        : Component.translatable("screen.storagenetwork.receiver.dormant").withStyle(ChatFormatting.YELLOW);
    ms.text(font, status, x, y, 0xFFFFFF, false);
    if (m != null) {
      ms.text(font, m.makeTooltip(), x, y + 40, 0xFFFFFF, false);
    }
  }

  @Override
  public void extractBackground(GuiGraphicsExtractor ms, int mouseX, int mouseY, float partialTicks) {
    super.extractBackground(ms, mouseX, mouseY, partialTicks);
    int xCenter = (width - imageWidth) / 2;
    int yCenter = (height - imageHeight) / 2;
    ms.blit(RenderPipelines.GUI_TEXTURED, texture, xCenter, yCenter, 0, 0, imageWidth, imageHeight, 256, 256);
    ms.blit(RenderPipelines.GUI_TEXTURED, ClientEventRegistry.SLOT, leftPos + ContainerNetworkReceiver.UPGRADE_X - 1, topPos + ContainerNetworkReceiver.UPGRADE_Y - 1, 0, 0, 18, 18, 18, 18);
    drawGhostUpgrade(ms);
  }

  private void drawGhostUpgrade(GuiGraphicsExtractor ms) {
    if (menu.getSlot(0).hasItem()) {
      return;
    }
    int sx = leftPos + ContainerNetworkReceiver.UPGRADE_X;
    int sy = topPos + ContainerNetworkReceiver.UPGRADE_Y;
    // RenderSystem#setShaderColor is gone in 26.1 (shader-level alpha tint has no replacement
    // hook on GuiGraphicsExtractor#fakeItem), so the ghost preview now renders at full opacity
    // instead of the old 35% faded look.
    ms.fakeItem(new ItemStack(SsnRegistry.Items.CHUNKLOAD_UPGRADE.get()), sx, sy);
  }
}
