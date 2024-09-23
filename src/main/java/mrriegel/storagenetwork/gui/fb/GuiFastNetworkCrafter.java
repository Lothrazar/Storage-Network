package mrriegel.storagenetwork.gui.fb;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.mojang.realmsclient.gui.ChatFormatting;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.data.EnumSortType;
import mrriegel.storagenetwork.gui.IPublicGuiContainer;
import mrriegel.storagenetwork.gui.IStorageInventory;
import mrriegel.storagenetwork.gui.ItemSlotNetwork;
import mrriegel.storagenetwork.jei.JeiHooks;
import mrriegel.storagenetwork.jei.SearchSettings;
import mrriegel.storagenetwork.network.ClearRecipeMessage;
import mrriegel.storagenetwork.network.InsertMessage;
import mrriegel.storagenetwork.network.RequestMessage;
import mrriegel.storagenetwork.network.SortMessage;
import mrriegel.storagenetwork.registry.PacketRegistry;
import mrriegel.storagenetwork.util.UtilTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag.TooltipFlags;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.OreDictionary;
import shadows.fastbench.gui.GuiFastBench;

/**
 * Base class for Request table inventory and Remote inventory
 *
 *
 */
public abstract class GuiFastNetworkCrafter extends GuiFastBench implements IPublicGuiContainer, IStorageInventory {

  private static final int HEIGHT = 256;
  private static final int WIDTH = 176;
  private static final ResourceLocation texture = new ResourceLocation(StorageNetwork.MODID, "textures/gui/request.png");
  protected int page = 1, maxPage = 1;
  public List<ItemStack> stacks, craftableStacks;
  protected ItemStack stackUnderMouse = ItemStack.EMPTY;
  protected GuiTextField searchBar;
  protected GuiStorageButton directionBtn, sortBtn, keepBtn, jeiBtn, clearTextBtn;
  protected List<ItemSlotNetwork> slots;
  protected List<ItemStack> displayedStacks = null;
  protected Set<Integer> zeroStacks = new TreeSet<>();
  protected long lastClick;
  private boolean forceFocus;

  public GuiFastNetworkCrafter(EntityPlayer player, World world, BlockPos pos) {
    super(player.inventory, world, pos);
    this.xSize = WIDTH;
    this.ySize = HEIGHT;
    this.stacks = Lists.newArrayList();
    this.craftableStacks = Lists.newArrayList();
    PacketRegistry.INSTANCE.sendToServer(new RequestMessage());
    lastClick = System.currentTimeMillis();
  }

  @Override
  public void setStacks(List<ItemStack> stacks) {
    this.stacks = stacks;
    zeroStacks.clear();
    if (isShiftKeyDown() && displayedStacks != null) {
      for (int i = 0; i < displayedStacks.size(); i++) {
        ItemStack stack = displayedStacks.get(i);
        boolean match = false;
        for (ItemStack newStack : stacks) {
          if (ItemHandlerHelper.canItemStacksStack(newStack, stack)) {
            match = true;
            stack.setCount(newStack.getCount());
            break;
          }
        }
        // since lots of things don't do anything with empty stacks,
        // just have the custom item slot show zero with the size actually being 1
        if (!match) {
          zeroStacks.add(i);
          stack.setCount(1);
        }
      }
    }
  }

  @Override
  public void setCraftableStacks(List<ItemStack> stacks) {
    this.craftableStacks = stacks;
  }

  protected boolean canClick() {
    return System.currentTimeMillis() > lastClick + 100L;
  }

  @Override
  public void drawGradientRectP(int left, int top, int right, int bottom, int startColor, int endColor) {
    super.drawGradientRect(left, top, right, bottom, startColor, endColor);
  }

  @Override
  public FontRenderer getFont() {
    return this.fontRenderer;
  }

  @Override
  public boolean isPointInRegionP(int rectX, int rectY, int rectWidth, int rectHeight, int pointX, int pointY) {
    return super.isPointInRegion(rectX, rectY, rectWidth, rectHeight, pointX, pointY);
  }

  @Override
  public void renderToolTipP(ItemStack stack, int x, int y) {
    super.renderToolTip(stack, x, y);
  }

  @Override
  public void initGui() {
    super.initGui();
    Keyboard.enableRepeatEvents(true);
    searchBar = new GuiTextField(0, fontRenderer, guiLeft + 81, guiTop + 96, 85, fontRenderer.FONT_HEIGHT);
    searchBar.setMaxStringLength(30);
    searchBar.setEnableBackgroundDrawing(false);
    searchBar.setVisible(true);
    searchBar.setTextColor(16777215);
    searchBar.setFocused(true);
    searchBar.setText(SearchSettings.getSearch());
    directionBtn = new GuiStorageButton(0, guiLeft + 7, guiTop + 93, "");
    this.addButton(directionBtn);
    sortBtn = new GuiStorageButton(1, guiLeft + 21, guiTop + 93, "");
    this.addButton(sortBtn);
    keepBtn = new GuiStorageButton(6, guiLeft + 35, guiTop + 93, "");
    this.addButton(keepBtn);
    jeiBtn = new GuiStorageButton(4, guiLeft + 49, guiTop + 93, "");
    if (JeiHooks.isJeiLoaded()) {
      this.addButton(jeiBtn);
    }
    clearTextBtn = new GuiStorageButton(5, guiLeft + 64, guiTop + 93, "X");
    this.addButton(clearTextBtn);
  }

  private int getLines() {
    return 4;
  }

  private int getColumns() {
    return 9;
  }

  public abstract boolean getDownwards();

  public abstract void setDownwards(boolean d);

  public abstract EnumSortType getSort();

  public abstract void setSort(EnumSortType s);

  public abstract BlockPos getPos();

  protected abstract int getDim();

  protected boolean inField(int mouseX, int mouseY) {
    return mouseX > (guiLeft + 7) && mouseX < (guiLeft + xSize - 7) && mouseY > (guiTop + 7) && mouseY < (guiTop + 90);
  }

  protected boolean inSearchbar(int mouseX, int mouseY) {
    return isPointInRegion(searchBar.x - guiLeft, searchBar.y - guiTop, searchBar.width, fontRenderer.FONT_HEIGHT, mouseX, mouseY);
  }

  protected boolean inX(int mouseX, int mouseY) {
    return isPointInRegion(63, 110, 7, 7, mouseX, mouseY);
  }

  protected abstract boolean isScreenValid();

  private boolean doesStackMatchSearch(ItemStack stack) {
    String searchText = searchBar.getText();
    if (searchText.startsWith("@")) {
      String name = UtilTileEntity.getModNameForItem(stack.getItem());
      return name.toLowerCase().contains(searchText.toLowerCase().substring(1));
    }
    else if (searchText.startsWith("#")) {
      String tooltipString;
      List<String> tooltip = stack.getTooltip(mc.player, TooltipFlags.NORMAL);
      tooltipString = Joiner.on(' ').join(tooltip).toLowerCase();
      tooltipString = ChatFormatting.stripFormatting(tooltipString);
      return tooltipString.toLowerCase().contains(searchText.toLowerCase().substring(1));
    }
    else if (searchText.startsWith("$")) {
      StringBuilder oreDictStringBuilder = new StringBuilder();
      for (int oreId : OreDictionary.getOreIDs(stack)) {
        String oreName = OreDictionary.getOreName(oreId);
        oreDictStringBuilder.append(oreName).append(' ');
      }
      return oreDictStringBuilder.toString().toLowerCase().contains(searchText.toLowerCase().substring(1));
    }
    else if (searchText.startsWith("%")) {
      StringBuilder creativeTabStringBuilder = new StringBuilder();
      for (CreativeTabs creativeTab : stack.getItem().getCreativeTabs()) {
        if (creativeTab != null) {
          String creativeTabName = creativeTab.getTabLabel();
          creativeTabStringBuilder.append(creativeTabName).append(' ');
        }
      }
      return creativeTabStringBuilder.toString().toLowerCase().contains(searchText.toLowerCase().substring(1));
    }
    else {
      return stack.getDisplayName().toLowerCase().contains(searchText.toLowerCase());
    }
  }

  @Override
  public void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
    if (this.isScreenValid() == false) {
      return;
    }
    renderTextures();
    if (!isShiftKeyDown() || displayedStacks == null) {
      displayedStacks = applySearchTextToSlots();
      sortItemStacks(displayedStacks);
    }
    applyScrollPaging(displayedStacks);
    rebuildItemSlots(displayedStacks);
    renderItemSlots(mouseX, mouseY);
    searchBar.drawTextBox();
  }

  private void renderTextures() {
    this.drawDefaultBackground();//dim the background as normal
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    this.mc.getTextureManager().bindTexture(texture);
    int xCenter = (this.width - this.xSize) / 2;
    int yCenter = (this.height - this.ySize) / 2;
    this.drawTexturedModalRect(xCenter, yCenter, 0, 0, this.xSize, this.ySize);
  }

  private List<ItemStack> applySearchTextToSlots() {
    String searchText = searchBar.getText();
    List<ItemStack> stacksToDisplay = searchText.equals("") ? Lists.newArrayList(stacks) : Lists.newArrayList();
    if (!searchText.equals("")) {
      for (ItemStack stackWrapper : stacks) {
        if (doesStackMatchSearch(stackWrapper)) {
          stacksToDisplay.add(stackWrapper);
        }
      }
    }
    return stacksToDisplay;
  }

  private void renderItemSlots(int mouseX, int mouseY) {
    stackUnderMouse = ItemStack.EMPTY;
    for (ItemSlotNetwork slot : slots) {
      slot.drawSlot(mouseX, mouseY);
      if (slot.isMouseOverSlot(mouseX, mouseY)) {
        stackUnderMouse = slot.getStack();
      }
    }
    if (slots.isEmpty()) {
      stackUnderMouse = ItemStack.EMPTY;
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
        ItemStack stack = stacksToDisplay.get(index);
        int count = stack.getCount();
        if (zeroStacks.contains(index))
          count = 0;
        slots.add(new ItemSlotNetwork(this, stack, guiLeft + 8 + col * 18, guiTop + 10 + row * 18, count, guiLeft, guiTop, true));
        index++;
      }
    }
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

  private void sortItemStacks(List<ItemStack> stacksToDisplay) {
    Collections.sort(stacksToDisplay, new Comparator<ItemStack>() {

      int mul = getDownwards() ? -1 : 1;

      @Override
      public int compare(ItemStack o2, ItemStack o1) {
        switch (getSort()) {
          case AMOUNT:
            return Integer.compare(o1.getCount(), o2.getCount()) * mul;
          case NAME:
            return o2.getDisplayName().compareToIgnoreCase(o1.getDisplayName()) * mul;
          case MOD:
            return UtilTileEntity.getModNameForItem(o2.getItem()).compareToIgnoreCase(UtilTileEntity.getModNameForItem(o1.getItem())) * mul;
        }
        return 0;
      }
    });
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    super.drawScreen(mouseX, mouseY, partialTicks);
    super.renderHoveredToolTip(mouseX, mouseY);
    if (this.isScreenValid() == false) {
      mc.player.closeScreen();
      return;
    }
    drawTooltips(mouseX, mouseY);
  }

  @Override
  public void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
    this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 4, 4210752);
    if (this.isScreenValid() == false) {
      return;
    }
    if (forceFocus) {
      this.searchBar.setFocused(true);
      if (this.searchBar.isFocused()) {
        this.forceFocus = false;
      }
    }
  }

  public void drawTooltips(int mouseX, int mouseY) {
    for (ItemSlotNetwork s : slots) {
      if (s.isMouseOverSlot(mouseX, mouseY)) {
        s.drawTooltip(mouseX, mouseY);
      }
    }
    if (inSearchbar(mouseX, mouseY)) {
      List<String> lis = Lists.newArrayList();
      if (!isShiftKeyDown()) {
        lis.add(I18n.format("gui.storagenetwork.shift"));
      }
      else {
        lis.add(I18n.format("gui.storagenetwork.fil.tooltip_0"));
        lis.add(I18n.format("gui.storagenetwork.fil.tooltip_1"));
        lis.add(I18n.format("gui.storagenetwork.fil.tooltip_2"));
        lis.add(I18n.format("gui.storagenetwork.fil.tooltip_3"));
      }
      drawHoveringText(lis, mouseX, mouseY);
    }
    if (clearTextBtn.isMouseOver()) {
      drawHoveringText(Lists.newArrayList(I18n.format("gui.storagenetwork.tooltip_clear")), mouseX, mouseY);
    }
    if (sortBtn.isMouseOver()) {
      drawHoveringText(Lists.newArrayList(I18n.format("gui.storagenetwork.req.tooltip_" + getSort().toString())), mouseX, mouseY);
    }
    if (keepBtn != null && keepBtn.isMouseOver()) {
      String s = I18n.format(SearchSettings.isSearchKept() ? "gui.storagenetwork.fil.tooltip_keep_on" : "gui.storagenetwork.fil.tooltip_keep_off");
      drawHoveringText(Lists.newArrayList(s), mouseX, mouseY);
    }
    if (directionBtn.isMouseOver()) {
      drawHoveringText(Lists.newArrayList(I18n.format("gui.storagenetwork.sort")), mouseX, mouseY);
    }
    if (jeiBtn != null && jeiBtn.isMouseOver()) {
      String s = I18n.format(SearchSettings.isJeiSearchSynced() ? "gui.storagenetwork.fil.tooltip_jei_on" : "gui.storagenetwork.fil.tooltip_jei_off");
      drawHoveringText(Lists.newArrayList(s), mouseX, mouseY);
    }
  }

  @Override
  public void onGuiClosed() {
    super.onGuiClosed();
    Keyboard.enableRepeatEvents(false);
  }

  @Override
  public void actionPerformed(GuiButton button) throws IOException {
    super.actionPerformed(button);
    boolean doSort = true;
    if (button.id == directionBtn.id) {
      setDownwards(!getDownwards());
    }
    else if (button.id == sortBtn.id) {
      setSort(getSort().next());
    }
    else if (button.id == keepBtn.id) {
      doSort = false;
      SearchSettings.setKeepSearch(!SearchSettings.isSearchKept());
      SearchSettings.setSearch(searchBar.getText());
    }
    else if (button.id == jeiBtn.id) {
      doSort = false;
      SearchSettings.setJeiSearchSync(!SearchSettings.isJeiSearchSynced());
      SearchSettings.setSearch(searchBar.getText());
    }
    else if (button.id == this.clearTextBtn.id) {
      doSort = false;
      clearSearch();
      //      this.fieldOperationLimit.setFocused(true);//doesnt work..somethings overriding it?
      this.forceFocus = true;//we have to force it to go next-tick
    }
    if (doSort) {
      PacketRegistry.INSTANCE.sendToServer(new SortMessage(getPos(), getDownwards(), getSort()));
    }
  }

  private void clearSearch() {
    searchBar.setText("");
    SearchSettings.setSearch("");
  }

  @Override
  public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
    super.mouseClicked(mouseX, mouseY, mouseButton);
    searchBar.setFocused(false);
    if (inSearchbar(mouseX, mouseY)) {
      searchBar.setFocused(true);
      if (mouseButton == UtilTileEntity.MOUSE_BTN_RIGHT) {
        clearSearch();
      }
    }
    else if (inX(mouseX, mouseY)) {
      PacketRegistry.INSTANCE.sendToServer(new ClearRecipeMessage());
      PacketRegistry.INSTANCE.sendToServer(new RequestMessage(0, ItemStack.EMPTY, false, false));
    }
    else {
      ItemStack stackCarriedByMouse = mc.player.inventory.getItemStack();
      boolean middleClick = mouseButton == UtilTileEntity.MOUSE_BTN_MIDDLE_CLICK;
      if (middleClick && !stackCarriedByMouse.isEmpty()) {
        //weird delete bug
        return;
      }
      if (!stackUnderMouse.isEmpty() &&
          (mouseButton == UtilTileEntity.MOUSE_BTN_LEFT
              || mouseButton == UtilTileEntity.MOUSE_BTN_RIGHT
              || mouseButton == UtilTileEntity.MOUSE_BTN_MIDDLE_CLICK)
          && stackCarriedByMouse.isEmpty() && canClick()) {
        PacketRegistry.INSTANCE.sendToServer(new RequestMessage(mouseButton, stackUnderMouse, isShiftKeyDown(),
            mouseButton == UtilTileEntity.MOUSE_BTN_MIDDLE_CLICK));
        lastClick = System.currentTimeMillis();
      }
      else if (!stackCarriedByMouse.isEmpty() && inField(mouseX, mouseY) && canClick()) {
        PacketRegistry.INSTANCE.sendToServer(new InsertMessage(getDim(), mouseButton));
        lastClick = System.currentTimeMillis();
      }
    }
  }

  @Override
  public void keyTyped(char typedChar, int keyCode) throws IOException {
    if (!this.checkHotbarKeys(keyCode)) {
      Keyboard.enableRepeatEvents(true);
      if (searchBar.isFocused() && this.searchBar.textboxKeyTyped(typedChar, keyCode)) {
        PacketRegistry.INSTANCE.sendToServer(new RequestMessage(0, ItemStack.EMPTY, false, false));
        SearchSettings.setSearch(searchBar.getText());
        return;
      }
      else if (this.stackUnderMouse.isEmpty() == false) {
        try {
          JeiHooks.testJeiKeybind(keyCode, this.stackUnderMouse);
        }
        catch (Throwable e) {
          //its ok JEI not installed for maybe an addon mod is ok
        }
      }
    }
    super.keyTyped(typedChar, keyCode);
  }

  @Override
  public void updateScreen() {
    super.updateScreen();
    if (searchBar != null) {
      searchBar.updateCursorCounter();
    }
  }

  @Override
  public void handleMouseInput() throws IOException {
    super.handleMouseInput();
    int i = Mouse.getX() * this.width / this.mc.displayWidth;
    int j = this.height - Mouse.getY() * this.height / this.mc.displayHeight - 1;
    if (inField(i, j)) {
      int mouse = Mouse.getEventDWheel();
      if (mouse > 0 && page > 1) {
        page--;
      }
      if (mouse < 0 && page < maxPage) {
        page++;
      }
    }
  }

  public class GuiStorageButton extends GuiButton {

    public GuiStorageButton(int id, int x, int y, String str) {
      super(id, x, y, 14, 14, str);
    }

    public GuiStorageButton(int id, int x, int y, int width, String str) {
      super(id, x, y, width, 14, str);
    }

    @Override
    public void drawButton(Minecraft mc, int x, int y, float pticks) {
      if (this.visible) {
        FontRenderer fontrenderer = mc.fontRenderer;
        mc.getTextureManager().bindTexture(texture);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.hovered = x >= this.x && y >= this.y && x < this.x + this.width && y < this.y + this.height;
        int k = this.getHoverState(this.hovered);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.blendFunc(770, 771);
        this.drawTexturedModalRect(this.x, this.y, 162 + 14 * k, 0, width, height);
        if (id == directionBtn.id) {
          this.drawTexturedModalRect(this.x + 4, this.y + 3, WIDTH + (getDownwards() ? 6 : 0), 14, 6, 8);
        }
        if (id == sortBtn.id) {
          this.drawTexturedModalRect(this.x + 4, this.y + 3, 188 + (getSort() == EnumSortType.AMOUNT ? 6 : getSort() == EnumSortType.MOD ? 12 : 0), 14, 6, 8);
        }
        if (id == keepBtn.id) {
          this.drawTexturedModalRect(this.x + 4, this.y + 3, WIDTH + (SearchSettings.isSearchKept() ? 12 : 18), 22, 6, 8);
        }
        if (id == jeiBtn.id) {
          this.drawTexturedModalRect(this.x + 4, this.y + 3, WIDTH + (SearchSettings.isJeiSearchSynced() ? 0 : 6), 22, 6, 8);
        }
        this.mouseDragged(mc, x, y);
        int l = 14737632;
        if (packedFGColour != 0) {
          l = packedFGColour;
        }
        else if (!this.enabled) {
          l = 10526880;
        }
        else if (this.hovered) {
          l = 16777120;
        }
        this.drawCenteredString(fontrenderer, this.displayString, this.x + this.width / 2, this.y + (this.height - 8) / 2, l);
      }
    }
  }
}
