package com.lothrazar.storagenetwork.compat.emi;

import com.lothrazar.storagenetwork.block.expand.ContainerNetworkInventoryExpanded;
import com.lothrazar.storagenetwork.block.expand.ScreenNetworkInventoryExpanded;
import com.lothrazar.storagenetwork.block.request.ContainerNetworkCraftingTable;
import com.lothrazar.storagenetwork.block.request.ScreenNetworkTable;
import com.lothrazar.storagenetwork.gui.ISearchHandler;
import com.lothrazar.storagenetwork.gui.DefaultNetworkWidget;
import com.lothrazar.storagenetwork.item.remote.ContainerNetworkCraftingRemote;
import com.lothrazar.storagenetwork.item.remote.ContainerNetworkExpandedRemote;
import com.lothrazar.storagenetwork.item.remote.ScreenNetworkCraftingRemote;
import com.lothrazar.storagenetwork.item.remote.ScreenNetworkExpandedRemote;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

@EmiEntrypoint
public class EMIPlugin implements EmiPlugin {

  @Override
  public void register(EmiRegistry registry) {
    registry.addWorkstation(VanillaEmiRecipeCategories.CRAFTING, EmiStack.of(SsnRegistry.Blocks.REQUEST.get()));
    registry.addRecipeHandler(SsnRegistry.Menus.REQUEST.get(), new EmiTransferHandler<ContainerNetworkCraftingTable>());
    registry.addRecipeHandler(SsnRegistry.Menus.CRAFTING_REMOTE.get(), new EmiTransferHandler<ContainerNetworkCraftingRemote>());
    registry.addRecipeHandler(SsnRegistry.Menus.REQUEST_EXPANDED.get(), new EmiTransferHandler<ContainerNetworkInventoryExpanded>());
    registry.addRecipeHandler(SsnRegistry.Menus.EXPANDED_REMOTE.get(), new EmiTransferHandler<ContainerNetworkExpandedRemote>());
    //        registry.addGenericDragDropHandler(new EmiGhostIngredientHandler());
    registry.addGenericStackProvider((scr, x, y) -> {
      if (scr instanceof ScreenNetworkTable || scr instanceof ScreenNetworkCraftingRemote || scr instanceof ScreenNetworkInventoryExpanded || scr instanceof ScreenNetworkExpandedRemote) {
        net.minecraft.world.inventory.Slot sl = ((AbstractContainerScreen<?>) scr).getSlotUnderMouse();
        if (sl != null) return new EmiStackInteraction(EmiStack.of(sl.getItem()), null, false);
      }
      return EmiStackInteraction.EMPTY;
    });
  }

  static {
    DefaultNetworkWidget.searchHandlers.add(new ISearchHandler() {

      @Override
      public void setSearch(String set) {
        EmiApi.setSearchText(set);
      }

      @Override
      public String getSearch() {
        return EmiApi.getSearchText();
      }

      @Override
      public String getName() {
        return "EMI";
      }
    });
  }
}