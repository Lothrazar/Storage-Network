package com.lothrazar.storagenetwork.block.receiver;

import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.api.DimPos;
import com.lothrazar.storagenetwork.registry.ClientEventRegistry;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class ScreenNetworkReceiver extends AbstractContainerScreen<ContainerNetworkReceiver> {

  private final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(StorageNetworkMod.MODID, "textures/gui/receiver.png");

  public ScreenNetworkReceiver(ContainerNetworkReceiver container, Inventory inv, Component name) {
    super(container, inv, name);
    this.imageWidth = 176;
    this.imageHeight = 166;
    this.inventoryLabelY = this.imageHeight - 94;
  }

  @Override
  public void render(GuiGraphics ms, int mouseX, int mouseY, float partialTicks) {
    renderBackground(ms, mouseX, mouseY, partialTicks);
    super.render(ms, mouseX, mouseY, partialTicks);
    drawStatus(ms);
    this.renderTooltip(ms, mouseX, mouseY);
    drawGhostHintTooltip(ms, mouseX, mouseY);
  }

  private void drawGhostHintTooltip(GuiGraphics ms, int mouseX, int mouseY) {
    if (menu.getSlot(0).hasItem()) {
      return;
    }
    int sx = leftPos + ContainerNetworkReceiver.UPGRADE_X;
    int sy = topPos + ContainerNetworkReceiver.UPGRADE_Y;
    if (mouseX >= sx && mouseX < sx + 16 && mouseY >= sy && mouseY < sy + 16) {
      ms.renderTooltip(font, Component.translatable("screen.storagenetwork.receiver.slothint").withStyle(ChatFormatting.GRAY), mouseX, mouseY);
    }
  }

  private void drawStatus(GuiGraphics ms) {
    TileNetworkReceiver tile = menu.tile;
    if (tile == null) {
      return;
    }
    int x = leftPos + 8;
    int y = topPos + 18;
    if (!tile.isBound()) {
      ms.drawString(font, Component.translatable("screen.storagenetwork.receiver.unbound").withStyle(ChatFormatting.GRAY), x, y, 0xFFFFFF, false);
      return;
    }
    DimPos m = tile.getBoundMaster();
    Component status = tile.isActive()
        ? Component.translatable("screen.storagenetwork.receiver.active").withStyle(ChatFormatting.GREEN)
        : Component.translatable("screen.storagenetwork.receiver.dormant").withStyle(ChatFormatting.YELLOW);
    ms.drawString(font, status, x, y, 0xFFFFFF, false);
    if (m != null) {
      ms.drawString(font, m.makeTooltip(), x, y + 40, 0xFFFFFF, false);
    }
  }

  @Override
  protected void renderBg(GuiGraphics ms, float partialTicks, int mouseX, int mouseY) {
    int xCenter = (width - imageWidth) / 2;
    int yCenter = (height - imageHeight) / 2;
    ms.blit(texture, xCenter, yCenter, 0, 0, imageWidth, imageHeight);
    ms.blit(ClientEventRegistry.SLOT, leftPos + ContainerNetworkReceiver.UPGRADE_X - 1, topPos + ContainerNetworkReceiver.UPGRADE_Y - 1, 0, 0, 18, 18, 18, 18);
    drawGhostUpgrade(ms);
  }

  private void drawGhostUpgrade(GuiGraphics ms) {
    if (menu.getSlot(0).hasItem()) {
      return;
    }
    int sx = leftPos + ContainerNetworkReceiver.UPGRADE_X;
    int sy = topPos + ContainerNetworkReceiver.UPGRADE_Y;
    RenderSystem.enableBlend();
    RenderSystem.setShaderColor(1f, 1f, 1f, 0.35f);
    ms.renderFakeItem(new ItemStack(SsnRegistry.Items.CHUNKLOAD_UPGRADE.get()), sx, sy);
    RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
  }
}
