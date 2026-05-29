package com.lothrazar.storagenetwork;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.lothrazar.storagenetwork.block.cable.export.ScreenCableExportFilter;
import com.lothrazar.storagenetwork.block.cable.inputfilter.ScreenCableImportFilter;
import com.lothrazar.storagenetwork.block.cable.processing.ScreenCableProcess;
import com.lothrazar.storagenetwork.block.cable.linkfilter.ScreenCableFilter;
import com.lothrazar.storagenetwork.block.collection.ScreenCollectionFilter;
import com.lothrazar.storagenetwork.block.cradle.CradleAdapterRegistry;
import com.lothrazar.storagenetwork.block.cradle.ScreenStorageCradle;
import com.lothrazar.storagenetwork.block.cradle.adapter.ItemHandlerCradleAdapter;
import com.lothrazar.storagenetwork.compat.ae2.Ae2CradleBootstrap;
import net.neoforged.fml.ModList;
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
import com.lothrazar.storagenetwork.registry.SsnTab;
import com.lothrazar.storagenetwork.registry.StorageNetworkCapabilities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(StorageNetworkMod.MODID)
public class StorageNetworkMod {

  public static final String MODID = "storagenetwork";
  public static final Logger LOGGER = LogManager.getLogger();
  public static ConfigRegistry CONFIG;

  public StorageNetworkMod(IEventBus modEventBus, ModContainer modContainer) {
    modContainer.registerConfig(ModConfig.Type.COMMON, ConfigRegistry.COMMON_CONFIG);
    modEventBus.addListener(StorageNetworkMod::setup);
    modEventBus.addListener(PacketRegistry::registerPayloads);
    modEventBus.register(SsnTab.class);
    modEventBus.register(StorageNetworkCapabilities.class);
    NeoForge.EVENT_BUS.register(new SsnEvents());
    SsnRegistry.Blocks.init();
    SsnRegistry.BLOCKS.register(modEventBus);
    SsnRegistry.Items.init();
    SsnRegistry.ITEMS.register(modEventBus);
    SsnRegistry.Tiles.init();
    SsnRegistry.TILES.register(modEventBus);
    SsnRegistry.Menus.init();
    SsnRegistry.CONTAINERS.register(modEventBus);
    if (FMLEnvironment.dist == Dist.CLIENT) {
      modEventBus.register(ClientEventRegistry.class);
      modEventBus.addListener(this::setupClient);
      modEventBus.addListener(this::registerScreens);
      modEventBus.addListener(this::registerMapping);
    }
  }

  private static void setup(FMLCommonSetupEvent event) {
    CONFIG = new ConfigRegistry();
    // Built-in cradle adapter (vanilla shulkers + any block/item exposing IItemHandler)
    CradleAdapterRegistry.register(new ItemHandlerCradleAdapter());
    // Optional compat - classloaded only if the host mod is present
    if (ModList.get().isLoaded("ae2")) {
      Ae2CradleBootstrap.register();
    }
  }

  private void setupClient(final FMLClientSetupEvent event) {
  }

  private void registerScreens(final RegisterMenuScreensEvent event) {
    event.register(SsnRegistry.Menus.REQUEST.get(), ScreenNetworkTable::new);
    event.register(SsnRegistry.Menus.FILTER_KABEL.get(), ScreenCableFilter::new);
    event.register(SsnRegistry.Menus.IMPORT_FILTER_KABEL.get(), ScreenCableImportFilter::new);
    event.register(SsnRegistry.Menus.EXPORT_KABEL.get(), ScreenCableExportFilter::new);
    event.register(SsnRegistry.Menus.PROCESS_KABEL.get(), ScreenCableProcess::new);
    event.register(SsnRegistry.Menus.INVENTORY_REMOTE.get(), ScreenNetworkRemote::new);
    event.register(SsnRegistry.Menus.CRAFTING_REMOTE.get(), ScreenNetworkCraftingRemote::new);
    event.register(SsnRegistry.Menus.INVENTORY.get(), ScreenNetworkInventory::new);
    event.register(SsnRegistry.Menus.COLLECTOR.get(), ScreenCollectionFilter::new);
    event.register(SsnRegistry.Menus.REQUEST_EXPANDED.get(), ScreenNetworkInventoryExpanded::new);
    event.register(SsnRegistry.Menus.EXPANDED_REMOTE.get(), ScreenNetworkExpandedRemote::new);
    event.register(SsnRegistry.Menus.STORAGE_CRADLE.get(), ScreenStorageCradle::new);
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
