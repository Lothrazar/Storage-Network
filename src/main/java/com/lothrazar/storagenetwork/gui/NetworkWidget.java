package com.lothrazar.storagenetwork.gui;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.lothrazar.storagenetwork.StorageNetwork;
import com.lothrazar.storagenetwork.api.IGuiNetwork;
import com.lothrazar.storagenetwork.block.request.GuiNetworkTable;
import com.lothrazar.storagenetwork.gui.ButtonRequest.TextureEnum;
import com.lothrazar.storagenetwork.jei.JeiHooks;
import com.lothrazar.storagenetwork.network.InsertMessage;
import com.lothrazar.storagenetwork.network.RequestMessage;
import com.lothrazar.storagenetwork.registry.PacketRegistry;
import com.lothrazar.storagenetwork.util.UtilTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.ModList;

public class NetworkWidget {

  private final IGuiNetwork gui;
  public TextFieldWidget searchBar;
  long lastClick;
  int page = 1, maxPage = 1;
  public List<ItemStack> stacks;
  List<ItemSlotNetwork> slots;
  private int lines = 4;
  private int columns = 9;
  public ItemStack stackUnderMouse = ItemStack.EMPTY;
  private int fieldHeight = 90;
  public ButtonRequest directionBtn;
  public ButtonRequest sortBtn;
  public ButtonRequest jeiBtn;

  @Deprecated
  public NetworkWidget(IGuiNetwork gui) {
    this(gui, NetworkGuiSize.NORMAL);
  }

  public NetworkWidget(IGuiNetwork gui, NetworkGuiSize size) {
    this.gui = gui;
    stacks = Lists.newArrayList();
    slots = Lists.newArrayList();
    PacketRegistry.INSTANCE.sendToServer(new RequestMessage());
    lastClick = System.currentTimeMillis();
    switch (size) {
      case LARGE:
        setLines(8);
        setFieldHeight(180 - 8); // offset is important 
      break;
      case NORMAL:
        setLines(4);
        setFieldHeight(90);
      break;
    }
  }

  public void applySearchTextToSlots() {
    String searchText = searchBar.getText();
    List<ItemStack> stacksToDisplay = searchText.equals("") ? Lists.newArrayList(stacks) : Lists.newArrayList();
    if (!searchText.equals("")) {
      for (ItemStack stack : stacks) {
        if (doesStackMatchSearch(stack)) {
          stacksToDisplay.add(stack);
        }
      }
    }
    this.sortStackWrappers(stacksToDisplay);
    this.applyScrollPaging(stacksToDisplay);
    this.rebuildItemSlots(stacksToDisplay);
  }

  public void clearSearch() {
    if (searchBar == null) {
      return;
    }
    searchBar.setText("");
    if (ModList.get().isLoaded("jei") && gui.isJeiSearchSynced()) {
      JeiHooks.setFilterText("");
    }
  }

  private boolean doesStackMatchSearch(ItemStack stack) {
    String searchText = searchBar.getText();
    if (searchText.startsWith("@")) { // TODO: ENUM //search modname 
      String name = UtilTileEntity.getModNameForItem(stack.getItem());
      return name.toLowerCase().contains(searchText.toLowerCase().substring(1));
    }
    else if (searchText.startsWith("#")) { // search tooltips
      String tooltipString;
      Minecraft mc = Minecraft.getInstance();
      List<ITextComponent> tooltip = stack.getTooltip(mc.player, ITooltipFlag.TooltipFlags.NORMAL);
      List<String> unformattedTooltip = tooltip.stream().map(ITextComponent::getString).collect(Collectors.toList());
      tooltipString = Joiner.on(' ').join(unformattedTooltip).toLowerCase().trim();
      return tooltipString.contains(searchText.toLowerCase().substring(1));
    }
    else if (searchText.startsWith("$")) { // search tags
      List<String> joiner = new ArrayList<>();
      for (ResourceLocation oreId : stack.getItem().getTags()) {
        String oreName = oreId.toString();
        joiner.add(oreName);
      }
      String dictFinal = Joiner.on(' ').join(joiner).toLowerCase().trim();
      return dictFinal.contains(searchText.toLowerCase().substring(1));
    }
    else {
      return stack.getDisplayName().getString().toLowerCase().contains(searchText.toLowerCase());
    }
  }

  public boolean canClick() {
    return System.currentTimeMillis() > lastClick + 100L;
  }

  int getLines() {
    return lines;
  }

  int getColumns() {
    return columns;
  }

  public void setLines(int v) {
    lines = v;
  }

  void setColumns(int v) {
    columns = v;
  }

  public void applyScrollPaging(List<ItemStack> stacksToDisplay) {
    maxPage = stacksToDisplay.size() / (getColumns());
    if (stacksToDisplay.size() % (getColumns()) != 0) {
      maxPage++;
    }
    maxPage -= (getLines() - 1);
    if (maxPage < 1) {
      maxPage = 1;
    }
    if (page < 1) {
      page = 1;
    }
    if (page > maxPage) {
      page = maxPage;
    }
  }

  public void mouseScrolled(double mouseButton) {
    // < 0 going down
    // > 0 going up
    if (mouseButton > 0 && page > 1) {
      page--;
    }
    if (mouseButton < 0 && page < maxPage) {
      page++;
    }
  }

  public void rebuildItemSlots(List<ItemStack> stacksToDisplay) {
    slots = Lists.newArrayList();
    int index = (page - 1) * (getColumns());
    for (int row = 0; row < getLines(); row++) {
      for (int col = 0; col < getColumns(); col++) {
        if (index >= stacksToDisplay.size()) {
          break;
        }
        int in = index;
        //        StorageNetwork.LOGGER.info(in + "GUI STORAGE rebuildItemSlots "+stacksToDisplay.get(in));
        slots.add(new ItemSlotNetwork(gui, stacksToDisplay.get(in),
            gui.getGuiLeft() + 8 + col * 18,
            gui.getGuiTop() + 10 + row * 18,
            stacksToDisplay.get(in).getCount(),
            gui.getGuiLeft(), gui.getGuiTop(), true));
        index++;
      }
    }
  }

  public boolean inSearchBar(double mouseX, double mouseY) {
    return gui.isInRegion(
        searchBar.x - gui.getGuiLeft(), searchBar.y - gui.getGuiTop(), // x, y
        searchBar.getWidth(), searchBar.getHeightRealms(), // width, height
        mouseX, mouseY);
  }

  public void initSearchbar() {
    searchBar.setEnableBackgroundDrawing(false);
    searchBar.setVisible(true);
    searchBar.setTextColor(16777215);
    searchBar.setFocused2(StorageNetwork.CONFIG.enableAutoSearchFocus());
    try {
      if (ModList.get().isLoaded("jei") && gui.isJeiSearchSynced()) {
        searchBar.setText(JeiHooks.getFilterText());
      }
    }
    catch (Exception e) {
      StorageNetwork.LOGGER.error("Search bar error ", e);
    }
  }

  public void syncTextToJei() {
    if (ModList.get().isLoaded("jei") && gui.isJeiSearchSynced()) {
      JeiHooks.setFilterText(searchBar.getText());
    }
  }

  public void drawGuiContainerForegroundLayer(MatrixStack ms, int mouseX, int mouseY, FontRenderer font) {
    for (ItemSlotNetwork slot : slots) {
      if (slot != null && slot.isMouseOverSlot(mouseX, mouseY)) {
        slot.drawTooltip(ms, mouseX, mouseY);
        return; // slots and btns do not overlap
      }
    }
    // 
    TranslationTextComponent tooltip = null;
    if (directionBtn != null && directionBtn.isMouseOver(mouseX, mouseY)) {
      tooltip = new TranslationTextComponent("gui.storagenetwork.sort");
    }
    else if (sortBtn != null && sortBtn.isMouseOver(mouseX, mouseY)) {
      tooltip = new TranslationTextComponent("gui.storagenetwork.req.tooltip_" + gui.getSort().name().toLowerCase());
    }
    else if (ModList.get().isLoaded("jei") && jeiBtn != null && jeiBtn.isMouseOver(mouseX, mouseY)) {
      tooltip = new TranslationTextComponent(gui.isJeiSearchSynced() ? "gui.storagenetwork.fil.tooltip_jei_on" : "gui.storagenetwork.fil.tooltip_jei_off");
    }
    else if (this.inSearchBar(mouseX, mouseY)) {
      //tooltip = new TranslationTextComponent("gui.storagenetwork.fil.tooltip_clear");
      if (!Screen.hasShiftDown()) {
        tooltip = new TranslationTextComponent("gui.storagenetwork.shift");
      }
      else {
        List<ITextComponent> lis = Lists.newArrayList();
        lis.add(new TranslationTextComponent("gui.storagenetwork.fil.tooltip_mod")); //@
        lis.add(new TranslationTextComponent("gui.storagenetwork.fil.tooltip_tooltip")); //#
        lis.add(new TranslationTextComponent("gui.storagenetwork.fil.tooltip_tags")); //$
        lis.add(new TranslationTextComponent("gui.storagenetwork.fil.tooltip_clear")); //clear
        Screen screen = ((Screen) gui);
        screen.renderWrappedToolTip(ms, lis, mouseX - gui.getGuiLeft(), mouseY - gui.getGuiTop(), font);
        return; // all done, we have our tts rendered
      }
    }
    //do we have a tooltip
    if (tooltip != null) {
      Screen screen = ((Screen) gui);
      screen.renderWrappedToolTip(ms, Lists.newArrayList(tooltip), mouseX - gui.getGuiLeft(), mouseY - gui.getGuiTop(), font);
    }
  }

  public void renderItemSlots(MatrixStack ms, int mouseX, int mouseY, FontRenderer font) {
    stackUnderMouse = ItemStack.EMPTY;
    for (ItemSlotNetwork slot : slots) {
      slot.drawSlot(ms, font, mouseX, mouseY);
      if (slot.isMouseOverSlot(mouseX, mouseY)) {
        stackUnderMouse = slot.getStack();
      }
    }
    if (slots.isEmpty()) {
      stackUnderMouse = ItemStack.EMPTY;
    }
  }

  public boolean charTyped(char typedChar, int keyCode) {
    if (searchBar.isFocused() && searchBar.charTyped(typedChar, keyCode)) {
      PacketRegistry.INSTANCE.sendToServer(new RequestMessage(0, ItemStack.EMPTY, false, false));
      syncTextToJei();
      return true;
    }
    return false;
  }

  public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
    searchBar.setFocused2(false);
    if (inSearchBar(mouseX, mouseY)) {
      searchBar.setFocused2(true);
      if (mouseButton == UtilTileEntity.MOUSE_BTN_RIGHT) {
        clearSearch();
        return;
      }
    }
    ClientPlayerEntity player = Minecraft.getInstance().player;
    if (player == null) {
      return;
    }
    ItemStack stackCarriedByMouse = player.inventory.getItemStack();
    if (!stackUnderMouse.isEmpty()
        && (mouseButton == UtilTileEntity.MOUSE_BTN_LEFT || mouseButton == UtilTileEntity.MOUSE_BTN_RIGHT)
        && stackCarriedByMouse.isEmpty() &&
        this.canClick()) {
      PacketRegistry.INSTANCE.sendToServer(new RequestMessage(mouseButton, this.stackUnderMouse.copy(), Screen.hasShiftDown(),
          Screen.hasAltDown() || Screen.hasControlDown()));
      this.lastClick = System.currentTimeMillis();
    }
    else if (!stackCarriedByMouse.isEmpty() && inField((int) mouseX, (int) mouseY) &&
        this.canClick()) {
          //0 isd getDim()
          PacketRegistry.INSTANCE.sendToServer(new InsertMessage(0, mouseButton));
          this.lastClick = System.currentTimeMillis();
        }
  }

  private boolean inField(int mouseX, int mouseY) {
    return mouseX > (gui.getGuiLeft() + 7) && mouseX < (gui.getGuiLeft() + GuiNetworkTable.WIDTH - 7)
        && mouseY > (gui.getGuiTop() + 7) && mouseY < (gui.getGuiTop() + getFieldHeight());
  }

  public void initButtons() {
    int y = this.searchBar.y - 4;
    this.directionBtn = new ButtonRequest(
        gui.getGuiLeft() + 6, y, "", (p) -> {
          gui.setDownwards(!gui.getDownwards());
          gui.syncDataToServer();
        });
    directionBtn.setHeight(16);
    this.sortBtn = new ButtonRequest(gui.getGuiLeft() + 22, y, "", (p) -> {
      gui.setSort(gui.getSort().next());
      gui.syncDataToServer();
    });
    sortBtn.setHeight(16);
    if (ModList.get().isLoaded("jei")) {
      jeiBtn = new ButtonRequest(gui.getGuiLeft() + 38, y, "", (p) -> {
        gui.setJeiSearchSynced(!gui.isJeiSearchSynced());
        gui.syncDataToServer();
      });
      jeiBtn.setHeight(16);
    }
  }

  public void sortStackWrappers(List<ItemStack> stacksToDisplay) {
    Collections.sort(stacksToDisplay, new Comparator<ItemStack>() {

      final int mul = gui.getDownwards() ? -1 : 1;

      @Override
      public int compare(ItemStack o2, ItemStack o1) {
        switch (gui.getSort()) {
          case AMOUNT:
            return Integer.compare(o1.getCount(), o2.getCount()) * mul;
          case NAME:
            return o2.getDisplayName().getString().compareToIgnoreCase(o1.getDisplayName().getString()) * mul;
          case MOD:
            return UtilTileEntity.getModNameForItem(o2.getItem()).compareToIgnoreCase(UtilTileEntity.getModNameForItem(o1.getItem())) * mul;
        }
        return 0;
      }
    });
  }

  public void render() {
    switch (gui.getSort()) {
      case AMOUNT:
        sortBtn.setTextureId(TextureEnum.SORT_AMT);
      break;
      case MOD:
        sortBtn.setTextureId(TextureEnum.SORT_MOD);
      break;
      case NAME:
        sortBtn.setTextureId(TextureEnum.SORT_NAME);
      break;
    }
    directionBtn.setTextureId(gui.getDownwards() ? TextureEnum.SORT_DOWN : TextureEnum.SORT_UP);
    if (jeiBtn != null && ModList.get().isLoaded("jei")) {
      jeiBtn.setTextureId(gui.isJeiSearchSynced() ? TextureEnum.JEI_GREEN : TextureEnum.JEI_RED);
    }
  }

  public int getFieldHeight() {
    return fieldHeight;
  }

  public void setFieldHeight(int fieldHeight) {
    this.fieldHeight = fieldHeight;
  }

  public enum NetworkGuiSize {
    NORMAL, LARGE;
  }
}
