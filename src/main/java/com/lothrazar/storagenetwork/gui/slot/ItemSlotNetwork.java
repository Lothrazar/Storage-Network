package com.lothrazar.storagenetwork.gui.slot;

import com.lothrazar.storagenetwork.api.gui.GuiPrivate;
import com.lothrazar.storagenetwork.api.util.UtilInventory;
import org.joml.Matrix3x2fStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

/**
 * used as the MAIN grid in the network item display
 * <p>
 * also as ghost/filter items in the cable filter slots
 */
public class ItemSlotNetwork {

  private final int x;
  private final int y;
  private int size;
  private final int guiLeft;
  private final int guiTop;
  private boolean showNumbers;
  private final GuiPrivate parent;
  private ItemStack stack;

  public ItemSlotNetwork(GuiPrivate parent, ItemStack stack, int x, int y, int size, int guiLeft, int guiTop, boolean number) {
    this.x = x;
    this.y = y;
    this.size = size;
    this.guiLeft = guiLeft;
    this.guiTop = guiTop;
    setShowNumbers(number);
    this.parent = parent;
    setStack(stack);
  }

  public boolean isMouseOverSlot(int mouseX, int mouseY) {
    return parent.isInRegion(x - guiLeft, y - guiTop, 16, 16, mouseX, mouseY);
  }

  public void drawSlot(GuiGraphicsExtractor poseStack, Font font, int mx, int my) {
    if (!getStack().isEmpty()) {
      //      poseStack.pushPose();
      if (isMouseOverSlot(mx, my)) {
        int j1 = x;
        int k1 = y;
        poseStack.fillGradient(j1, k1, j1 + 16, k1 + 16, -2130706433, -2130706433);
        //        parent.drawGradient(poseStack, j1, k1, j1 + 16, k1 + 16, -2130706433, -2130706433);
      }
      poseStack.item(stack, x, y);
      //      Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(poseStack, getStack(), x, y);
      if (isShowNumbers() && size > 1) {
        String amount;
        //cant sneak in gui
        //default to short form, show full amount if sneak
        if (Minecraft.getInstance().hasShiftDown()) {
          amount = size + "";
        }
        else {
          amount = UtilInventory.formatLargeNumber(size);
        }
        final float scale = 0.85F;
        Matrix3x2fStack pose = poseStack.pose();
        pose.pushMatrix();
        pose.translate(x + 3, y + 3);
        pose.scale(scale, scale);
        pose.translate(-1 * x, -1 * y);
        poseStack.itemDecorations(font, stack, x, y, amount);
        pose.popMatrix();
      }
    }
  }

  public void drawTooltip(GuiGraphicsExtractor ms, int mx, int my) {
    if (isMouseOverSlot(mx, my) && !getStack().isEmpty()) {
      // setTooltipForNextFrame defers rendering until after the GUI-local pose translate
      // has been popped, so it needs absolute screen coords here, not GUI-relative ones.
      parent.renderStackTooltip(ms, getStack(), mx, my);
    }
  }

  public ItemStack getStack() {
    return stack;
  }

  public void setStack(ItemStack stack) {
    this.stack = stack;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  private boolean isShowNumbers() {
    return showNumbers;
  }

  private void setShowNumbers(boolean showNumbers) {
    this.showNumbers = showNumbers;
  }
}
