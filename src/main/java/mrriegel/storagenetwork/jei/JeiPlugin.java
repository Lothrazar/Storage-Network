package mrriegel.storagenetwork.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mrriegel.storagenetwork.block.request.ContainerRequest;
import mrriegel.storagenetwork.gui.GuiHandler;
import mrriegel.storagenetwork.gui.fb.ContainerFastRemote;
import mrriegel.storagenetwork.gui.fb.ContainerFastRequest;
import mrriegel.storagenetwork.item.remote.ContainerRemote;
import mrriegel.storagenetwork.registry.ModBlocks;
import net.minecraft.item.ItemStack;

@JEIPlugin
public class JeiPlugin implements IModPlugin {

  @Override
  public void register(IModRegistry registry) {
    registry.getRecipeTransferRegistry().addRecipeTransferHandler(new RequestRecipeTransferHandler<>(ContainerRequest.class), VanillaRecipeCategoryUid.CRAFTING);
    registry.getRecipeTransferRegistry().addRecipeTransferHandler(new RequestRecipeTransferHandlerRemote<>(ContainerRemote.class), VanillaRecipeCategoryUid.CRAFTING);
    registry.addRecipeCatalyst(new ItemStack(ModBlocks.request), VanillaRecipeCategoryUid.CRAFTING);
    registry.getRecipeTransferRegistry().addUniversalRecipeTransferHandler(new ProcessRecipeTransferHandler());
    if (GuiHandler.FB_LOADED) {
      registry.getRecipeTransferRegistry().addRecipeTransferHandler(new RequestRecipeTransferHandler<>(ContainerFastRequest.class), VanillaRecipeCategoryUid.CRAFTING);
      registry.getRecipeTransferRegistry().addRecipeTransferHandler(new RequestRecipeTransferHandlerRemote<>(ContainerFastRemote.class), VanillaRecipeCategoryUid.CRAFTING);
      registry.getRecipeTransferRegistry().addRecipeTransferHandler(new RequestRecipeTransferHandler<>(ContainerFastRequest.Client.class), VanillaRecipeCategoryUid.CRAFTING);
      registry.getRecipeTransferRegistry().addRecipeTransferHandler(new RequestRecipeTransferHandlerRemote<>(ContainerFastRemote.Client.class), VanillaRecipeCategoryUid.CRAFTING);
    }
  }
}
