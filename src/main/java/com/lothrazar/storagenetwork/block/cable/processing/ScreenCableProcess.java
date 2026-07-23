package com.lothrazar.storagenetwork.block.cable.processing;

import net.minecraft.client.Minecraft;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.google.common.collect.Lists;
import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.api.gui.GuiPrivate;
import com.lothrazar.storagenetwork.api.capabilities.FilterItemStackHandler;
import com.lothrazar.storagenetwork.gui.components.ButtonRequest;
import com.lothrazar.storagenetwork.gui.components.ButtonRequest.TextureEnum;
import com.lothrazar.storagenetwork.gui.slot.ItemSlotNetwork;
import com.lothrazar.storagenetwork.network.CableProcessMessage;
import com.lothrazar.storagenetwork.network.CableProcessMessage.ProcessMessageType;
import com.lothrazar.storagenetwork.registry.ClientEventRegistry;
import com.lothrazar.storagenetwork.util.SsnConsts;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class ScreenCableProcess extends AbstractContainerScreen<ContainerCableProcess> implements GuiPrivate {

  private static final Button.CreateNarration DEFAULT_NARRATION = supplier -> supplier.get();
  private static final int FONT = 0xFFFFFFFF;
  private static final int SLOT_SIZE = 18;
  // Layout: two 3x3 grids side by side inside a 176-wide GUI
  private static final int INPUT_X = 8;
  private static final int OUTPUT_X = 114;
  private static final int GRID_Y = 36;
  private static final int FACE_Y = GRID_Y + 3 * SLOT_SIZE + 4; // 94

  private final Identifier texture = Identifier.fromNamespaceAndPath(StorageNetworkMod.MODID, "textures/gui/cable_process.png");
  private final ContainerCableProcess container;
  private List<ItemSlotNetwork> inputSlots;
  private List<ItemSlotNetwork> outputSlots;
  private ButtonRequest btnRedstone;
  private ButtonRequest btnImport;
  private ButtonRequest btnReset;
  private ButtonRequest btnAlways;
  private ButtonRequest btnInputFace;
  private ButtonRequest btnOutputFace;
  private ButtonRequest btnMinus;
  private ButtonRequest btnPlus;

  public ScreenCableProcess(ContainerCableProcess container, Inventory inv, Component name) {
    super(container, inv, name, 176, 200);
    this.container = container;
  }

  @Override
  public void init() {
    super.init();
    btnRedstone = addRenderableWidget(new ButtonRequest(leftPos + 4, topPos + 4, "", p -> {
      send(ProcessMessageType.REDSTONE, 0, ItemStack.EMPTY);
    }, DEFAULT_NARRATION));
    btnImport = addRenderableWidget(new ButtonRequest(leftPos + 22, topPos + 4, "", p -> {
      send(ProcessMessageType.IMPORT_FILTERS, 0, ItemStack.EMPTY);
    }, DEFAULT_NARRATION));
    btnImport.setTextureId(TextureEnum.IMPORT);
    btnReset = addRenderableWidget(new ButtonRequest(leftPos + 40, topPos + 4, "R", p -> {
      send(ProcessMessageType.RESET_CYCLE, 0, ItemStack.EMPTY);
    }, DEFAULT_NARRATION));
    btnMinus = addRenderableWidget(new ButtonRequest(leftPos + 64, topPos + 4, "", p -> {
      ProcessRequestModel m = container.tile.getProcessModel();
      int next = Math.max(0, m.getCount() - (Minecraft.getInstance().hasShiftDown() ? 10 : 1));
      send(ProcessMessageType.SET_COUNT, next, ItemStack.EMPTY);
    }, DEFAULT_NARRATION));
    btnMinus.setTextureId(TextureEnum.MINUS);
    btnPlus = addRenderableWidget(new ButtonRequest(leftPos + 116, topPos + 4, "", p -> {
      ProcessRequestModel m = container.tile.getProcessModel();
      int next = m.getCount() + (Minecraft.getInstance().hasShiftDown() ? 10 : 1);
      send(ProcessMessageType.SET_COUNT, next, ItemStack.EMPTY);
    }, DEFAULT_NARRATION));
    btnPlus.setTextureId(TextureEnum.PLUS);
    btnAlways = addRenderableWidget(new ButtonRequest(leftPos + 138, topPos + 4, "A", p -> {
      ProcessRequestModel m = container.tile.getProcessModel();
      send(ProcessMessageType.SET_ALWAYS_ACTIVE, m.isAlwaysActive() ? 0 : 1, ItemStack.EMPTY);
    }, DEFAULT_NARRATION));
    btnInputFace = addRenderableWidget(new ButtonRequest(leftPos + INPUT_X + SLOT_SIZE, topPos + FACE_Y, "", p -> {
      ProcessRequestModel m = container.tile.getProcessModel();
      send(ProcessMessageType.SET_INPUT_FACE, cycleFace(m.getInputFace()).ordinal(), ItemStack.EMPTY);
    }, DEFAULT_NARRATION));
    btnOutputFace = addRenderableWidget(new ButtonRequest(leftPos + OUTPUT_X + SLOT_SIZE, topPos + FACE_Y, "", p -> {
      ProcessRequestModel m = container.tile.getProcessModel();
      send(ProcessMessageType.SET_OUTPUT_FACE, cycleFace(m.getOutputFace()).ordinal(), ItemStack.EMPTY);
    }, DEFAULT_NARRATION));
  }

  private static Direction cycleFace(Direction d) {
    Direction[] all = Direction.values();
    return all[(d.ordinal() + 1) % all.length];
  }

  private void send(ProcessMessageType type, int value, ItemStack stack) {
    ClientPacketDistributor.sendToServer(new CableProcessMessage(type.ordinal(), value, stack));
  }

  @Override
  public void extractRenderState(GuiGraphicsExtractor ms, int mouseX, int mouseY, float partialTicks) {
    super.extractRenderState(ms, mouseX, mouseY, partialTicks);
    btnRedstone.setTextureId(container.cap.needsRedstone() ? TextureEnum.REDSTONETRUE : TextureEnum.REDSTONEFALSE);
    ProcessRequestModel m = container.tile.getProcessModel();
    btnAlways.setMessage(Component.literal(m.isAlwaysActive() ? "A" : "N"));
    btnInputFace.setMessage(Component.literal(faceShort(m.getInputFace())));
    btnOutputFace.setMessage(Component.literal(faceShort(m.getOutputFace())));
    drawGhostTooltips(ms, mouseX, mouseY);
    drawButtonTooltips(ms, mouseX, mouseY);
  }

  private static String faceShort(Direction d) {
    return d == null ? "?" : d.getName().substring(0, 1).toUpperCase();
  }

  @Override
  protected void extractLabels(GuiGraphicsExtractor ms, int mouseX, int mouseY) {
    ProcessRequestModel m = container.tile.getProcessModel();
    // count label centered between - and + buttons (between x=80 and x=116)
    String countLabel = m.isAlwaysActive() ? "INF" : String.valueOf(m.getCount());
    ms.text(font, countLabel, 98 - font.width(countLabel) / 2, 8, FONT);
    // status: y=24 sits just above the grid at y=36
    String status = m.getStatus().name();
    ms.text(font, status, 8, 24, FONT);
    // face labels under each grid
    ms.text(font, "In", INPUT_X, FACE_Y + 4, FONT);
    ms.text(font, "Out", OUTPUT_X, FACE_Y + 4, FONT);
  }

  @Override
  public void extractBackground(GuiGraphicsExtractor ms, int mouseX, int mouseY, float partialTicks) {
    super.extractBackground(ms, mouseX, mouseY, partialTicks);
    int xCenter = (width - imageWidth) / 2;
    int yCenter = (height - imageHeight) / 2;
    // background sheet (200x176 - see textures/gui/cable_process.png)
    ms.blit(RenderPipelines.GUI_TEXTURED, texture, xCenter, yCenter, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
    // slot frames behind ghost items
    drawSlotFrames(ms, INPUT_X);
    drawSlotFrames(ms, OUTPUT_X);
    inputSlots = buildGhostGrid(ms, container.cap.getFilters(), INPUT_X, mouseX, mouseY);
    outputSlots = buildGhostGrid(ms, container.cap.getFiltersOut(), OUTPUT_X, mouseX, mouseY);
  }

  private void drawSlotFrames(GuiGraphicsExtractor ms, int x0) {
    for (int row = 0; row < 3; row++) {
      for (int col = 0; col < 3; col++) {
        int x = leftPos + x0 + col * SLOT_SIZE;
        int y = topPos + GRID_Y + row * SLOT_SIZE;
        // slot.png is 18x18; draw at (x-1, y-1) so 16x16 item sits centered inside the frame
        ms.blit(RenderPipelines.GUI_TEXTURED, ClientEventRegistry.SLOT, x - 1, y - 1, 0, 0, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE);
      }
    }
  }

  private List<ItemSlotNetwork> buildGhostGrid(GuiGraphicsExtractor ms, FilterItemStackHandler handler, int x0, int mouseX, int mouseY) {
    List<ItemSlotNetwork> list = new ArrayList<>();
    int index = 0;
    for (int row = 0; row < 3; row++) {
      for (int col = 0; col < 3; col++) {
        if (index >= handler.getSlots()) {
          break;
        }
        ItemStack stack = handler.getStackInSlot(index);
        int x = x0 + col * SLOT_SIZE;
        int y = GRID_Y + row * SLOT_SIZE;
        ItemSlotNetwork slot = new ItemSlotNetwork(this, stack, leftPos + x, topPos + y, stack.getCount(), leftPos, topPos, true);
        list.add(slot);
        slot.drawSlot(ms, font, mouseX, mouseY);
        index++;
      }
    }
    return list;
  }

  private void drawGhostTooltips(GuiGraphicsExtractor ms, int mouseX, int mouseY) {
    drawGhostTooltips(ms, inputSlots, mouseX, mouseY);
    drawGhostTooltips(ms, outputSlots, mouseX, mouseY);
  }

  private void drawGhostTooltips(GuiGraphicsExtractor ms, List<ItemSlotNetwork> slots, int mouseX, int mouseY) {
    if (slots == null) {
      return;
    }
    for (ItemSlotNetwork slot : slots) {
      if (slot.isMouseOverSlot(mouseX, mouseY) && !slot.getStack().isEmpty()) {
        ms.setTooltipForNextFrame(font, slot.getStack(), mouseX, mouseY);
        return;
      }
    }
  }

  private void drawButtonTooltips(GuiGraphicsExtractor ms, int mouseX, int mouseY) {
    tip(ms, btnRedstone, mouseX, mouseY,
        "gui.storagenetwork.redstone." + container.cap.needsRedstone());
    tip(ms, btnImport, mouseX, mouseY, "gui.storagenetwork.processing.import");
    tip(ms, btnReset, mouseX, mouseY, "gui.storagenetwork.refresh");
    tip(ms, btnMinus, mouseX, mouseY, "processing.buttons.minus");
    tip(ms, btnPlus, mouseX, mouseY, "processing.buttons.plus");
    String alwaysKey = container.tile.getProcessModel().isAlwaysActive()
        ? "processing.buttons.toggle.true" : "processing.buttons.toggle.false";
    tip(ms, btnAlways, mouseX, mouseY, alwaysKey);
    tip(ms, btnInputFace, mouseX, mouseY, "gui.storagenetwork.processing.recipe");
    tip(ms, btnOutputFace, mouseX, mouseY, "gui.storagenetwork.processing.extract");
  }

  private void tip(GuiGraphicsExtractor ms, AbstractWidget btn, int mouseX, int mouseY, String key) {
    if (btn != null && btn.isMouseOver(mouseX, mouseY)) {
      // render() is in raw screen coords (no leftPos/topPos translation), so pass the mouse position as-is.
      ms.setTooltipForNextFrame(font,
          Lists.newArrayList(Component.translatable(key)),
          Optional.empty(),
          mouseX, mouseY);
    }
  }

  @Override
  public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
    double mouseX = event.x();
    double mouseY = event.y();
    int mouseButton = event.button();
    ItemStack carried = minecraft.player.containerMenu.getCarried();
    if (handleGridClick(inputSlots, container.cap.getFilters(), ProcessMessageType.SAVE_FILTER_IN, mouseX, mouseY, mouseButton, carried)) {
      return true;
    }
    if (handleGridClick(outputSlots, container.cap.getFiltersOut(), ProcessMessageType.SAVE_FILTER_OUT, mouseX, mouseY, mouseButton, carried)) {
      return true;
    }
    return super.mouseClicked(event, doubleClick);
  }

  private boolean handleGridClick(List<ItemSlotNetwork> slots,
      FilterItemStackHandler handler,
      ProcessMessageType saveType, double mouseX, double mouseY, int mouseButton, ItemStack carried) {
    if (slots == null) {
      return false;
    }
    for (int i = 0; i < slots.size(); i++) {
      ItemSlotNetwork slot = slots.get(i);
      if (!slot.isMouseOverSlot((int) mouseX, (int) mouseY)) {
        continue;
      }
      if (!slot.getStack().isEmpty()) {
        if (mouseButton == SsnConsts.MOUSE_BTN_RIGHT) {
          int direction = Minecraft.getInstance().hasShiftDown() ? -1 : 1;
          int newCount = Math.min(64, slot.getStack().getCount() + direction);
          if (newCount < 1) {
            newCount = 1;
          }
          ItemStack updated = slot.getStack().copy();
          updated.setCount(newCount);
          slot.setStack(updated);
          handler.setStackInSlot(i, updated);
          send(saveType, i, updated);
        }
        else {
          slot.setStack(ItemStack.EMPTY);
          handler.setStackInSlot(i, ItemStack.EMPTY);
          send(saveType, i, ItemStack.EMPTY);
        }
      }
      else {
        ItemStack ghost = carried.copy();
        if (!ghost.isEmpty()) {
          ghost.setCount(1);
        }
        slot.setStack(ghost);
        handler.setStackInSlot(i, ghost);
        send(saveType, i, ghost);
      }
      return true;
    }
    return false;
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
    if (scrollY != 0) {
      if (scrollGrid(inputSlots, container.cap.getFilters(), ProcessMessageType.SAVE_FILTER_IN, mouseX, mouseY, scrollY)) {
        return true;
      }
      if (scrollGrid(outputSlots, container.cap.getFiltersOut(), ProcessMessageType.SAVE_FILTER_OUT, mouseX, mouseY, scrollY)) {
        return true;
      }
    }
    return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
  }

  private boolean scrollGrid(List<ItemSlotNetwork> slots,
      FilterItemStackHandler handler,
      ProcessMessageType saveType, double mouseX, double mouseY, double scrollY) {
    if (slots == null) {
      return false;
    }
    for (int i = 0; i < slots.size(); i++) {
      ItemSlotNetwork slot = slots.get(i);
      if (!slot.isMouseOverSlot((int) mouseX, (int) mouseY)) {
        continue;
      }
      if (slot.getStack().isEmpty()) {
        return false;
      }
      ItemStack updated = slot.getStack().copy();
      int dir = scrollY > 0 ? 1 : -1;
      int newCount = Math.max(1, Math.min(64, updated.getCount() + dir));
      if (newCount == updated.getCount()) {
        return false;
      }
      updated.setCount(newCount);
      slot.setStack(updated);
      handler.setStackInSlot(i, updated);
      send(saveType, i, updated);
      return true;
    }
    return false;
  }

  @Override
  public void renderStackTooltip(GuiGraphicsExtractor ms, ItemStack stack, int mouseX, int mouseY) {
    ms.setTooltipForNextFrame(font, stack, mouseX, mouseY);
  }


  @Override
  public boolean isInRegion(int x, int y, int width, int height, double mouseX, double mouseY) {
    return super.isHovering(x, y, width, height, mouseX, mouseY);
  }
}
