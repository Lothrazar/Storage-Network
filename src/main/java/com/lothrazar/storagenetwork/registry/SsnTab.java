package com.lothrazar.storagenetwork.registry;

import java.util.ArrayList;
import java.util.List;
import com.lothrazar.storagenetwork.StorageNetworkMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

public class SsnTab {

  private static final ResourceKey<CreativeModeTab> TAB = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath(StorageNetworkMod.MODID, "tab"));

  @SubscribeEvent
  public static void onCreativeModeTabRegister(RegisterEvent event) {
    event.register(Registries.CREATIVE_MODE_TAB, helper -> {
      helper.register(TAB, CreativeModeTab.builder().icon(() -> new ItemStack(SsnRegistry.Blocks.REQUEST.get()))
          .title(Component.translatable("itemGroup." + StorageNetworkMod.MODID))
          .displayItems((enabledFlags, populator) -> {
            List<ItemStack> stacks = new ArrayList<>();
            // If Patchouli is loaded, place the guidebook first.
            if (ModList.get().isLoaded("patchouli")) {
              try {
                // FQCN kept so the class doesn't load when Patchouli isn't present.
                ItemStack book = vazkii.patchouli.api.PatchouliAPI.get()
                    .getBookStack(ResourceLocation.fromNamespaceAndPath(StorageNetworkMod.MODID, "network_book"));
                if (!book.isEmpty()) {
                  stacks.add(book);
                }
              }
              catch (Throwable t) {
                StorageNetworkMod.LOGGER.warn("Patchouli book lookup failed: " + t);
              }
            }
            SsnRegistry.ITEMS.getEntries().forEach(reg -> stacks.add(new ItemStack(reg.get())));
            populator.acceptAll(stacks);
          }).build());
    });
  }
}
