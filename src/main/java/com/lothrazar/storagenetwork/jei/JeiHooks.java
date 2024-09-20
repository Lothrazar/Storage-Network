package com.lothrazar.storagenetwork.jei;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IJeiKeyMappings;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.ModList;

public class JeiHooks {

  private static boolean isJeiLoaded() {
    return ModList.get().isLoaded("jei");
  }

  private static IJeiRuntime getRuntime() {
    if (!isJeiLoaded()) {
      return null;
    }
    try {
      return JeiPlugin.runtime;
    }
    catch (Exception e) {
      return null;
    }
  }

  private static String getJeiTextInternal() {
    return getRuntime().getIngredientFilter().getFilterText();
  }

  public static void testJeiKeybind(InputConstants.Key keyCode, ItemStack stackUnderMouse) {
    try {
      if (!isJeiLoaded() || getRuntime() == null) {
        return;
      }
      if (stackUnderMouse.is(Items.AIR)) {
        return;
      }
      IJeiKeyMappings keys = getRuntime().getKeyMappings();
      final boolean showRecipe = keys.getShowRecipe().isActiveAndMatches(keyCode); // || KeyBindings.showRecipe.get(1).isActiveAndMatches(keyCode);
      final boolean showUses = keys.getShowUses().isActiveAndMatches(keyCode); // || KeyBindings.showUses.get(1).isActiveAndMatches(keyCode);
      if (showRecipe || showUses) {
        IRecipesGui gui = getRuntime().getRecipesGui();
        IJeiHelpers helpers = getRuntime().getJeiHelpers();
        RecipeIngredientRole mode = showRecipe ? RecipeIngredientRole.OUTPUT : RecipeIngredientRole.INPUT;
        var focus = helpers.getFocusFactory().createFocus(mode, VanillaTypes.ITEM_STACK, stackUnderMouse);
        gui.show(focus);
      }
    }
    catch (Exception e) {
      // JEI not installed i guess lol
    }
  }
}
