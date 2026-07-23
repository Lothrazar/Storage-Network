package com.lothrazar.storagenetwork.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.lothrazar.library.gui.TileableTexture;
import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.api.EnumSearchPrefix;
import com.lothrazar.storagenetwork.api.gui.*;
import com.lothrazar.storagenetwork.gui.components.ButtonRequest;
import com.lothrazar.storagenetwork.gui.components.ButtonRequest.TextureEnum;
import com.lothrazar.storagenetwork.gui.slot.ItemSlotNetwork;
import com.lothrazar.storagenetwork.network.ClearRecipeMessage;
import com.lothrazar.storagenetwork.network.InsertMessage;
import com.lothrazar.storagenetwork.network.RequestMessage;
import net.minecraft.client.gui.components.AbstractWidget;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import com.lothrazar.storagenetwork.util.SsnConsts;
import com.lothrazar.storagenetwork.api.util.CacheModName;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.fml.ModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DefaultNetworkWidget implements NetworkWidget {
  public static final Logger LOGGER = LogManager.getLogger();

  private static final int W = 256;
  //i know they could all be in the same png file and i pull out sprites from it, but split images is easier to work with
  public static final TileableTexture head = new TileableTexture(Identifier.fromNamespaceAndPath(StorageNetworkMod.MODID, "textures/gui/expandable_head.png"), W, 10);
  public static final TileableTexture head_right = new TileableTexture(Identifier.fromNamespaceAndPath(StorageNetworkMod.MODID, "textures/gui/expandable_head_right.png"), W, 10);
  public static final TileableTexture row = new TileableTexture(Identifier.fromNamespaceAndPath(StorageNetworkMod.MODID, "textures/gui/expandable_row.png"), W, SsnConsts.SQ);
  public static final TileableTexture row_right = new TileableTexture(Identifier.fromNamespaceAndPath(StorageNetworkMod.MODID, "textures/gui/expandable_row_right.png"), W, SsnConsts.SQ);
  public static final TileableTexture crafting = new TileableTexture(Identifier.fromNamespaceAndPath(StorageNetworkMod.MODID, "textures/gui/expandable_crafting.png"), W, 66);
  public static final TileableTexture crafting_right = new TileableTexture(Identifier.fromNamespaceAndPath(StorageNetworkMod.MODID, "textures/gui/expandable_crafting_right.png"), W, 66);
  public static final TileableTexture player = new TileableTexture(Identifier.fromNamespaceAndPath(StorageNetworkMod.MODID, "textures/gui/expandable_player.png"), 176, 84);

  public static List<SearchHandler> searchHandlers = new ArrayList<>();
  protected static final Button.CreateNarration DEFAULT_NARRATION = (supplier) -> {
    return supplier.get();
  };
  private ItemStack stackUnderMouse = ItemStack.EMPTY;
  public List<ItemStack> stacks;
  private EditBox searchBar;
  public ButtonRequest directionBtn;
  public ButtonRequest sortBtn;
  public ButtonRequest jeiBtn;
  public ButtonRequest focusBtn;
  public ButtonRequest clearGridBtn;
  public ButtonRequest fullStackBtn;
  private List<ItemSlotNetwork> slots;
  private final GuiNetwork gui;
  private long lastClick;
  //
  private int page = 1;
  private int maxPage = 1;
  private int lines = 4;
  private int columns = 9;
  public int scrollHeight = 152;
  private int scrollWidth = 176;//defaults to WIDTH
  //
  private int xNetwork = 8;
  private int yNetwork = 10;
  private final NetworkScreenSize size;

  public DefaultNetworkWidget(GuiNetwork gui, NetworkScreenSize size) {
    this.gui = gui;
    stacks = Lists.newArrayList();
    slots = Lists.newArrayList();
    this.size = size;
    setScreenSize();
    ClientPacketDistributor.sendToServer(new RequestMessage());
    lastClick = System.currentTimeMillis();
  }

  public AbstractWidget getSearchBar() {
    return searchBar;
  }


  @Override
  public int getX(){
    return xNetwork;
  }
  @Override
  public int getY(){
    return yNetwork;
  }
  @Override
  public int getScrollHeight(){
    return scrollHeight;
  }

  @Override
  public ItemStack getStackUnderMouse() {
    return stackUnderMouse;
  }


  private void setScreenSize() {
    int buffer = 0;
    setLines(size.lines());
    setColumns(size.columns());
    switch (size) {
      case NORMAL:
        buffer = 59;
      break;
      case LARGE:
      break;
      case EXPANDED:
        buffer = -10;
        this.xNetwork = 10; // head.height();
        this.scrollWidth = W + 12 * 18; //imageWidth
      break;
    }
    scrollHeight = (SsnConsts.SQ + 1) * this.getLines() + buffer;
  }

  @Override
  public void init(Font font) {
    int x = gui.getGuiLeft() + 81;
    int y = gui.getGuiTop();
    switch (this.size) {
      case NORMAL -> {
        y += 96; //
      }
      case LARGE -> {
        y += 160; //
      }
      case EXPANDED -> {
        x += 80;
        y += 256 + 140;
      }
    }
    searchBar = new EditBox(font,
        x, y,
        85, font.lineHeight, null);
    searchBar.setMaxLength(30);
    searchBar.setBordered(false);
    searchBar.setVisible(true);
    searchBar.setTextColor(16777215);
    //    searchBar.setFocus(StorageNetwork.CONFIG.enableAutoSearchFocus());
    if (ModList.get().isLoaded("jei")) {
      initJei();
    }
    x = gui.getGuiLeft() + 6;
    y = this.searchBar.getY() - 4;
    if (this.size == NetworkScreenSize.EXPANDED) {
      x += 155;
      y += 16;
    }
    directionBtn = new ButtonRequest(
        x, y, "", (p) -> {
          gui.setDownwards(!gui.getDownwards());
          gui.syncDataToServer();
        }, DEFAULT_NARRATION);
    directionBtn.setHeight(16);
    x += 16;
    sortBtn = new ButtonRequest(x, y, "", (p) -> {
      gui.setSort(gui.getSort().next());
      gui.syncDataToServer();
    }, DEFAULT_NARRATION);
    sortBtn.setHeight(16);
    x += 16;
    if (ModList.get().isLoaded("jei")) {
      jeiBtn = new ButtonRequest(x, y, "", (p) -> {
        gui.setJeiSearchSynced(!gui.isJeiSearchSynced());
        gui.syncDataToServer();
      }, DEFAULT_NARRATION);
      jeiBtn.setHeight(16);
    }
    x = searchBar.getX() + searchBar.getWidth() + 2;
    y = searchBar.getY() - 2;
    focusBtn = new ButtonRequest(
        x, y, "", (p) -> {
          gui.setAutoFocus(!gui.getAutoFocus());
          gui.syncDataToServer();
        }, DEFAULT_NARRATION);
    focusBtn.setHeight(11);
    focusBtn.setWidth(6);
    if (this.getSize() != NetworkScreenSize.LARGE) {
      x = searchBar.getX() - 19;
      y = searchBar.getY() + 13;
      if (this.getSize() == NetworkScreenSize.EXPANDED) {
        //omg this is a bit of a mess i should refactor this
        x = searchBar.getX() - 99;
        y = searchBar.getY() - 2;
      }
      clearGridBtn = new ButtonRequest(
          x, y, "", (p) -> {
            ClientPacketDistributor.sendToServer(ClearRecipeMessage.INSTANCE);
            ClientPacketDistributor.sendToServer(new RequestMessage(0, ItemStack.EMPTY, false, false));
          }, DEFAULT_NARRATION);
      clearGridBtn.setHeight(7);
      clearGridBtn.setWidth(7);
      this.clearGridBtn.setTextureId(TextureEnum.CRAFTCLEAR);
      //full-stack craft toggle - sits next to the recipe output slot
      int fsX = gui.getGuiLeft() + 124;
      int fsY = gui.getGuiTop() + 128;
      if (this.getSize() == NetworkScreenSize.EXPANDED) {
        fsX = gui.getGuiLeft() + 204;
        fsY = gui.getGuiTop() + 260;
      }
      fullStackBtn = new ButtonRequest(
          fsX, fsY, "", (p) -> {
            gui.setFullStackCraft(!gui.isFullStackCraft());
            gui.syncDataToServer();
          }, DEFAULT_NARRATION);
      fullStackBtn.setHeight(16);
      fullStackBtn.setWidth(16);
    }
  }

  @Override
  public List<ItemStack> getStacks() {
    return stacks;
  }

  @Override
  public void setStacks(List<ItemStack> stacks) {
    this.stacks = stacks;
  }

  @Override
  public NetworkScreenSize getSize() {
    return size;
  }

  @Override
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

  @Override
  public void clearSearch() {
    if (searchBar == null) {
      return;
    }
    searchBar.setValue("");
    if (ModList.get().isLoaded("jei") && gui.isJeiSearchSynced()) {
      searchHandlers.forEach((handler) -> handler.setSearch(""));
    }
  }

  private boolean doesStackMatchSearch(ItemStack stack) {
    String searchText = searchBar.getValue();
    if (searchText.startsWith(EnumSearchPrefix.MOD.getPrefix())) { //  search modname 
      String name = CacheModName.getModNameForItem(stack.getItem());
      return name.toLowerCase().contains(searchText.toLowerCase().substring(1));
    }
    else if (searchText.startsWith(EnumSearchPrefix.TOOLTIP.getPrefix())) { // search tooltips
      String tooltipString;
      Minecraft mc = Minecraft.getInstance();
      List<Component> tooltip = stack.getTooltipLines(Item.TooltipContext.of(mc.level), mc.player, TooltipFlag.Default.NORMAL);
      List<String> unformattedTooltip = tooltip.stream().map(Component::getString).collect(Collectors.toList());
      tooltipString = Joiner.on(' ').join(unformattedTooltip).toLowerCase().trim();
      return tooltipString.contains(searchText.toLowerCase().substring(1));
    }
    else if (searchText.startsWith(EnumSearchPrefix.TAG.getPrefix())) { // search tags
      List<String> joiner = new ArrayList<>();
      for (Identifier oreId : stack.typeHolder().tags().map((tagKey) -> tagKey.location()).toList()) {
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

  private boolean canClick() {
    return System.currentTimeMillis() > lastClick + 100L;
  }

  private int getLines() {
    return lines;
  }

  private int getColumns() {
    return columns;
  }

  private  void setColumns(int c) {
    this.columns = c;
  }

  private  void setLines(int v) {
    lines = v;
  }

  private void applyScrollPaging(List<ItemStack> stacksToDisplay) {
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

  @Override
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

  private void rebuildItemSlots(List<ItemStack> stacksToDisplay) {
    slots = Lists.newArrayList();
    int index = (page - 1) * (getColumns());
    for (int row = 0; row < getLines(); row++) {
      for (int col = 0; col < getColumns(); col++) {
        if (index >= stacksToDisplay.size()) {
          break;
        }
        int in = index;
        slots.add(new ItemSlotNetwork(gui, stacksToDisplay.get(in),
            gui.getGuiLeft() + xNetwork + col * SsnConsts.SQ,
            gui.getGuiTop() + yNetwork + row * SsnConsts.SQ,
            stacksToDisplay.get(in).getCount(),
            gui.getGuiLeft(), gui.getGuiTop(), true));
        index++;
      }
    }
  }

  private boolean inSearchBar(double mouseX, double mouseY) {
    return gui.isInRegion(
        searchBar.getX() - gui.getGuiLeft(), searchBar.getY() - gui.getGuiTop(), // x, y
        searchBar.getWidth(), searchBar.getHeight(), // width, height
        mouseX, mouseY);
  }

  private void initJei() {
    try {
      if (gui != null && searchBar != null && gui.isJeiSearchSynced()) {
        Optional<String> searchResult = searchHandlers.stream().map(SearchHandler::getSearch).findFirst();
        searchResult.ifPresent(s -> searchBar.setValue(s));
      }
    }
    catch (Exception e) {
      LOGGER.error("Search bar error ", e);
    }
  }

  @Override
  public void syncTextToJei() {
    if (ModList.get().isLoaded("jei") && gui.isJeiSearchSynced()) {
      searchHandlers.forEach((handler) -> handler.setSearch(searchBar.getValue()));
    }
  }

  @Override
  public void drawGuiContainerForegroundLayer(GuiGraphicsExtractor ms, int mouseX, int mouseY, Font font) {
    for (ItemSlotNetwork slot : slots) {
      if (slot != null && slot.isMouseOverSlot(mouseX, mouseY)) {
        slot.drawTooltip(ms, mouseX, mouseY);
        return; // slots and btns do not overlap
      }
    }
    // 
    MutableComponent tooltip = null;
    if (directionBtn != null && directionBtn.isMouseOver(mouseX, mouseY)) {
      tooltip = Component.translatable("gui.storagenetwork.sort");
    }
    else if (sortBtn != null && sortBtn.isMouseOver(mouseX, mouseY)) {
      tooltip = Component.translatable("gui.storagenetwork.req.tooltip_" + gui.getSort().name().toLowerCase());
    }
    else if (clearGridBtn != null && clearGridBtn.isMouseOver(mouseX, mouseY)) {
      tooltip = Component.translatable("gui.storagenetwork.req.tooltip_cleargrid");
    }
    else if (focusBtn != null && focusBtn.isMouseOver(mouseX, mouseY)) {
      tooltip = Component.translatable("gui.storagenetwork.autofocus.tooltip." + gui.getAutoFocus());
    }
    else if (fullStackBtn != null && fullStackBtn.isMouseOver(mouseX, mouseY)) {
      tooltip = Component.translatable(gui.isFullStackCraft() ? "gui.storagenetwork.fullstack.on" : "gui.storagenetwork.fullstack.off");
    }
    else if (ModList.get().isLoaded("jei") && jeiBtn != null && jeiBtn.isMouseOver(mouseX, mouseY)) {
      tooltip = Component.translatable(gui.isJeiSearchSynced() ? "gui.storagenetwork.fil.tooltip_jei_on" : "gui.storagenetwork.fil.tooltip_jei_off");
    }
    else if (this.inSearchBar(mouseX, mouseY)) {
      //tooltip = new TranslationTextComponent("gui.storagenetwork.fil.tooltip_clear");
      if (!Minecraft.getInstance().hasShiftDown()) {
        tooltip = Component.translatable("gui.storagenetwork.shift");
      }
      else {
        List<Component> lis = Lists.newArrayList();
        lis.add(Component.translatable("gui.storagenetwork.fil.tooltip_mod")); //@
        lis.add(Component.translatable("gui.storagenetwork.fil.tooltip_tooltip")); //#
        lis.add(Component.translatable("gui.storagenetwork.fil.tooltip_tags")); //$
        lis.add(Component.translatable("gui.storagenetwork.fil.tooltip_clear")); //clear
        ms.setTooltipForNextFrame(font, lis, Optional.empty(), mouseX - gui.getGuiLeft(), mouseY - gui.getGuiTop());
        return; // all done, we have our tts rendered
      }
    }
    //do we have a tooltip
    if (tooltip != null) {
      ms.setTooltipForNextFrame(font, Lists.newArrayList(tooltip), Optional.empty(), mouseX - gui.getGuiLeft(), mouseY - gui.getGuiTop());
    }
  }

  @Override
  public void renderItemSlots(GuiGraphicsExtractor ms, int mouseX, int mouseY, Font font) {
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
    if (searchBar.isFocused() && searchBar.charTyped(new CharacterEvent(typedChar))) {
      ClientPacketDistributor.sendToServer(new RequestMessage(0, ItemStack.EMPTY, false, false));
      syncTextToJei();
      return true;
    }
    return false;
  }

  public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
    searchBar.setFocused(false);
    if (inSearchBar(mouseX, mouseY)) {
      searchBar.setFocused(true);
      if (mouseButton == SsnConsts.MOUSE_BTN_RIGHT) {
        clearSearch();
        return;
      }
    }
    LocalPlayer player = Minecraft.getInstance().player;
    if (player == null || !this.canClick()) {
      return;
    }
    ItemStack stackCarriedByMouse = player.containerMenu.getCarried();
    if (!stackUnderMouse.isEmpty()
        && (mouseButton == SsnConsts.MOUSE_BTN_LEFT || mouseButton == SsnConsts.MOUSE_BTN_RIGHT)
        && stackCarriedByMouse.isEmpty()) {
      // Request an item (from the network) if we are in the upper section of the GUI 
      ClientPacketDistributor.sendToServer(new RequestMessage(mouseButton, this.stackUnderMouse.copy(), Minecraft.getInstance().hasShiftDown(),
          Minecraft.getInstance().hasAltDown() || Minecraft.getInstance().hasControlDown()));
      this.lastClick = System.currentTimeMillis();
    }
    else if (!stackCarriedByMouse.isEmpty() && inField((int) mouseX, (int) mouseY)) {
      // Insert the item held by the mouse into the network
      ClientPacketDistributor.sendToServer(new InsertMessage(0, mouseButton));
      this.lastClick = System.currentTimeMillis();
    }
  }

  private boolean inField(int mouseX, int mouseY) {
    int fieldHeight = 0;
    switch (size) {
      case NORMAL:
        fieldHeight = 90;
      break;
      case LARGE:
        fieldHeight = 172;
      break;
      case EXPANDED:
        fieldHeight = 390;
      break;
    }
    boolean inField = mouseX > (gui.getGuiLeft() + 7) && mouseX < (gui.getGuiLeft() + this.scrollWidth - 7)
        && mouseY > (gui.getGuiTop() + 7) && mouseY < (gui.getGuiTop() + fieldHeight);
    return inField;
  }

  private void sortStackWrappers(List<ItemStack> stacksToDisplay) {
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
            return CacheModName.getModNameForItem(o2.getItem()).compareToIgnoreCase(CacheModName.getModNameForItem(o1.getItem())) * mul;
        }
        return 0;
      }
    });
  }
  @Override
  public void renderSearchBar(GuiGraphicsExtractor ms, int mouseX, int mouseY, float partialTicks) {
    searchBar.extractRenderState(ms, mouseX, mouseY, partialTicks);
  }

  @Override
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
    if (this.clearGridBtn != null) {}
    focusBtn.setTextureId(gui.getAutoFocus() ? TextureEnum.RED : TextureEnum.GREY);
    directionBtn.setTextureId(gui.getDownwards() ? TextureEnum.SORT_DOWN : TextureEnum.SORT_UP);
    if (fullStackBtn != null) {
      fullStackBtn.setTextureId(gui.isFullStackCraft() ? TextureEnum.SHIFT_DEFAULT : TextureEnum.SHIFT_SINGLE);
    }
    if (jeiBtn != null && ModList.get().isLoaded("jei")) {
      jeiBtn.setTextureId(gui.isJeiSearchSynced() ? TextureEnum.JEI_GREEN : TextureEnum.JEI_RED);
    }
  }

  private void blitSegment(GuiGraphicsExtractor ms, TileableTexture tt, int xpos, int ypos) {
    ms.blit(RenderPipelines.GUI_TEXTURED, tt.texture(), xpos, ypos, 0, 0, tt.width(), tt.height(), tt.width(), tt.height());
  }

  @Override
  public void renderBgExpanded(GuiGraphicsExtractor ms, int xCenter, int yCenter) {
    //render the top
    int xpos = xCenter;
    int ypos = yCenter;
    blitSegment(ms, head, xpos, ypos);
    blitSegment(ms, head_right, xpos + W, ypos);
    ypos += head.height();
    //render the rows
    for (int line = 0; line < this.getLines(); line++) {
      blitSegment(ms, row, xpos, ypos);
      blitSegment(ms, row_right, xpos + W, ypos);
      ypos += row.height();
    }
    blitSegment(ms, crafting, xpos, ypos);
    blitSegment(ms, crafting_right, xpos + W, ypos);
    ypos += crafting.height() - 4;
    blitSegment(ms, player, xpos, ypos);
  }

  @Override
  public void keyPressed(int keyCode, int scanCode, int b) {
     searchBar.keyPressed(new KeyEvent(keyCode, scanCode, b));
  }
}
