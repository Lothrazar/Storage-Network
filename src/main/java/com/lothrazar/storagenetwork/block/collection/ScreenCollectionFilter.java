package com.lothrazar.storagenetwork.block.collection;

import net.minecraft.client.Minecraft;
import java.util.List;
import com.google.common.collect.Lists;
import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.api.gui.GuiPrivate;
import com.lothrazar.storagenetwork.api.capabilities.FilterItemStackHandler;
import com.lothrazar.storagenetwork.gui.slot.ItemSlotNetwork;
import com.lothrazar.storagenetwork.network.CableIOMessage;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import com.lothrazar.storagenetwork.util.SsnConsts;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class ScreenCollectionFilter extends AbstractContainerScreen<ContainerCollectionFilter> implements GuiPrivate {

  private final Identifier texture = Identifier.fromNamespaceAndPath(StorageNetworkMod.MODID, "textures/gui/plain_filter.png");
  ContainerCollectionFilter containerCableLink;
  private List<ItemSlotNetwork> itemSlotsGhost;

  public ScreenCollectionFilter(ContainerCollectionFilter containerCableFilter, Inventory inv, Component name) {
    super(containerCableFilter, inv, name);
    this.containerCableLink = containerCableFilter;
  }

  @Override
  public void renderStackTooltip(GuiGraphicsExtractor ms, ItemStack stack, int mousex, int mousey) {
    ms.setTooltipForNextFrame(font, stack, mousex, mousey);
  }


  @Override
  public void init() {
    super.init();
  }

  private void sendStackSlot(int value, ItemStack stack) {
    ClientPacketDistributor.sendToServer(new CableIOMessage(CableIOMessage.CableMessageType.SAVE_FITLER.ordinal(), value, stack));
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
        ItemStack stack = containerCableLink.cap.getFilter().getStackInSlot(index);
        int x = 8 + col * SsnConsts.SQ;
        itemSlotsGhost.add(new ItemSlotNetwork(this, stack, leftPos + x, topPos + y, stack.getCount(), leftPos, topPos, true));
        index++;
      }
      //move down to second row
      y += SsnConsts.SQ;
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
          ItemStack cpy = mouse.copy();
          cpy.setCount(1);
          slot.setStack(cpy);
          this.sendStackSlot(i, cpy);
          return true;
        }
      }
    }
    return super.mouseClicked(event, doubleClick);
  }

  @Override
  public boolean isInRegion(int x, int y, int width, int height, double mouseX, double mouseY) {
    return super.isHovering(x, y, width, height, mouseX, mouseY);
  }
}
