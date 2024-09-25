package mrriegel.storagenetwork.block.cable.io;

import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;
import com.google.common.collect.Lists;
import mrriegel.storagenetwork.api.data.EnumUpgradeType;
import mrriegel.storagenetwork.block.cable.GuiCable;
import mrriegel.storagenetwork.block.cable.GuiCableButton;
import mrriegel.storagenetwork.gui.ItemSlotNetwork;
import mrriegel.storagenetwork.network.CableDataMessage;
import mrriegel.storagenetwork.network.CableLimitMessage;
import mrriegel.storagenetwork.registry.PacketRegistry;
import mrriegel.storagenetwork.util.inventory.FilterItemStackHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

public class GuiCableIO extends GuiCable {

  ContainerCableIO containerCableIO;
  protected GuiCableButton btnOperationToggle;
  protected ItemSlotNetwork operationItemSlot;
  protected GuiTextField fieldOperationLimit;

  public GuiCableIO(ContainerCableIO containerCableIO) {
    super(containerCableIO);
    this.containerCableIO = containerCableIO;
  }

  @Override
  public FilterItemStackHandler getFilterHandler() {
    return containerCableIO.cap.filters;
  }

  @Override
  public void importSlotsButtonPressed() {
    super.importSlotsButtonPressed();
    // import here for faster responsiveness I guess
    containerCableIO.cap.importFilterStacks();
  }

  @Override
  public void initGui() {
    super.initGui();
    btnWhite.setCustomDrawMethod(guiCableButton -> {
      if (this.containerCableIO.cap.filters.isWhitelist) {
        this.drawTexturedModalRect(guiCableButton.x + 1, guiCableButton.y + 3, 176, 83, 13, 10);
      }
      else {
        this.drawTexturedModalRect(guiCableButton.x + 1, guiCableButton.y + 3, 190, 83, 13, 10);
      }
    });
    Keyboard.enableRepeatEvents(true);
    fieldOperationLimit = new GuiTextField(99, fontRenderer, guiLeft + 50, guiTop + 69, TEXTBOX_WIDTH, fontRenderer.FONT_HEIGHT);
    fieldOperationLimit.setMaxStringLength(5);
    fieldOperationLimit.setEnableBackgroundDrawing(false);
    fieldOperationLimit.setVisible(true);
    fieldOperationLimit.setTextColor(16777215);
    fieldOperationLimit.setFocused(false);
    fieldOperationLimit.setText("" + this.containerCableIO.cap.operationLimit);
    fieldOperationLimit.width = TEXTBOX_WIDTH - 6;
    btnOperationToggle = new GuiCableButton(CableDataMessage.CableMessageType.TOGGLE_MODE, guiLeft + 28, guiTop + 66, "");
    btnOperationToggle.setCustomDrawMethod(guiCableButton -> {
      // TODO: Do these < and > in the GUI need to get swapped?
      if (this.containerCableIO.cap.operationMustBeSmaller) {
        guiCableButton.displayString = "<";
      }
      else {
        guiCableButton.displayString = ">";
      }
    });
    this.addButton(btnOperationToggle);
    operationItemSlot = new ItemSlotNetwork(this, this.containerCableIO.cap.operationStack, guiLeft + 8, guiTop + 66, 1, guiLeft, guiTop, false);
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
    super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
    int xMiddle = (this.width - this.xSize) / 2;
    int yMiddle = (this.height - this.ySize) / 2;
    int u = 176;
    int v = 34;
    for (int ug = 0; ug < 4; ug++) {
      this.drawTexturedModalRect(xMiddle + 97 + ug * SLOT_SIZE, yMiddle + 5, u, v, SLOT_SIZE, SLOT_SIZE);
    }
    if (containerCableIO == null || containerCableIO.cap == null) {
      return;
    }
    if (hasOperationUpgrade(EnumUpgradeType.OPERATION)) {
      btnOperationToggle.enabled = true;
      btnOperationToggle.visible = true;
      this.mc.getTextureManager().bindTexture(texture);
      this.drawTexturedModalRect(xMiddle + 7, yMiddle + 65, u, v, SLOT_SIZE, SLOT_SIZE);//the extra slot
      //also draw textbox
      this.drawTexturedModalRect(xMiddle + 47, yMiddle + 67, 0, 171, TEXTBOX_WIDTH, 12);
      fieldOperationLimit.drawTextBox();
      operationItemSlot.drawSlot(mouseX, mouseY);
    }
    else if (btnOperationToggle != null) {
      btnOperationToggle.enabled = false;
      btnOperationToggle.visible = false;
    }
    checkOreBtn.setIsChecked(containerCableIO.cap.filters.ores);
    checkMetaBtn.setIsChecked(containerCableIO.cap.filters.meta);
    checkNbtBtn.setIsChecked(containerCableIO.cap.filters.nbt);
    fontRenderer.drawString(String.valueOf(containerCableIO.cap.getPriority()),
        guiLeft + 30 - fontRenderer.getStringWidth(String.valueOf(containerCableIO.cap.getPriority())) / 2,
        5 + btnMinus.y, 4210752);
    itemSlotsGhost = Lists.newArrayList();
    int rows = 2;
    int cols = 9;
    int index = 0;
    int y = 26;
    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < cols; col++) {
        ItemStack stack = containerCableIO.cap.filters.getStackInSlot(index);
        int x = 8 + col * SLOT_SIZE;
        itemSlotsGhost.add(new ItemSlotNetwork(this, stack, guiLeft + x, guiTop + y, stack.getCount(), guiLeft, guiTop, true));
        index++;
      }
      //move down to second row 
      y += SLOT_SIZE;
    }
    for (ItemSlotNetwork s : itemSlotsGhost) {
      s.setShowNumbers(hasOperationUpgrade(EnumUpgradeType.STOCK));
      s.drawSlot(mouseX, mouseY);
    }
  }

  @Override
  protected void drawTooltips(int mouseX, int mouseY) {
    super.drawTooltips(mouseX, mouseY);
    if (containerCableIO == null || containerCableIO.cap == null) {
      return;
    }
    if (hasOperationUpgrade(EnumUpgradeType.OPERATION)) {
      operationItemSlot.drawTooltip(mouseX, mouseY);
      if (btnOperationToggle.isMouseOver()) {
        String s = I18n.format("gui.storagenetwork.operate.tooltip",
            I18n.format("gui.storagenetwork.operate.tooltip." + (containerCableIO.cap.operationMustBeSmaller ? "less" : "more")),
            containerCableIO.cap.operationLimit,
            containerCableIO.cap.operationStack != null ? containerCableIO.cap.operationStack.getDisplayName() : "Items");
        this.drawHoveringText(Lists.newArrayList(s), mouseX, mouseY, fontRenderer);
      }
    }
  }

  private boolean hasOperationUpgrade(EnumUpgradeType u) {
    return containerCableIO.cap.upgrades.getUpgradesOfType(u) > 0;
  }

  @Override
  protected void actionPerformed(GuiButton button) throws IOException {
    super.actionPerformed(button);
    if (containerCableIO == null || containerCableIO.cap == null) {
      return;
    }
    int change = GuiScreen.isShiftKeyDown() ? 10 : 1;
    if (GuiScreen.isAltKeyDown()) {
      change *= 5;
    }
    if (button.id == btnMinus.id) {
      containerCableIO.cap.priority -= change;
      PacketRegistry.INSTANCE.sendToServer(new CableDataMessage(button.id, containerCableIO.cap.priority));
    }
    else if (button.id == btnPlus.id) {
      containerCableIO.cap.priority += change;
      PacketRegistry.INSTANCE.sendToServer(new CableDataMessage(button.id, containerCableIO.cap.priority));
    }
    else if (button.id == btnOperationToggle.id) {
      containerCableIO.cap.operationMustBeSmaller = !containerCableIO.cap.operationMustBeSmaller;
      PacketRegistry.INSTANCE.sendToServer(new CableDataMessage(button.id));
    }
  }

  @Override
  protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
    super.mouseClicked(mouseX, mouseY, mouseButton);
    if (containerCableIO.cap.upgrades.getUpgradesOfType(EnumUpgradeType.OPERATION) < 1) {
      return;
    }
    fieldOperationLimit.mouseClicked(mouseX, mouseY, mouseButton);
    if (!operationItemSlot.isMouseOverSlot(mouseX, mouseY)) {
      return;
    }
    ItemStack stackCarriedByMouse = mc.player.inventory.getItemStack().copy();
    operationItemSlot.setStack(stackCarriedByMouse);
    int num = fieldOperationLimit.getText().isEmpty() ? 0 : Integer.valueOf(fieldOperationLimit.getText());
    PacketRegistry.INSTANCE.sendToServer(new CableLimitMessage(num, stackCarriedByMouse));
  }

  @Override
  protected void keyTyped(char typedChar, int keyCode) throws IOException {
    super.keyTyped(typedChar, keyCode);
    if (!this.checkHotbarKeys(keyCode)) {
      Keyboard.enableRepeatEvents(true);
      if (hasOperationUpgrade(EnumUpgradeType.OPERATION)) {
        String s = fieldOperationLimit.getText();
        if (fieldOperationLimit.isFocused() && this.fieldOperationLimit.textboxKeyTyped(typedChar, keyCode)) {
          if (!StringUtils.isNumeric(fieldOperationLimit.getText()) && !fieldOperationLimit.getText().isEmpty()) fieldOperationLimit.setText(s);
          int num = 0;
          try {
            num = fieldOperationLimit.getText().isEmpty() ? 0 : Integer.valueOf(fieldOperationLimit.getText());
          }
          catch (Exception e) {
            fieldOperationLimit.setText("0");
          }
          containerCableIO.cap.operationLimit = num;
          PacketRegistry.INSTANCE.sendToServer(new CableLimitMessage(num, operationItemSlot.getStack()));
        }
      }
    }
  }
}
