package com.lothrazar.storagenetwork.jei;

import java.util.Optional;
import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.block.request.ContainerNetworkCraftingTable;
import com.lothrazar.storagenetwork.gui.NetworkWidget;
import com.lothrazar.storagenetwork.item.remote.ContainerNetworkCraftingRemote;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;

@mezz.jei.api.JeiPlugin
public class JeiPlugin implements IModPlugin {

  public static IJeiRuntime runtime = null;

  @Override
  public ResourceLocation getPluginUid() {
    return new ResourceLocation(StorageNetworkMod.MODID, "jei");
  }

  @Override
  public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
    runtime = jeiRuntime;
  }

  @Override
  public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
    //    registration.addUniversalRecipeTransferHandler(null);
    // new non-universal
    //table
    registration.addRecipeTransferHandler(new RequestRecipeTransferHandler<ContainerNetworkCraftingTable>() {

      @Override
      public Class<? extends ContainerNetworkCraftingTable> getContainerClass() {
        return ContainerNetworkCraftingTable.class;
      }

      @Override
      public Optional<MenuType<ContainerNetworkCraftingTable>> getMenuType() {
        return Optional.of(SsnRegistry.Menus.REQUEST.get());
      }
    }, RecipeTypes.CRAFTING);
    //remote
    registration.addRecipeTransferHandler(new RequestRecipeTransferHandler<ContainerNetworkCraftingRemote>() {

      @Override
      public Class<? extends ContainerNetworkCraftingRemote> getContainerClass() {
        return ContainerNetworkCraftingRemote.class;
      }

      @Override
      public Optional<MenuType<ContainerNetworkCraftingRemote>> getMenuType() {
        return Optional.of(SsnRegistry.Menus.CRAFTING_REMOTE.get());
      }
    }, RecipeTypes.CRAFTING);
  }

  static {
    NetworkWidget.searchHandlers.add(new NetworkWidget.ISearchHandler() {

      @Override
      public void setSearch(String text) {
        if (runtime != null) {
          if (runtime.getIngredientFilter() != null) {
            runtime.getIngredientFilter().setFilterText(text);
          }
        }
      }

      @Override
      public String getSearch() {
        if (runtime != null) {
          if (runtime.getIngredientFilter() != null) {
            return runtime.getIngredientFilter().getFilterText();
          }
        }
        return "";
      }

      @Override
      public String getName() {
        return "JEI";
      }
    });
  }
}
