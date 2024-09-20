package com.lothrazar.storagenetwork.emi;

import com.lothrazar.storagenetwork.block.request.ContainerNetworkCraftingTable;
import com.lothrazar.storagenetwork.block.request.ScreenNetworkTable;
import com.lothrazar.storagenetwork.gui.NetworkWidget;
import com.lothrazar.storagenetwork.item.remote.ContainerNetworkCraftingRemote;
import com.lothrazar.storagenetwork.item.remote.ScreenNetworkCraftingRemote;
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
//        registry.addGenericDragDropHandler(new EmiGhostIngredientHandler());
        registry.addGenericStackProvider((scr, x, y) -> {
            if(scr instanceof ScreenNetworkTable || scr instanceof ScreenNetworkCraftingRemote) {
                net.minecraft.world.inventory.Slot sl = ((AbstractContainerScreen<?>) scr).getSlotUnderMouse();
                if(sl != null)return new EmiStackInteraction(EmiStack.of(sl.getItem()), null, false);
            }
            return EmiStackInteraction.EMPTY;
        });
    }

    static {
        NetworkWidget.searchHandlers.add(new NetworkWidget.ISearchHandler() {

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