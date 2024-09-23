package mrriegel.storagenetwork.jei;

import java.util.ArrayList;
import java.util.Map;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mrriegel.storagenetwork.block.cable.processing.ContainerCableProcessing;
import mrriegel.storagenetwork.network.ProcessRecipeMessage;
import mrriegel.storagenetwork.registry.PacketRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class ProcessRecipeTransferHandler implements IRecipeTransferHandler<ContainerCableProcessing> {

  @Override
  public Class<ContainerCableProcessing> getContainerClass() {
    return ContainerCableProcessing.class;
  }

  @Override
  public IRecipeTransferError transferRecipe(ContainerCableProcessing container, IRecipeLayout recipeLayout, EntityPlayer player,
      boolean maxTransfer, boolean doTransfer) {
    if (!doTransfer) {
      return null;
    }
    Map<Integer, ? extends IGuiIngredient<ItemStack>> ingredients = recipeLayout.getItemStacks().getGuiIngredients();
    ArrayList<ItemStack> inputs = new ArrayList<>(9), outputs = new ArrayList<>(9);
    for (Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> entry : ingredients.entrySet()) {
      IGuiIngredient<ItemStack> ingredient = entry.getValue();
      // TODO: prioritize ingredients in player inventory for convenience and stack similar inputs
      ItemStack displayed = ingredient.getDisplayedIngredient();
      if (displayed == null || displayed.isEmpty()) {
        continue;
      }
      if (ingredient.isInput()) {
        inputs.add(displayed);
      }
      else {
        outputs.add(displayed);
      }
    }
    PacketRegistry.INSTANCE.sendToServer(new ProcessRecipeMessage(inputs, outputs));
    return null;
  }
}
