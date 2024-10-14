package com.lothrazar.storagenetwork.block.cable.linkfilter;

import java.util.List;
import java.util.Optional;
import com.google.common.collect.Lists;
import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.api.IGuiPrivate;
import com.lothrazar.storagenetwork.block.cable.inputfilter.ScreenCableImportFilter;
import com.lothrazar.storagenetwork.capability.handler.FilterItemStackHandler;
import com.lothrazar.storagenetwork.gui.ButtonRequest;
import com.lothrazar.storagenetwork.gui.ButtonRequest.TextureEnum;
import com.lothrazar.storagenetwork.gui.ItemSlotNetwork;
import com.lothrazar.storagenetwork.network.CableDataMessage;
import com.lothrazar.storagenetwork.registry.PacketRegistry;
import com.lothrazar.storagenetwork.util.SsnConsts;
import com.lothrazar.storagenetwork.util.UtilTileEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class ScreenCableFilter extends AbstractContainerScreen<ContainerCableFilter> implements IGuiPrivate {

  protected static final Button.CreateNarration DEFAULT_NARRATION = (supplier) -> {
    return supplier.get();
  };
  private final ResourceLocation texture = new ResourceLocation(StorageNetworkMod.MODID, "textures/gui/cable.png");
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
    PacketRegistry.INSTANCE.sendToServer(new CableDataMessage(CableDataMessage.CableMessageType.IMPORT_FILTER.ordinal()));
  }

  private void sendStackSlot(int value, ItemStack stack) {
    PacketRegistry.INSTANCE.sendToServer(new CableDataMessage(CableDataMessage.CableMessageType.SAVE_FITLER.ordinal(), value, stack));
  }

  private void syncData(int priority) {
    PacketRegistry.INSTANCE.sendToServer(new CableDataMessage(CableDataMessage.CableMessageType.SYNC_DATA.ordinal(), priority, isAllowlist));
  }

  @Override
  public void render(GuiGraphics ms, int mouseX, int mouseY, float partialTicks) {
    renderBackground(ms);
    super.render(ms, mouseX, mouseY, partialTicks);
    btnAllowIgn.setTextureId(this.isAllowlist ? TextureEnum.ALLOWLIST : TextureEnum.IGNORELIST);
    if (containerCableLink == null || containerCableLink.cap == null || containerCableLink.cap.connectable == null) {
      return;
    }
    //   btnRedstone.setTextureId(containerCableLink.cap.connectable.needsRedstone() ? TextureEnum.ALLOWLIST : TextureEnum.IGNORELIST);
  }

  @Override
  public void renderLabels(GuiGraphics ms, int mouseX, int mouseY) {
    //    this.font.draw(ms, this.title, this.titleLabelX, this.titleLabelY, 4210752); // TODO: gui titles
    int priority = containerCableLink.cap.getPriority();
    ms.drawString(font, String.valueOf(priority),
        50 - font.width(String.valueOf(priority)) / 2,
        12,
        4210752);
    this.drawTooltips(ms, mouseX, mouseY);
  }

  private void drawTooltips(GuiGraphics ms, final int mouseX, final int mouseY) {
    if (btnImport != null && btnImport.isMouseOver(mouseX, mouseY)) {
      //NOT StringTextComponent
      ms.renderTooltip(font, Lists.newArrayList(Component.translatable("gui.storagenetwork.import")),
          Optional.empty(), mouseX - leftPos, mouseY - topPos);
    }
    if (btnAllowIgn != null && btnAllowIgn.isMouseOver(mouseX, mouseY)) {
      ms.renderTooltip(font, Lists.newArrayList(Component.translatable(this.isAllowlist
          ? "gui.storagenetwork.allowlist"
          : "gui.storagenetwork.ignorelist")), Optional.empty(),
          mouseX - leftPos, mouseY - topPos);
    }
    if (btnMinus != null && btnMinus.isMouseOver(mouseX, mouseY)) {
      ms.renderTooltip(font, Lists.newArrayList(Component.translatable("gui.storagenetwork.priority.down")), Optional.empty(),
          mouseX - leftPos, mouseY - topPos);
    }
    if (btnPlus != null && btnPlus.isMouseOver(mouseX, mouseY)) {
      ms.renderTooltip(font, Lists.newArrayList(Component.translatable("gui.storagenetwork.priority.up")), Optional.empty(),
          mouseX - leftPos, mouseY - topPos);
    }
    if (btnRedstone != null && btnRedstone.isMouseOver(mouseX, mouseY)) {
      ms.renderTooltip(font, Lists.newArrayList(Component.translatable("gui.storagenetwork.redstone")), Optional.empty(),
          mouseX - leftPos, mouseY - topPos);
    }
  }

  public static final int SLOT_SIZE = SsnConsts.SLOT_SIZE;

  @Override
  public void renderStackTooltip(GuiGraphics ms, ItemStack stack, int mousex, int mousey) {
    ms.renderTooltip(font, stack, mousex, mousey);
  }

  @Override
  public void drawGradient(GuiGraphics ms, int x, int y, int x2, int y2, int u, int v) {
    ms.fillGradient(x, y, x2, y2, u, v);
  }

  @Override
  protected void renderBg(GuiGraphics ms, float partialTicks, int mouseX, int mouseY) {
    int xCenter = (width - imageWidth) / 2;
    int yCenter = (height - imageHeight) / 2;
    ms.blit(texture, xCenter, yCenter, 0, 0, imageWidth, imageHeight);
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
  public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
    ItemStack mouse = minecraft.player.containerMenu.getCarried();
    for (int i = 0; i < this.itemSlotsGhost.size(); i++) {
      ItemSlotNetwork slot = itemSlotsGhost.get(i);
      if (slot.isMouseOverSlot((int) mouseX, (int) mouseY)) {
        if (slot.getStack().isEmpty() == false) {
          //i hit non-empty slot, clear it no matter what
          if (mouseButton == UtilTileEntity.MOUSE_BTN_RIGHT) {
            int direction = hasShiftDown() ? -1 : 1;
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
    return super.mouseClicked(mouseX, mouseY, mouseButton);
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
    if (delta != 0) {
      for (int i = 0; i < this.itemSlotsGhost.size(); i++) {
        ItemSlotNetwork slot = itemSlotsGhost.get(i);
        if (slot.isMouseOverSlot((int) mouseX, (int) mouseY)) {
          ItemStack changeme = ScreenCableImportFilter.scrollStack(delta, slot);
          if (changeme != null) {
            this.sendStackSlot(i, changeme);
            return true;
          }
        }
      }
    }
    return super.mouseScrolled(mouseX, mouseY, delta);
  }

  @Override
  public boolean isInRegion(int x, int y, int width, int height, double mouseX, double mouseY) {
    return super.isHovering(x, y, width, height, mouseX, mouseY);
  }
}
