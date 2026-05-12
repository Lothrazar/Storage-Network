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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(StorageNetworkMod.MODID)
public class StorageNetworkMod {

  public static final String MODID = "storagenetwork";
  public static final Logger LOGGER = LogManager.getLogger();
  public static ConfigRegistry CONFIG;

  public StorageNetworkMod(IEventBus modEventBus) {
    modEventBus.addListener(StorageNetworkMod::setup);
    modEventBus.addListener(PacketRegistry::registerPayloads);
    NeoForge.EVENT_BUS.register(new SsnEvents());
    SsnRegistry.BLOCKS.register(modEventBus);
    SsnRegistry.ITEMS.register(modEventBus);
    SsnRegistry.TILES.register(modEventBus);
    SsnRegistry.CONTAINERS.register(modEventBus);
    if (FMLEnvironment.dist == Dist.CLIENT) {
      modEventBus.addListener(this::setupClient);
      modEventBus.addListener(this::registerMapping);
    }
  }

  private static void setup(FMLCommonSetupEvent event) {
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
