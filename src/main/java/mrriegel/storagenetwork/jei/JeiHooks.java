package mrriegel.storagenetwork.jei;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.Focus;
import mrriegel.storagenetwork.StorageNetwork;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Optional;

public class JeiHooks {

  private static boolean jeiLoaded;

  public static boolean isJeiLoaded() {
    return jeiLoaded;
  }

  public static void setJeiLoaded(boolean jeiLoaded) {
    JeiHooks.jeiLoaded = jeiLoaded;
  }

  public static String getFilterText() {
    try {
      if (jeiLoaded) {
        return getJeiTextInternal();
      }
    }
    catch (Exception e) {
      StorageNetwork.instance.logger.error(" mezz.jei.Internal not found ", e);
    }
    return "";
  }

  /**
   * so if JEI is not loaded, this will be called but then its an empty FN
   *
   * @param s
   */
  public static void setFilterText(String s) {
    try {
      if (jeiLoaded) {
        setJeiTextInternal(s);
      }
    }
    catch (Exception e) {
      StorageNetwork.instance.logger.error(" mezz.jei.Internal not found ", e);
    }
  }

  @Optional.Method(modid = "jei")
  private static void setJeiTextInternal(String s) {
    mezz.jei.Internal.getRuntime().getIngredientFilter().setFilterText(s);
  }

  @Optional.Method(modid = "jei")
  private static String getJeiTextInternal() {
    return mezz.jei.Internal.getRuntime().getIngredientFilter().getFilterText();
  }

  @Optional.Method(modid = "jei")
  public static void testJeiKeybind(int keyCode, ItemStack stackUnderMouse) {
    final boolean showRecipe = KeyBindings.showRecipe.isActiveAndMatches(keyCode);
    final boolean showUses = KeyBindings.showUses.isActiveAndMatches(keyCode);
    if (showRecipe || showUses) {
      //      IClickedIngredient<?> clicked = getIngredientUnderMouseForKey(MouseHelper.getX(), MouseHelper.getY());
      //      if (clicked != null) {
      IFocus.Mode mode = showRecipe ? IFocus.Mode.OUTPUT : IFocus.Mode.INPUT;
      mezz.jei.Internal.getRuntime().getRecipesGui().show(new Focus<ItemStack>(mode, stackUnderMouse));
      //        clicked.onClickHandled();
      //        return true;
      //      }
    }
  }
}
