package com.lothrazar.storagenetwork;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.lothrazar.storagenetwork.block.cable.export.ScreenCableExportFilter;
import com.lothrazar.storagenetwork.block.cable.inputfilter.ScreenCableImportFilter;
import com.lothrazar.storagenetwork.block.cable.linkfilter.ScreenCableFilter;
import com.lothrazar.storagenetwork.block.collection.ScreenCollectionFilter;
import com.lothrazar.storagenetwork.block.expand.ScreenNetworkInventoryExpanded;
import com.lothrazar.storagenetwork.block.inventory.ScreenNetworkInventory;
import com.lothrazar.storagenetwork.block.request.ScreenNetworkTable;
import com.lothrazar.storagenetwork.item.remote.ScreenNetworkCraftingRemote;
import com.lothrazar.storagenetwork.item.remote.ScreenNetworkExpandedRemote;
import com.lothrazar.storagenetwork.item.remote.ScreenNetworkRemote;
import com.lothrazar.storagenetwork.registry.ClientEventRegistry;
import com.lothrazar.storagenetwork.registry.ConfigRegistry;
import com.lothrazar.storagenetwork.registry.PacketRegistry;
import com.lothrazar.storagenetwork.registry.SsnEvents;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod(StorageNetworkMod.MODID)
public class StorageNetworkMod {

  public static final String MODID = "storagenetwork";
  public static final Logger LOGGER = LogManager.getLogger();
  public static ConfigRegistry CONFIG;

  public StorageNetworkMod() {
    FMLJavaModLoadingContext.get().getModEventBus().addListener(StorageNetworkMod::setup);
    MinecraftForge.EVENT_BUS.register(new SsnRegistry.Tiles());
    MinecraftForge.EVENT_BUS.register(new SsnEvents());
    IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
    SsnRegistry.BLOCKS.register(bus);
    SsnRegistry.ITEMS.register(bus);
    SsnRegistry.TILES.register(bus);
    SsnRegistry.CONTAINERS.register(bus);
    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
      FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupClient);
      FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerMapping);
    });
  }

  private static void setup(FMLCommonSetupEvent event) {
    PacketRegistry.init();
    CONFIG = new ConfigRegistry(FMLPaths.CONFIGDIR.get().resolve(MODID + ".toml"));
  }

  private void setupClient(final FMLClientSetupEvent event) {
    MenuScreens.register(SsnRegistry.Menus.REQUEST.get(), ScreenNetworkTable::new);
    MenuScreens.register(SsnRegistry.Menus.FILTER_KABEL.get(), ScreenCableFilter::new);
    MenuScreens.register(SsnRegistry.Menus.IMPORT_FILTER_KABEL.get(), ScreenCableImportFilter::new);
    MenuScreens.register(SsnRegistry.Menus.EXPORT_KABEL.get(), ScreenCableExportFilter::new);
    MenuScreens.register(SsnRegistry.Menus.INVENTORY_REMOTE.get(), ScreenNetworkRemote::new);
    MenuScreens.register(SsnRegistry.Menus.CRAFTING_REMOTE.get(), ScreenNetworkCraftingRemote::new);
    MenuScreens.register(SsnRegistry.Menus.INVENTORY.get(), ScreenNetworkInventory::new);
    MenuScreens.register(SsnRegistry.Menus.COLLECTOR.get(), ScreenCollectionFilter::new);
    MenuScreens.register(SsnRegistry.Menus.REQUEST_EXPANDED.get(), ScreenNetworkInventoryExpanded::new);
    MenuScreens.register(SsnRegistry.Menus.EXPANDED_REMOTE.get(), ScreenNetworkExpandedRemote::new);
  }

  private void registerMapping(final RegisterKeyMappingsEvent event) {
    event.register(ClientEventRegistry.INVENTORY_KEY);
    event.register(ClientEventRegistry.COLLECTOR_TOGGLE_KEY);
  }

  public static void log(String s) {
    if (CONFIG.logspam()) {
      LOGGER.info(s);
    }
  }
}
