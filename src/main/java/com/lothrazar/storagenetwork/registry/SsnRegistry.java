package com.lothrazar.storagenetwork.registry;

import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.block.cable.BlockCable;
import com.lothrazar.storagenetwork.block.cable.TileCable;
import com.lothrazar.storagenetwork.block.cable.export.BlockCableExport;
import com.lothrazar.storagenetwork.block.cable.export.ContainerCableExportFilter;
import com.lothrazar.storagenetwork.block.cable.export.TileCableExport;
import com.lothrazar.storagenetwork.block.cable.input.BlockCableIO;
import com.lothrazar.storagenetwork.block.cable.input.TileCableIO;
import com.lothrazar.storagenetwork.block.cable.inputfilter.BlockCableImportFilter;
import com.lothrazar.storagenetwork.block.cable.inputfilter.ContainerCableImportFilter;
import com.lothrazar.storagenetwork.block.cable.inputfilter.TileCableImportFilter;
import com.lothrazar.storagenetwork.block.cable.link.BlockCableLink;
import com.lothrazar.storagenetwork.block.cable.link.TileCableLink;
import com.lothrazar.storagenetwork.block.cable.linkfilter.BlockCableFilter;
import com.lothrazar.storagenetwork.block.cable.linkfilter.ContainerCableFilter;
import com.lothrazar.storagenetwork.block.cable.linkfilter.TileCableFilter;
import com.lothrazar.storagenetwork.block.collection.BlockCollection;
import com.lothrazar.storagenetwork.block.collection.ContainerCollectionFilter;
import com.lothrazar.storagenetwork.block.collection.TileCollection;
import com.lothrazar.storagenetwork.block.exchange.BlockExchange;
import com.lothrazar.storagenetwork.block.exchange.TileExchange;
import com.lothrazar.storagenetwork.block.inventory.BlockInventory;
import com.lothrazar.storagenetwork.block.inventory.ContainerNetworkInventory;
import com.lothrazar.storagenetwork.block.inventory.TileInventory;
import com.lothrazar.storagenetwork.block.main.BlockMain;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.block.request.BlockRequest;
import com.lothrazar.storagenetwork.block.request.ContainerNetworkCraftingTable;
import com.lothrazar.storagenetwork.block.request.TileRequest;
import com.lothrazar.storagenetwork.item.ItemBuilder;
import com.lothrazar.storagenetwork.item.ItemCollector;
import com.lothrazar.storagenetwork.item.ItemPicker;
import com.lothrazar.storagenetwork.item.ItemUpgrade;
import com.lothrazar.storagenetwork.item.remote.ContainerNetworkCraftingRemote;
import com.lothrazar.storagenetwork.item.remote.ContainerNetworkRemote;
import com.lothrazar.storagenetwork.item.remote.ItemStorageCraftingRemote;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SsnRegistry {

  public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, StorageNetworkMod.MODID);
  public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, StorageNetworkMod.MODID);
  public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, StorageNetworkMod.MODID);
  public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, StorageNetworkMod.MODID);

  @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
  public static class Blocks {

    public static final RegistryObject<Block> REQUEST = BLOCKS.register("request", () -> new BlockRequest());
    public static final RegistryObject<Block> KABEL = BLOCKS.register("kabel", () -> new BlockCable());
    public static final RegistryObject<Block> MASTER = BLOCKS.register("master", () -> new BlockMain());
    public static final RegistryObject<Block> STORAGE_KABEL = BLOCKS.register("storage_kabel", () -> new BlockCableLink());
    public static final RegistryObject<Block> IMPORT_KABEL = BLOCKS.register("import_kabel", () -> new BlockCableIO());
    public static final RegistryObject<Block> IMPORT_FILTER_KABEL = BLOCKS.register("import_filter_kabel", () -> new BlockCableImportFilter());
    public static final RegistryObject<Block> FILTER_KABEL = BLOCKS.register("filter_kabel", () -> new BlockCableFilter());
    public static final RegistryObject<Block> EXPORT_KABEL = BLOCKS.register("export_kabel", () -> new BlockCableExport());
    public static final RegistryObject<Block> INVENTORY = BLOCKS.register("inventory", () -> new BlockInventory());
    public static final RegistryObject<Block> EXCHANGE = BLOCKS.register("exchange", () -> new BlockExchange());
    public static final RegistryObject<Block> COLLECTOR = BLOCKS.register("collector", () -> new BlockCollection());
  }

  @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
  public static class Items {

    public static final RegistryObject<Item> REQUEST = ITEMS.register("request", () -> new BlockItem(Blocks.REQUEST.get(), new Item.Properties()));
    public static final RegistryObject<Item> KABEL = ITEMS.register("kabel", () -> new BlockItem(Blocks.KABEL.get(), new Item.Properties()));
    public static final RegistryObject<Item> INV = ITEMS.register("inventory", () -> new BlockItem(Blocks.INVENTORY.get(), new Item.Properties()));
    public static final RegistryObject<Item> MAS = ITEMS.register("master", () -> new BlockItem(Blocks.MASTER.get(), new Item.Properties()));
    public static final RegistryObject<Item> SK = ITEMS.register("storage_kabel", () -> new BlockItem(Blocks.STORAGE_KABEL.get(), new Item.Properties()));
    public static final RegistryObject<Item> IK = ITEMS.register("import_kabel", () -> new BlockItem(Blocks.IMPORT_KABEL.get(), new Item.Properties()));
    public static final RegistryObject<Item> IFK = ITEMS.register("import_filter_kabel", () -> new BlockItem(Blocks.IMPORT_FILTER_KABEL.get(), new Item.Properties()));
    public static final RegistryObject<Item> FK = ITEMS.register("filter_kabel", () -> new BlockItem(Blocks.FILTER_KABEL.get(), new Item.Properties()));
    public static final RegistryObject<Item> EK = ITEMS.register("export_kabel", () -> new BlockItem(Blocks.EXPORT_KABEL.get(), new Item.Properties()));
    public static final RegistryObject<Item> EXCHANGE = ITEMS.register("exchange", () -> new BlockItem(Blocks.EXCHANGE.get(), new Item.Properties()));
    public static final RegistryObject<Item> COL = ITEMS.register("collector", () -> new BlockItem(Blocks.COLLECTOR.get(), new Item.Properties()));
    public static final RegistryObject<ItemUpgrade> STACK_UPGRADE = ITEMS.register("stack_upgrade", () -> new ItemUpgrade(new Item.Properties()));
    public static final RegistryObject<ItemUpgrade> SPEED_UPGRADE = ITEMS.register("speed_upgrade", () -> new ItemUpgrade(new Item.Properties()));
    public static final RegistryObject<ItemUpgrade> SLOW_UPGRADE = ITEMS.register("slow_upgrade", () -> new ItemUpgrade(new Item.Properties()));
    public static final RegistryObject<ItemUpgrade> STOCK_UPGRADE = ITEMS.register("stock_upgrade", () -> new ItemUpgrade(new Item.Properties()));
    public static final RegistryObject<ItemUpgrade> OP_U = ITEMS.register("operation_upgrade", () -> new ItemUpgrade(new Item.Properties()));
    public static final RegistryObject<ItemUpgrade> SINGLE_UPGRADE = ITEMS.register("single_upgrade", () -> new ItemUpgrade(new Item.Properties()));
    public static final RegistryObject<ItemStorageCraftingRemote> INVENTORY_REMOTE = ITEMS.register("inventory_remote", () -> new ItemStorageCraftingRemote(new Item.Properties()));
    public static final RegistryObject<ItemStorageCraftingRemote> CRAFTING_REMOTE = ITEMS.register("crafting_remote", () -> new ItemStorageCraftingRemote(new Item.Properties()));
    public static final RegistryObject<Item> PICKER_REMOTE = ITEMS.register("picker_remote", () -> new ItemPicker(new Item.Properties()));
    public static final RegistryObject<ItemCollector> COLLECTOR_REMOTE = ITEMS.register("collector_remote", () -> new ItemCollector(new Item.Properties()));
    public static final RegistryObject<Item> BUILDER_REMOTE = ITEMS.register("builder_remote", () -> new ItemBuilder(new Item.Properties()));
  }

  @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
  public static class Tiles {

    public static final RegistryObject<BlockEntityType<TileMain>> MASTER = TILES.register("master", () -> BlockEntityType.Builder.of(TileMain::new, Blocks.MASTER.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileInventory>> INVENTORY = TILES.register("inventory", () -> BlockEntityType.Builder.of(TileInventory::new, Blocks.INVENTORY.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileRequest>> REQUEST = TILES.register("request", () -> BlockEntityType.Builder.of(TileRequest::new, Blocks.REQUEST.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileCable>> KABEL = TILES.register("kabel", () -> BlockEntityType.Builder.of(TileCable::new, Blocks.KABEL.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileCableLink>> STORAGE_KABEL = TILES.register("storage_kabel", () -> BlockEntityType.Builder.of(TileCableLink::new, Blocks.STORAGE_KABEL.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileCableIO>> IMPORT_KABEL = TILES.register("import_kabel", () -> BlockEntityType.Builder.of(TileCableIO::new, Blocks.IMPORT_KABEL.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileCableImportFilter>> IMPORT_FILTER_KABEL = TILES.register("import_filter_kabel", () -> BlockEntityType.Builder.of(TileCableImportFilter::new, Blocks.IMPORT_FILTER_KABEL.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileCableFilter>> FILTER_KABEL = TILES.register("filter_kabel", () -> BlockEntityType.Builder.of(TileCableFilter::new, Blocks.FILTER_KABEL.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileCableExport>> EXPORT_KABEL = TILES.register("export_kabel", () -> BlockEntityType.Builder.of(TileCableExport::new, Blocks.EXPORT_KABEL.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileExchange>> EXCHANGE = TILES.register("exchange", () -> BlockEntityType.Builder.of(TileExchange::new, Blocks.EXCHANGE.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileCollection>> COLLECTOR = TILES.register("collector", () -> BlockEntityType.Builder.of(TileCollection::new, Blocks.COLLECTOR.get()).build(null));
  }

  @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
  public static class Menus {

    public static final RegistryObject<MenuType<ContainerNetworkCraftingTable>> REQUEST = CONTAINERS.register("request", () -> IForgeMenuType.create((windowId, inv, data) -> {
      return new ContainerNetworkCraftingTable(windowId, inv.player.level(), data.readBlockPos(), inv, inv.player);
    }));
    public static final RegistryObject<MenuType<ContainerCollectionFilter>> COLLECTOR = CONTAINERS.register("collector", () -> IForgeMenuType.create((windowId, inv, data) -> {
      return new ContainerCollectionFilter(windowId, inv.player.level(), data.readBlockPos(), inv, inv.player);
    }));
    public static final RegistryObject<MenuType<ContainerCableFilter>> FILTER_KABEL = CONTAINERS.register("filter_kabel", () -> IForgeMenuType.create((windowId, inv, data) -> {
      return new ContainerCableFilter(windowId, inv.player.level(), data.readBlockPos(), inv, inv.player);
    }));
    public static final RegistryObject<MenuType<ContainerCableImportFilter>> IMPORT_FILTER_KABEL = CONTAINERS.register("import_filter_kabel", () -> IForgeMenuType.create((windowId, inv, data) -> {
      return new ContainerCableImportFilter(windowId, inv.player.level(), data.readBlockPos(), inv, inv.player);
    }));
    public static final RegistryObject<MenuType<ContainerCableExportFilter>> EXPORT_KABEL = CONTAINERS.register("export_kabel", () -> IForgeMenuType.create((windowId, inv, data) -> {
      return new ContainerCableExportFilter(windowId, inv.player.level(), data.readBlockPos(), inv, inv.player);
    }));
    public static final RegistryObject<MenuType<ContainerNetworkInventory>> INVENTORY = CONTAINERS.register("inventory", () -> IForgeMenuType.create((windowId, inv, data) -> {
      return new ContainerNetworkInventory(windowId, inv.player.level(), data.readBlockPos(), inv, inv.player);
    }));
    public static final RegistryObject<MenuType<ContainerNetworkRemote>> INVENTORY_REMOTE = CONTAINERS.register("inventory_remote", () -> IForgeMenuType.create((windowId, inv, data) -> {
      return new ContainerNetworkRemote(windowId, inv.player.getInventory());
    }));
    public static final RegistryObject<MenuType<ContainerNetworkCraftingRemote>> CRAFTING_REMOTE = CONTAINERS.register("crafting_remote", () -> IForgeMenuType.create((windowId, inv, data) -> {
      return new ContainerNetworkCraftingRemote(windowId, inv.player.getInventory());
    }));
  }
}
