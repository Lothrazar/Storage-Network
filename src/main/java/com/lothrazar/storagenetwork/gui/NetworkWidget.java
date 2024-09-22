package com.lothrazar.storagenetwork.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.api.EnumSearchPrefix;
import com.lothrazar.storagenetwork.api.IGuiNetwork;
import com.lothrazar.storagenetwork.block.request.ScreenNetworkTable;
import com.lothrazar.storagenetwork.gui.ButtonRequest.TextureEnum;
import com.lothrazar.storagenetwork.jei.JeiHooks;
import com.lothrazar.storagenetwork.network.InsertMessage;
import com.lothrazar.storagenetwork.network.RequestMessage;
import com.lothrazar.storagenetwork.registry.PacketRegistry;
import com.lothrazar.storagenetwork.util.UtilTileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.fml.ModList;

public class NetworkWidget {

  public ItemStack stackUnderMouse = ItemStack.EMPTY;
  public List<ItemStack> stacks;
  public EditBox searchBar;
  public ButtonRequest directionBtn;
  public ButtonRequest sortBtn;
  public ButtonRequest jeiBtn;
  public ButtonRequest focusBtn;
  private int fieldHeight = 90;
  private List<ItemSlotNetwork> slots;
  private final IGuiNetwork gui;
  private long lastClick;
  private int page = 1;
  private int maxPage = 1;
  private int lines = 4;
  private final int columns = 9;

  @Deprecated
  public NetworkWidget(IGuiNetwork gui) {
    this(gui, NetworkScreenSize.NORMAL);
  }

  public NetworkWidget(IGuiNetwork gui, NetworkScreenSize size) {
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
    String searchText = searchBar.getValue();
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
    searchBar.setValue("");
    if (ModList.get().isLoaded("jei") && gui.isJeiSearchSynced()) {
      JeiHooks.setFilterText("");
    }
  }

  private boolean doesStackMatchSearch(ItemStack stack) {
    String searchText = searchBar.getValue();
    if (searchText.startsWith(EnumSearchPrefix.MOD.getPrefix())) { //  search modname 
      String name = UtilTileEntity.getModNameForItem(stack.getItem());
      return name.toLowerCase().contains(searchText.toLowerCase().substring(1));
    }
    else if (searchText.startsWith(EnumSearchPrefix.TOOLTIP.getPrefix())) { // search tooltips
      String tooltipString;
      Minecraft mc = Minecraft.getInstance();
      List<Component> tooltip = stack.getTooltipLines(mc.player, TooltipFlag.Default.NORMAL);
      List<String> unformattedTooltip = tooltip.stream().map(Component::getString).collect(Collectors.toList());
      tooltipString = Joiner.on(' ').join(unformattedTooltip).toLowerCase().trim();
      return tooltipString.contains(searchText.toLowerCase().substring(1));
    }
    else if (searchText.startsWith(EnumSearchPrefix.TAG.getPrefix())) { // search tags
      List<String> joiner = new ArrayList<>();
      for (ResourceLocation oreId : stack.getTags().map((tagKey) -> tagKey.location()).collect(Collectors.toList())) {
        String oreName = oreId.toString();
        joiner.add(oreName);
      }
      String dictFinal = Joiner.on(' ').join(joiner).toLowerCase().trim();
      return dictFinal.contains(searchText.toLowerCase().substring(1));
    }
    else {
      return stack.getHoverName().getString().toLowerCase().contains(searchText.toLowerCase());
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
            gui.getGuiTopFixJei() + 10 + row * 18,
            stacksToDisplay.get(in).getCount(),
            gui.getGuiLeft(), gui.getGuiTopFixJei(), true));
        index++;
      }
    }
  }

  public boolean inSearchBar(double mouseX, double mouseY) {
    return gui.isInRegion(
        searchBar.x - gui.getGuiLeft(), searchBar.y - gui.getGuiTopFixJei(), // x, y
        searchBar.getWidth(), searchBar.getHeight(), // width, height
        mouseX, mouseY);
  }

  public void initSearchbar() {
    searchBar.setBordered(false);
    searchBar.setVisible(true);
    searchBar.setTextColor(16777215);
    //    searchBar.setFocus(StorageNetwork.CONFIG.enableAutoSearchFocus());
    if (ModList.get().isLoaded("jei")) {
      initJei();
    }
  }

  private void initJei() {
    try {
      if (gui != null && searchBar != null && gui.isJeiSearchSynced()) {
        searchBar.setValue(JeiHooks.getFilterText());
      }
    }
    catch (Exception e) {
      StorageNetworkMod.LOGGER.error("Search bar error ", e);
    }
  }

  public void syncTextToJei() {
    if (ModList.get().isLoaded("jei") && gui.isJeiSearchSynced()) {
      JeiHooks.setFilterText(searchBar.getValue());
    }
  }

  public void drawGuiContainerForegroundLayer(PoseStack ms, int mouseX, int mouseY, Font font) {
    for (ItemSlotNetwork slot : slots) {
      if (slot != null && slot.isMouseOverSlot(mouseX, mouseY)) {
        slot.drawTooltip(ms, mouseX, mouseY);
        return; // slots and btns do not overlap
      }
    }
    // 
    TranslatableComponent tooltip = null;
    if (directionBtn != null && directionBtn.isMouseOver(mouseX, mouseY)) {
      tooltip = new TranslatableComponent("gui.storagenetwork.sort");
    }
    else if (sortBtn != null && sortBtn.isMouseOver(mouseX, mouseY)) {
      tooltip = new TranslatableComponent("gui.storagenetwork.req.tooltip_" + gui.getSort().name().toLowerCase());
    }
    else if (focusBtn != null && focusBtn.isMouseOver(mouseX, mouseY)) {
      tooltip = new TranslatableComponent("gui.storagenetwork.autofocus.tooltip." + gui.getAutoFocus());
    }
    else if (ModList.get().isLoaded("jei") && jeiBtn != null && jeiBtn.isMouseOver(mouseX, mouseY)) {
      tooltip = new TranslatableComponent(gui.isJeiSearchSynced() ? "gui.storagenetwork.fil.tooltip_jei_on" : "gui.storagenetwork.fil.tooltip_jei_off");
    }
    else if (this.inSearchBar(mouseX, mouseY)) {
      //tooltip = new TranslationTextComponent("gui.storagenetwork.fil.tooltip_clear");
      if (!Screen.hasShiftDown()) {
        tooltip = new TranslatableComponent("gui.storagenetwork.shift");
      }
      else {
        List<Component> lis = Lists.newArrayList();
        lis.add(new TranslatableComponent("gui.storagenetwork.fil.tooltip_mod")); //@
        lis.add(new TranslatableComponent("gui.storagenetwork.fil.tooltip_tooltip")); //#
        lis.add(new TranslatableComponent("gui.storagenetwork.fil.tooltip_tags")); //$
        lis.add(new TranslatableComponent("gui.storagenetwork.fil.tooltip_clear")); //clear
        Screen screen = ((Screen) gui);
        screen.renderTooltip(ms, lis, Optional.empty(), mouseX - gui.getGuiLeft(), mouseY - gui.getGuiTopFixJei());
        return; // all done, we have our tts rendered
      }
    }
    //do we have a tooltip
    if (tooltip != null) {
      Screen screen = ((Screen) gui);
      screen.renderTooltip(ms, Lists.newArrayList(tooltip), Optional.empty(), mouseX - gui.getGuiLeft(), mouseY - gui.getGuiTopFixJei());
    }
  }

  public void renderItemSlots(PoseStack ms, int mouseX, int mouseY, Font font) {
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
    searchBar.setFocus(false);
    if (inSearchBar(mouseX, mouseY)) {
      searchBar.setFocus(true);
      if (mouseButton == UtilTileEntity.MOUSE_BTN_RIGHT) {
        clearSearch();
        return;
      }
    }
    LocalPlayer player = Minecraft.getInstance().player;
    if (player == null) {
      return;
    }
    ItemStack stackCarriedByMouse = player.containerMenu.getCarried();
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
    return mouseX > (gui.getGuiLeft() + 7) && mouseX < (gui.getGuiLeft() + ScreenNetworkTable.WIDTH - 7)
        && mouseY > (gui.getGuiTopFixJei() + 7) && mouseY < (gui.getGuiTopFixJei() + getFieldHeight());
  }

  public void initButtons() {
    int y = this.searchBar.y - 4;
    directionBtn = new ButtonRequest(
        gui.getGuiLeft() + 6, y, "", (p) -> {
          gui.setDownwards(!gui.getDownwards());
          gui.syncDataToServer();
        });
    directionBtn.setHeight(16);
    sortBtn = new ButtonRequest(gui.getGuiLeft() + 22, y, "", (p) -> {
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
    focusBtn = new ButtonRequest(
        gui.getGuiLeft() + 166, y + 2, "", (p) -> {
          gui.setAutoFocus(!gui.getAutoFocus());
          gui.syncDataToServer();
        });
    focusBtn.setHeight(11);
    focusBtn.setWidth(6);
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
            return o2.getHoverName().getString().compareToIgnoreCase(o1.getHoverName().getString()) * mul;
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
    focusBtn.setTextureId(gui.getAutoFocus() ? TextureEnum.RED : TextureEnum.GREY);
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

  public enum NetworkScreenSize {
    NORMAL, LARGE;
  }
}
