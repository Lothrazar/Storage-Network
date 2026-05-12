package com.lothrazar.storagenetwork.registry;

import java.util.List;
import com.lothrazar.storagenetwork.StorageNetworkMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = StorageNetworkMod.MODID)
public class SsnTab {

  private static final ResourceKey<CreativeModeTab> TAB = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath(StorageNetworkMod.MODID, "tab"));

  @SubscribeEvent
  public static void onCreativeModeTabRegister(RegisterEvent event) {
    event.register(Registries.CREATIVE_MODE_TAB, helper -> {
      helper.register(TAB, CreativeModeTab.builder().icon(() -> new ItemStack(SsnRegistry.Blocks.REQUEST.get()))
          .title(Component.translatable("itemGroup." + StorageNetworkMod.MODID))
          .displayItems((enabledFlags, populator) -> {
            List<ItemStack> stacks = SsnRegistry.ITEMS.getEntries().stream().map(reg -> new ItemStack(reg.get())).toList();
            populator.acceptAll(stacks);
          }).build());
    });
  }
}
