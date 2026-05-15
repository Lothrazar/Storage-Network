package com.lothrazar.storagenetwork.gui.components;

import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ButtonRequest extends Button {

  public static enum TextureEnum {

    ALLOWLIST, IGNORELIST, SORT_AMT, SORT_MOD, SORT_NAME, SORT_UP, SORT_DOWN, JEI_RED, JEI_GREEN, IMPORT, PLUS, MINUS, REDSTONETRUE, REDSTONEFALSE, RED, GREY, CRAFTCLEAR;

    public int getX() {
      switch (this) {
        case REDSTONETRUE:
          return 192;
        case REDSTONEFALSE:
          return 176;
        case IGNORELIST:
          return 195;
        case ALLOWLIST:
          return 177;
        case RED:
          return 179;
        case GREY:
          return 197;
        case SORT_NAME:
          return 198;
        case SORT_AMT:
          return 209;
        case SORT_MOD:
          return 221;
        case JEI_RED:
          return 187;
        case JEI_GREEN:
          return 175;
        case SORT_UP:
          return 187;
        case SORT_DOWN:
          return 175;
        case IMPORT:
          return 176;
        case PLUS:
          return 196;
        case MINUS:
          return 177;
        case CRAFTCLEAR:
          return 209;
        default:
          return 0;
      }
    }

    public int getY() {
      switch (this) {
        case REDSTONETRUE:
          return 96;
        case REDSTONEFALSE:
          return 96;
        case IGNORELIST:
        case ALLOWLIST:
          return 80;
        case RED:
        case GREY:
          return 82;
        case SORT_UP:
        case SORT_DOWN:
        case SORT_AMT:
        case SORT_MOD:
        case SORT_NAME:
          return 127;
        case JEI_RED:
        case JEI_GREEN:
          return 140;
        case IMPORT:
          return 156;
        case PLUS:
          return 13;
        case MINUS:
          return 13;
        case CRAFTCLEAR:
          return 53;
        default:
          return 0;
      }
    }
  }

  private static final int SIZE = 16;

  public ButtonRequest setTexture(ResourceLocation texture) {
    this.texture = texture;
    return this;
  }

  private ResourceLocation texture;
  private TextureEnum textureId = null;

  public ResourceLocation getTexture() {
    return texture;
  }

  public ButtonRequest(int xPos, int yPos, String displayString, OnPress handler, CreateNarration narration) {
    super(xPos, yPos, SIZE, SIZE, Component.translatable(displayString), handler, narration);
    texture = ResourceLocation.fromNamespaceAndPath(StorageNetworkMod.MODID, "textures/gui/cable.png");
  }

  private int getTextureY() {
    int i = 1;
    if (!this.active) {
      i = 0;
    }
    else if (this.isHoveredOrFocused()) {
      i = 2;
    }
    return i;// 46 + i * 20;
  }

  @Override
  public void renderWidget(GuiGraphics ms, int mouseX, int mouseY, float partial) {
    super.renderWidget(ms, mouseX, mouseY, partial);
    //    Minecraft minecraft = Minecraft.getInstance();
    //    minecraft.getTextureManager().bind(getTexture());
    RenderSystem.setShader(GameRenderer::getPositionTexShader);
    RenderSystem.setShaderTexture(0, getTexture());
    int k = getTextureY(); // getYImage ()
    RenderSystem.enableBlend();
    RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
    RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    ms.blit(getTexture(), this.getX(), this.getY(),
        160 + SIZE * k, 52,
        width, height);
    if (textureId != null) {
      ms.blit(getTexture(), this.getX(), this.getY(),
          textureId.getX(), textureId.getY(),
          width, height);
    }
    else {
      ms.drawCenteredString(Minecraft.getInstance().font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, 2210752);
    }
  }

  public TextureEnum getTextureId() {
    return textureId;
  }

  public void setTextureId(TextureEnum textureId) {
    this.textureId = textureId;
  }
}
