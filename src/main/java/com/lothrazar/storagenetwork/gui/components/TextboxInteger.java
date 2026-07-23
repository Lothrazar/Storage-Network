package com.lothrazar.storagenetwork.gui.components;
//package com.lothrazar.cyclic.gui;

import com.lothrazar.storagenetwork.network.CableIOMessage;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;

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
  public boolean keyPressed(KeyEvent event) {
    if (event.key() == KEY_BACKSPACE || event.key() == KEY_DELETE) {
      saveValue();
    }
    return super.keyPressed(event);
  }

  private void saveValue() {
    ClientPacketDistributor.sendToServer(new CableIOMessage(CableIOMessage.CableMessageType.SYNC_OP_TEXT.ordinal(), this.getCurrent(), false));
  }

  @Override
  public boolean charTyped(CharacterEvent event) {
    if (!Character.isDigit(event.codepoint())) {
      return false;
    }
    boolean worked = super.charTyped(event);
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
