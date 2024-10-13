package com.lothrazar.storagenetwork.gui;
//package com.lothrazar.cyclic.gui;

import com.lothrazar.storagenetwork.network.CableIOMessage;
import com.lothrazar.storagenetwork.registry.PacketRegistry;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;

/**
 * https://github.com/Lothrazar/Cyclic/blob/61887dc2b69541a553bb0259347d13d6f9d7730e/src/main/java/com/lothrazar/cyclic/gui/TextboxInteger.java
 * 
 */
public class TextboxInteger extends EditBox {

  public static final int KEY_ESC = 256;
  public static final int KEY_DELETE = 261;
  public static final int KEY_BACKSPACE = 259;

  public TextboxInteger(Font fontIn, int xIn, int yIn, int widthIn) {
    super(fontIn, xIn, yIn, widthIn, 16, null);
    this.setMaxLength(2);
    this.setBordered(true);
    this.setVisible(true);
    this.setTextColor(16777215);
  }

  @Override
  public void setFocused(boolean onFocusedChanged) {
    super.setFocused(onFocusedChanged);
    saveValue();
  }

  @Override
  public boolean keyPressed(int key, int mx, int my) {
    if (key == KEY_BACKSPACE || key == KEY_DELETE) {
      saveValue();
    }
    return super.keyPressed(key, mx, my);
  }

  private void saveValue() {
    PacketRegistry.INSTANCE.sendToServer(new CableIOMessage(CableIOMessage.CableMessageType.SYNC_OP_TEXT.ordinal(), this.getCurrent(), false));
  }

  @Override
  public boolean charTyped(char chr, int p) {
    if (!Character.isDigit(chr)) {
      return false;
    }
    boolean worked = super.charTyped(chr, p);
    if (worked) {
      saveValue();
    }
    return worked;
  }

  public int getCurrent() {
    try {
      return Integer.parseInt(this.getValue());
    }
    catch (Exception e) {
      return 0;
    }
  }
}
