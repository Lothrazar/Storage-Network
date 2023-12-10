package com.lothrazar.storagenetwork.emi;

import com.lothrazar.storagenetwork.api.IGuiNetwork;
import com.lothrazar.storagenetwork.gui.ContainerNetwork;
import com.lothrazar.storagenetwork.gui.NetworkWidget;
import com.lothrazar.storagenetwork.network.RecipeMessage;
import com.lothrazar.storagenetwork.registry.ConfigRegistry;
import com.lothrazar.storagenetwork.registry.PacketRegistry;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.*;

public class EmiTransferHandler<T extends ContainerNetwork> implements StandardRecipeHandler<T> {

    @Override
    public List<Slot> getInputSources(T handler) {
        return null;
    }

    @Override
    public List<Slot> getCraftingSlots(T handler) {
        return null;
    }

    @Override
    public @Nullable Slot getOutputSlot(T handler) {
        return handler.getResultSlot();
    }

    @Override
    public EmiPlayerInventory getInventory(AbstractContainerScreen<T> screen) {
        List<EmiStack> stacks = new ArrayList<>();
        if (screen instanceof IGuiNetwork) {
            NetworkWidget main = ((IGuiNetwork) screen).getNetworkWidget();
            if (main != null) {
                List<ItemStack> networkStacks = main.getStacks();
                networkStacks.stream().map(EmiStack::of).forEach(stacks::add);
            }
        }
        for (Slot each : screen.getMenu().getPlayerSlots()) {
            EmiStack emiStack = EmiStack.of(each.getItem());
            stacks.add(emiStack);
        }
        return new EmiPlayerInventory(stacks);
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING && recipe.supportsRecipeTree();
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<T> context) {
        AbstractContainerScreen<T> screen = context.getScreen();
        CompoundTag nbt = buildRecipe(recipe, screen);
        PacketRegistry.INSTANCE.sendToServer(new RecipeMessage(nbt));
        Minecraft.getInstance().setScreen(screen);
        return true;
    }

    private CompoundTag buildRecipe(EmiRecipe recipe, AbstractContainerScreen<T> screen) {
        CompoundTag nbt = new CompoundTag();
        List<EmiIngredient> ingredients = recipe.getInputs();
        for (Slot slot : screen.getMenu().slots) {
            if (slot.container instanceof net.minecraft.world.inventory.CraftingContainer) {
                if (slot.getSlotIndex() > ingredients.size() - 1) {
                    continue;
                }
                EmiIngredient slotIngredient = ingredients.get(slot.getSlotIndex());
                if (slotIngredient == null) {
                    continue;
                }
                List<ItemStack> possibleItems = new ArrayList<>();
                for (EmiStack each : slotIngredient.getEmiStacks()) {
                    ItemStack stack = each.getItemStack();
                    possibleItems.add(stack);
                }
                if (possibleItems.isEmpty()) {
                    continue;
                }
                ListTag invList = new ListTag();
                for (int i = 0; i < possibleItems.size(); i++) {
                    if (i >= ConfigRegistry.RECIPEMAXTAGS.get()) {
                        break;
                    }
                    ItemStack itemStack = possibleItems.get(i);
                    if (!itemStack.isEmpty()) {
                        CompoundTag stackTag = new CompoundTag();
                        itemStack.save(stackTag);
                        invList.add(stackTag);
                    }
                }
                nbt.put("s" + (slot.getSlotIndex()), invList);
            }
        }
        return nbt;
    }
}
