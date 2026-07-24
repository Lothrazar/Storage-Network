package com.lothrazar.storagenetwork.block.cable.linkfilter;

import net.minecraft.client.Minecraft;
import java.util.List;
import java.util.Optional;
import com.google.common.collect.Lists;
import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.api.gui.GuiPrivate;
import com.lothrazar.storagenetwork.block.cable.inputfilter.ScreenCableImportFilter;
import com.lothrazar.storagenetwork.api.capabilities.FilterItemStackHandler;
import com.lothrazar.storagenetwork.gui.components.ButtonRequest;
import com.lothrazar.storagenetwork.gui.components.ButtonRequest.TextureEnum;
import com.lothrazar.storagenetwork.gui.slot.ItemSlotNetwork;
import com.lothrazar.storagenetwork.network.CableDataMessage;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import com.lothrazar.storagenetwork.util.SsnConsts;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class ScreenCableFilter extends AbstractContainerScreen<ContainerCableFilter> implements GuiPrivate {

  protected static final Button.CreateNarration DEFAULT_NARRATION = (supplier) -> {
    return supplier.get();
  };
  private final Identifier texture = Identifier.fromNamespaceAndPath(StorageNetworkMod.MODID, "textures/gui/cable.png");
  ContainerCableFilter containerCableLink;
  private ButtonRequest btnRedstone;
  private ButtonRequest btnMinus;
  private ButtonRequest btnPlus;
  private ButtonRequest btnAllowIgn;
  private ButtonRequest btnImport;
  private boolean isAllowlist;
  private List<ItemSlotNetwork> itemSlotsGhost;

  public ScreenCableFilter(ContainerCableFilter containerCableFilter, Inventory inv, Component name) {
    super(containerCableFilter, inv, name);
    this.containerCableLink = containerCableFilter;
  }

  @Override
  public void init() {
    super.init();
    this.isAllowlist = containerCableLink.cap.getFilter().isAllowList;
    btnRedstone = addRenderableWidget(new ButtonRequest(leftPos + 4, topPos + 4, "", (p) -> {
      ClientPacketDistributor.sendToServer(new CableDataMessage(CableDataMessage.CableMessageType.REDSTONE.ordinal()));
    }, DEFAULT_NARRATION));
    btnMinus = addRenderableWidget(new ButtonRequest(leftPos + 28, topPos + 4, "", (p) -> {
      this.syncData(-1);
    }, DEFAULT_NARRATION));
    btnMinus.setTextureId(TextureEnum.MINUS);
    btnPlus = addRenderableWidget(new ButtonRequest(leftPos + 60, topPos + 4, "", (p) -> {
      this.syncData(+1);
    }, DEFAULT_NARRATION));
    btnPlus.setTextureId(TextureEnum.PLUS);
    btnAllowIgn = addRenderableWidget(new ButtonRequest(leftPos + 152, topPos + 4, "", (p) -> {
      this.isAllowlist = !this.isAllowlist;
      this.syncData(0);
    }, DEFAULT_NARRATION));
    btnImport = addRenderableWidget(new ButtonRequest(leftPos + 120, topPos + 4, "", (p) -> {
      importFilterSlots();
    }, DEFAULT_NARRATION));
    btnImport.setTextureId(TextureEnum.IMPORT);
  }

  private void importFilterSlots() {
    ClientPacketDistributor.sendToServer(new CableDataMessage(CableDataMessage.CableMessageType.IMPORT_FILTER.ordinal()));
  }

  private void sendStackSlot(int value, ItemStack stack) {
    ClientPacketDistributor.sendToServer(new CableDataMessage(CableDataMessage.CableMessageType.SAVE_FITLER.ordinal(), value, stack));
  }

  private void syncData(int priority) {
    ClientPacketDistributor.sendToServer(new CableDataMessage(CableDataMessage.CableMessageType.SYNC_DATA.ordinal(), priority, isAllowlist));
  }

  @Override
  public void extractRenderState(GuiGraphicsExtractor ms, int mouseX, int mouseY, float partialTicks) {
    super.extractRenderState(ms, mouseX, mouseY, partialTicks);
    btnAllowIgn.setTextureId(this.isAllowlist ? TextureEnum.ALLOWLIST : TextureEnum.IGNORELIST);
    if (containerCableLink == null || containerCableLink.cap == null || containerCableLink.cap.connectable == null) {
      return;
    }
    btnRedstone.setTextureId(containerCableLink.cap.connectable.needsRedstone() ? TextureEnum.REDSTONETRUE : TextureEnum.REDSTONEFALSE);
  }

  @Override
  protected void extractLabels(GuiGraphicsExtractor ms, int mouseX, int mouseY) {
    //    this.font.draw(ms, this.title, this.titleLabelX, this.titleLabelY, 4210752); // TODO: gui titles
    int priority = containerCableLink.cap.getPriority();
    ms.text(font, String.valueOf(priority),
        50 - font.width(String.valueOf(priority)) / 2,
        12,
        4210752);
    this.drawTooltips(ms, mouseX, mouseY);
  }

  private void drawTooltips(GuiGraphicsExtractor ms, final int mouseX, final int mouseY) {
    if (btnImport != null && btnImport.isMouseOver(mouseX, mouseY)) {
      //NOT StringTextComponent
      ms.setTooltipForNextFrame(font, Lists.newArrayList(Component.translatable("gui.storagenetwork.import")),
          Optional.empty(), mouseX, mouseY);
    }
    if (btnAllowIgn != null && btnAllowIgn.isMouseOver(mouseX, mouseY)) {
      ms.setTooltipForNextFrame(font, Lists.newArrayList(Component.translatable(this.isAllowlist
          ? "gui.storagenetwork.allowlist"
          : "gui.storagenetwork.ignorelist")), Optional.empty(),
          mouseX, mouseY);
    }
    if (btnMinus != null && btnMinus.isMouseOver(mouseX, mouseY)) {
      ms.setTooltipForNextFrame(font, Lists.newArrayList(Component.translatable("gui.storagenetwork.priority.down")), Optional.empty(),
          mouseX, mouseY);
    }
    if (btnPlus != null && btnPlus.isMouseOver(mouseX, mouseY)) {
      ms.setTooltipForNextFrame(font, Lists.newArrayList(Component.translatable("gui.storagenetwork.priority.up")), Optional.empty(),
          mouseX, mouseY);
    }
    if (btnRedstone != null && btnRedstone.isMouseOver(mouseX, mouseY)) {
      ms.setTooltipForNextFrame(font, Lists.newArrayList(Component.translatable("gui.storagenetwork.redstone."
          + containerCableLink.cap.connectable.needsRedstone())), Optional.empty(),
          mouseX, mouseY);
    }
  }

  public static final int SLOT_SIZE = SsnConsts.SQ;

  @Override
  public void renderStackTooltip(GuiGraphicsExtractor ms, ItemStack stack, int mousex, int mousey) {
    ms.setTooltipForNextFrame(font, stack, mousex, mousey);
  }


  @Override
  public void extractBackground(GuiGraphicsExtractor ms, int mouseX, int mouseY, float partialTicks) {
    super.extractBackground(ms, mouseX, mouseY, partialTicks);
    int xCenter = (width - imageWidth) / 2;
    int yCenter = (height - imageHeight) / 2;
    ms.blit(RenderPipelines.GUI_TEXTURED, texture, xCenter, yCenter, 0, 0, imageWidth, imageHeight, 256, 256);
    itemSlotsGhost = Lists.newArrayList();
    //TODO: shared with GuiCableIO
    int rows = 2;
    int cols = 9;
    int index = 0;
    int y = 35;
    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < cols; col++) {
        //
        ItemStack stack = containerCableLink.cap.getFilter().getStackInSlot(index);
        int x = 8 + col * SLOT_SIZE;
        itemSlotsGhost.add(new ItemSlotNetwork(this, stack, leftPos + x, topPos + y, stack.getCount(), leftPos, topPos, true));
        index++;
      }
      //move down to second row
      y += SLOT_SIZE;
    }
    for (ItemSlotNetwork s : itemSlotsGhost) {
      s.drawSlot(ms, font, mouseX, mouseY);
    }
  }

  public void setFilterItems(List<ItemStack> stacks) {
    FilterItemStackHandler filter = this.containerCableLink.cap.getFilter();
    for (int i = 0; i < stacks.size(); i++) {
      ItemStack s = stacks.get(i);
      filter.setStackInSlot(i, s);
    }
  }

  @Override
  public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
    double mouseX = event.x();
    double mouseY = event.y();
    int mouseButton = event.button();
    ItemStack mouse = minecraft.player.containerMenu.getCarried();
    for (int i = 0; i < this.itemSlotsGhost.size(); i++) {
      ItemSlotNetwork slot = itemSlotsGhost.get(i);
      if (slot.isMouseOverSlot((int) mouseX, (int) mouseY)) {
        if (slot.getStack().isEmpty() == false) {
          //i hit non-empty slot, clear it no matter what
          if (mouseButton == SsnConsts.MOUSE_BTN_RIGHT) {
            int direction = Minecraft.getInstance().hasShiftDown() ? -1 : 1;
            int newCount = Math.min(64, slot.getStack().getCount() + direction);
            if (newCount < 1) {
              newCount = 1;
            }
            slot.getStack().setCount(newCount);
          }
          else {
            slot.setStack(ItemStack.EMPTY);
          }
          this.sendStackSlot(i, slot.getStack());
          return true;
        }
        else {
          //i hit an empty slot, save what im holding
          slot.setStack(mouse.copy());
          this.sendStackSlot(i, mouse.copy());
          return true;
        }
      }
    }
    return super.mouseClicked(event, doubleClick);
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
    if (scrollY != 0) {
      for (int i = 0; i < this.itemSlotsGhost.size(); i++) {
        ItemSlotNetwork slot = itemSlotsGhost.get(i);
        if (slot.isMouseOverSlot((int) mouseX, (int) mouseY)) {
          ItemStack changeme = ScreenCableImportFilter.scrollStack(scrollY, slot);
          if (changeme != null) {
            this.sendStackSlot(i, changeme);
            return true;
          }
        }
      }
    }
    return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
  }

  @Override
  public boolean isInRegion(int x, int y, int width, int height, double mouseX, double mouseY) {
    return super.isHovering(x, y, width, height, mouseX, mouseY);
  }
}
