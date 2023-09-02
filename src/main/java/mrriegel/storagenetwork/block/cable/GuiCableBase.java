package mrriegel.storagenetwork.block.cable;

import java.io.IOException;
import java.util.List;
import org.lwjgl.input.Mouse;
import com.google.common.collect.Lists;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.gui.ItemSlotNetwork;
import mrriegel.storagenetwork.network.CableDataMessage;
import mrriegel.storagenetwork.network.CableFilterMessage;
import mrriegel.storagenetwork.registry.PacketRegistry;
import mrriegel.storagenetwork.util.UtilTileEntity;
import mrriegel.storagenetwork.util.inventory.FilterItemStackHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiCheckBox;
import net.minecraftforge.items.ItemHandlerHelper;

public abstract class GuiCableBase extends GuiContainer {

  public static final int SLOT_SIZE = 18;
  public static final int TEXTBOX_WIDTH = 26;
  protected List<ItemSlotNetwork> itemSlotsGhost;
  protected ResourceLocation texture = new ResourceLocation(StorageNetwork.MODID, "textures/gui/cable.png");
  protected GuiCableButton btnImport;
  public GuiCheckBox checkNbtBtn;
  public GuiCheckBox checkOreBtn;
  public GuiCheckBox checkMetaBtn;
  ContainerCable containerCable;

  public GuiCableBase(ContainerCable containerCable) {
    super(containerCable);
    this.xSize = 176;
    this.ySize = 171;
    this.itemSlotsGhost = Lists.newArrayList();
    this.containerCable = containerCable;
  }

  public void importSlotsButtonPressed() {}

  @Override
  protected void actionPerformed(GuiButton button) throws IOException {
    super.actionPerformed(button);
    FilterItemStackHandler stackHandler = getFilterHandler();
    if (stackHandler == null) {
      return;
    }
    if (btnImport != null && button.id == btnImport.id) {
      importSlotsButtonPressed();
      PacketRegistry.INSTANCE.sendToServer(new CableDataMessage(button.id));
    }
    if (button.id == checkMetaBtn.id || button.id == checkOreBtn.id || button.id == checkNbtBtn.id) {
      stackHandler.nbt = checkNbtBtn.isChecked();
      stackHandler.ores = checkOreBtn.isChecked();
      stackHandler.meta = checkMetaBtn.isChecked();
      PacketRegistry.INSTANCE.sendToServer(new CableFilterMessage(-1, null, checkOreBtn.isChecked(), checkMetaBtn.isChecked(), this.checkNbtBtn.isChecked()));
    }
  }

  @Override
  public void drawBackground(int tint) {
    super.drawBackground(tint);
  }

  public FontRenderer getFont() {
    return this.fontRenderer;
  }

  public FilterItemStackHandler getFilterHandler() {
    return null;
  }

  @Override
  protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
    super.mouseClicked(mouseX, mouseY, mouseButton);
    ItemStack stackCarriedByMouse = mc.player.inventory.getItemStack().copy();
    FilterItemStackHandler stackHandler = getFilterHandler();
    if (stackHandler == null) {
      return;
    }
    boolean isRightClick = mouseButton == UtilTileEntity.MOUSE_BTN_RIGHT;
    boolean isLeftClick = mouseButton == UtilTileEntity.MOUSE_BTN_LEFT;
    for (int slot = 0; slot < itemSlotsGhost.size(); slot++) {
      ItemSlotNetwork itemSlot = itemSlotsGhost.get(slot);
      if (!itemSlot.isMouseOverSlot(mouseX, mouseY)) {
        continue;
      }
      boolean doesExistAlready = stackHandler.exactStackAlreadyInList(stackCarriedByMouse);
      if (!stackCarriedByMouse.isEmpty() && !doesExistAlready) {
        int quantity = (isRightClick) ? 1 : stackCarriedByMouse.getCount();
        stackHandler.setStackInSlot(slot, ItemHandlerHelper.copyStackWithSize(stackCarriedByMouse, quantity));
      }
      else {
        ItemStack filterStack = stackHandler.getStackInSlot(slot);
        if (filterStack == null || filterStack.isEmpty()) {
          break;
        }
        if (isLeftClick) {
          stackHandler.setStackInSlot(slot, ItemStack.EMPTY);
        }
      }
      PacketRegistry.INSTANCE.sendToServer(new CableFilterMessage(slot, stackHandler.getStackInSlot(slot), stackHandler.ores, stackHandler.meta, stackHandler.nbt));
      break;
    }
  }

  @Override
  public void handleMouseInput() throws IOException {
    super.handleMouseInput();
    int wheel = Mouse.getDWheel();
    if (wheel == 0) {
      return;
    }
    int change = GuiScreen.isShiftKeyDown() ? 8 : 1;
    if (GuiScreen.isAltKeyDown()) {
      change *= 2;
    }
    boolean wheelUp = wheel > 0;
    int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
    int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
    for (int i = 0; i < itemSlotsGhost.size(); i++) {
      ItemSlotNetwork itemSlot = itemSlotsGhost.get(i);
      if (!itemSlot.isMouseOverSlot(mouseX, mouseY) || itemSlot.getStack().isEmpty()) {
        continue;
      }
      if (wheelUp)
        itemSlot.getStack().grow(change);
      else
        itemSlot.getStack().shrink(change);
      if (itemSlot.getStack().getCount() >= 64) {
        itemSlot.getStack().setCount(64);
      }
      //and save changes OFC
      FilterItemStackHandler stackHandler = getFilterHandler();
      PacketRegistry.INSTANCE.sendToServer(new CableFilterMessage(i, itemSlot.getStack(), stackHandler.ores, stackHandler.meta, stackHandler.nbt));
      return;
    }
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    super.drawScreen(mouseX, mouseY, partialTicks);
    super.renderHoveredToolTip(mouseX, mouseY);
    drawTooltips(mouseX, mouseY);
  }

  protected void drawTooltips(int mouseX, int mouseY) {
    for (ItemSlotNetwork s : itemSlotsGhost) {
      if (s != null && s.getStack() != null && !s.getStack().isEmpty() && s.isMouseOverSlot(mouseX, mouseY)) {
        this.renderToolTip(s.getStack(), mouseX, mouseY);
      }
    }
    if (btnImport != null && btnImport.isMouseOver()) {
      drawHoveringText(Lists.newArrayList(I18n.format("gui.storagenetwork.gui.import")), mouseX, mouseY);
    }
  }

  @Override
  public void onGuiClosed() {
    super.onGuiClosed();
  }

  public void setFilterItems(List<ItemStack> stacks) {
    FilterItemStackHandler filter = this.getFilterHandler();
    for (int i = 0; i < stacks.size(); i++) {
      ItemStack s = stacks.get(i);
      filter.setStackInSlot(i, s);
    }
  }
}
