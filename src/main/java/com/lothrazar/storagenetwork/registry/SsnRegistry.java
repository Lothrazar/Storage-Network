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
import com.lothrazar.storagenetwork.block.cable.processing.BlockCableProcess;
import com.lothrazar.storagenetwork.block.cable.processing.TileCableProcess;
import com.lothrazar.storagenetwork.block.collection.BlockCollection;
import com.lothrazar.storagenetwork.block.collection.ContainerCollectionFilter;
import com.lothrazar.storagenetwork.block.collection.TileCollection;
import com.lothrazar.storagenetwork.block.exchange.BlockExchange;
import com.lothrazar.storagenetwork.block.exchange.TileExchange;
import com.lothrazar.storagenetwork.block.expand.BlockInventoryExpanded;
import com.lothrazar.storagenetwork.block.expand.ContainerNetworkInventoryExpanded;
import com.lothrazar.storagenetwork.block.expand.TileInventoryExpanded;
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
import com.lothrazar.storagenetwork.item.remote.ContainerNetworkExpandedRemote;
import com.lothrazar.storagenetwork.item.remote.ContainerNetworkRemote;
import com.lothrazar.storagenetwork.item.remote.ItemRemote;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SsnRegistry {

  public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(StorageNetworkMod.MODID);
  public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(StorageNetworkMod.MODID);
  public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, StorageNetworkMod.MODID);
  public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(Registries.MENU, StorageNetworkMod.MODID);

  public static class Blocks {

    public static void init() {}

    public static final DeferredBlock<Block> REQUEST = BLOCKS.register("request", () -> new BlockRequest());
    public static final DeferredBlock<Block> KABEL = BLOCKS.register("kabel", () -> new BlockCable());
    public static final DeferredBlock<Block> MASTER = BLOCKS.register("master", () -> new BlockMain());
    public static final DeferredBlock<Block> STORAGE_KABEL = BLOCKS.register("storage_kabel", () -> new BlockCableLink());
    public static final DeferredBlock<Block> IMPORT_KABEL = BLOCKS.register("import_kabel", () -> new BlockCableIO());
    public static final DeferredBlock<Block> IMPORT_FILTER_KABEL = BLOCKS.register("import_filter_kabel", () -> new BlockCableImportFilter());
    public static final DeferredBlock<Block> FILTER_KABEL = BLOCKS.register("filter_kabel", () -> new BlockCableFilter());
    public static final DeferredBlock<Block> EXPORT_KABEL = BLOCKS.register("export_kabel", () -> new BlockCableExport());
    public static final DeferredBlock<Block> PROCESS_KABEL = BLOCKS.register("process_kabel", () -> new BlockCableProcess());
    public static final DeferredBlock<Block> INVENTORY = BLOCKS.register("inventory", () -> new BlockInventory());
    public static final DeferredBlock<Block> REQUEST_EXPANDED = BLOCKS.register("request_expanded", () -> new BlockInventoryExpanded());
    public static final DeferredBlock<Block> EXCHANGE = BLOCKS.register("exchange", () -> new BlockExchange());
    public static final DeferredBlock<Block> COLLECTOR = BLOCKS.register("collector", () -> new BlockCollection());
  }

  public static class Items {

    public static void init() {}

    public static final DeferredItem<Item> REQUEST = ITEMS.register("request", () -> new BlockItem(Blocks.REQUEST.get(), new Item.Properties()));
    public static final DeferredItem<Item> KABEL = ITEMS.register("kabel", () -> new BlockItem(Blocks.KABEL.get(), new Item.Properties()));
    public static final DeferredItem<Item> INVENTORY = ITEMS.register("inventory", () -> new BlockItem(Blocks.INVENTORY.get(), new Item.Properties()));
    public static final DeferredItem<Item> REQUEST_EXPANDED = ITEMS.register("request_expanded", () -> new BlockItem(Blocks.REQUEST_EXPANDED.get(), new Item.Properties()));
    public static final DeferredItem<Item> MAS = ITEMS.register("master", () -> new BlockItem(Blocks.MASTER.get(), new Item.Properties()));
    public static final DeferredItem<Item> SK = ITEMS.register("storage_kabel", () -> new BlockItem(Blocks.STORAGE_KABEL.get(), new Item.Properties()));
    public static final DeferredItem<Item> IK = ITEMS.register("import_kabel", () -> new BlockItem(Blocks.IMPORT_KABEL.get(), new Item.Properties()));
    public static final DeferredItem<Item> IFK = ITEMS.register("import_filter_kabel", () -> new BlockItem(Blocks.IMPORT_FILTER_KABEL.get(), new Item.Properties()));
    public static final DeferredItem<Item> FK = ITEMS.register("filter_kabel", () -> new BlockItem(Blocks.FILTER_KABEL.get(), new Item.Properties()));
    public static final DeferredItem<Item> EK = ITEMS.register("export_kabel", () -> new BlockItem(Blocks.EXPORT_KABEL.get(), new Item.Properties()));
    // public static final DeferredItem<Item> PK = ITEMS.register("process_kabel", () -> new BlockItem(Blocks.PROCESS_KABEL.get(), new Item.Properties()));
    public static final DeferredItem<Item> EXCHANGE = ITEMS.register("exchange", () -> new BlockItem(Blocks.EXCHANGE.get(), new Item.Properties()));
    public static final DeferredItem<Item> COL = ITEMS.register("collector", () -> new BlockItem(Blocks.COLLECTOR.get(), new Item.Properties()));
    public static final DeferredItem<ItemUpgrade> STACK_UPGRADE = ITEMS.register("stack_upgrade", () -> new ItemUpgrade(new Item.Properties()));
    public static final DeferredItem<ItemUpgrade> SPEED_UPGRADE = ITEMS.register("speed_upgrade", () -> new ItemUpgrade(new Item.Properties()));
    public static final DeferredItem<ItemUpgrade> SLOW_UPGRADE = ITEMS.register("slow_upgrade", () -> new ItemUpgrade(new Item.Properties()));
    public static final DeferredItem<ItemUpgrade> STOCK_UPGRADE = ITEMS.register("stock_upgrade", () -> new ItemUpgrade(new Item.Properties()));
    public static final DeferredItem<ItemUpgrade> OP_U = ITEMS.register("operation_upgrade", () -> new ItemUpgrade(new Item.Properties()));
    public static final DeferredItem<ItemUpgrade> SINGLE_UPGRADE = ITEMS.register("single_upgrade", () -> new ItemUpgrade(new Item.Properties()));
    public static final DeferredItem<ItemUpgrade> VOID_UPGRADE = ITEMS.register("void_upgrade", () -> new ItemUpgrade(new Item.Properties()));
    public static final DeferredItem<ItemRemote> INVENTORY_REMOTE = ITEMS.register("inventory_remote", () -> new ItemRemote(new Item.Properties()));
    public static final DeferredItem<ItemRemote> CRAFTING_REMOTE = ITEMS.register("crafting_remote", () -> new ItemRemote(new Item.Properties()));
    public static final DeferredItem<Item> PICKER_REMOTE = ITEMS.register("picker_remote", () -> new ItemPicker(new Item.Properties()));
    public static final DeferredItem<ItemCollector> COLLECTOR_REMOTE = ITEMS.register("collector_remote", () -> new ItemCollector(new Item.Properties()));
    public static final DeferredItem<Item> BUILDER_REMOTE = ITEMS.register("builder_remote", () -> new ItemBuilder(new Item.Properties()));
    public static final DeferredItem<ItemRemote> EXPANDED_REMOTE = ITEMS.register("expanded_remote", () -> new ItemRemote(new Item.Properties()));
  }

  public static class Tiles {

    public static void init() {}

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileMain>> MASTER = TILES.register("master", () -> BlockEntityType.Builder.of(TileMain::new, Blocks.MASTER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileInventory>> INVENTORY = TILES.register("inventory", () -> BlockEntityType.Builder.of(TileInventory::new, Blocks.INVENTORY.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileInventoryExpanded>> REQUEST_EXPANDED = TILES.register("request_expanded", () -> BlockEntityType.Builder.of(TileInventoryExpanded::new, Blocks.REQUEST_EXPANDED.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileRequest>> REQUEST = TILES.register("request", () -> BlockEntityType.Builder.of(TileRequest::new, Blocks.REQUEST.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileCable>> KABEL = TILES.register("kabel", () -> BlockEntityType.Builder.of(TileCable::new, Blocks.KABEL.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileCableLink>> STORAGE_KABEL = TILES.register("storage_kabel", () -> BlockEntityType.Builder.of(TileCableLink::new, Blocks.STORAGE_KABEL.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileCableIO>> IMPORT_KABEL = TILES.register("import_kabel", () -> BlockEntityType.Builder.of(TileCableIO::new, Blocks.IMPORT_KABEL.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileCableImportFilter>> IMPORT_FILTER_KABEL = TILES.register("import_filter_kabel", () -> BlockEntityType.Builder.of(TileCableImportFilter::new, Blocks.IMPORT_FILTER_KABEL.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileCableFilter>> FILTER_KABEL = TILES.register("filter_kabel", () -> BlockEntityType.Builder.of(TileCableFilter::new, Blocks.FILTER_KABEL.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileCableExport>> EXPORT_KABEL = TILES.register("export_kabel", () -> BlockEntityType.Builder.of(TileCableExport::new, Blocks.EXPORT_KABEL.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileExchange>> EXCHANGE = TILES.register("exchange", () -> BlockEntityType.Builder.of(TileExchange::new, Blocks.EXCHANGE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileCollection>> COLLECTOR = TILES.register("collector", () -> BlockEntityType.Builder.of(TileCollection::new, Blocks.COLLECTOR.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileCableProcess>> PROCESS_KABEL = TILES.register("process_kabel", () -> BlockEntityType.Builder.of(TileCableProcess::new, Blocks.PROCESS_KABEL.get()).build(null));
  }

  public static class Menus {

    public static void init() {}

    public static final DeferredHolder<MenuType<?>, MenuType<ContainerNetworkCraftingTable>> REQUEST = CONTAINERS.register("request", () -> IMenuTypeExtension.create((windowId, inv, data) -> {
      return new ContainerNetworkCraftingTable(windowId, inv.player.level(), data.readBlockPos(), inv, inv.player);
    }));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerCollectionFilter>> COLLECTOR = CONTAINERS.register("collector", () -> IMenuTypeExtension.create((windowId, inv, data) -> {
      return new ContainerCollectionFilter(windowId, inv.player.level(), data.readBlockPos(), inv, inv.player);
    }));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerCableFilter>> FILTER_KABEL = CONTAINERS.register("filter_kabel", () -> IMenuTypeExtension.create((windowId, inv, data) -> {
      return new ContainerCableFilter(windowId, inv.player.level(), data.readBlockPos(), inv, inv.player);
    }));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerCableImportFilter>> IMPORT_FILTER_KABEL = CONTAINERS.register("import_filter_kabel", () -> IMenuTypeExtension.create((windowId, inv, data) -> {
      return new ContainerCableImportFilter(windowId, inv.player.level(), data.readBlockPos(), inv, inv.player);
    }));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerCableExportFilter>> EXPORT_KABEL = CONTAINERS.register("export_kabel", () -> IMenuTypeExtension.create((windowId, inv, data) -> {
      return new ContainerCableExportFilter(windowId, inv.player.level(), data.readBlockPos(), inv, inv.player);
    }));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerNetworkInventory>> INVENTORY = CONTAINERS.register("inventory", () -> IMenuTypeExtension.create((windowId, inv, data) -> {
      return new ContainerNetworkInventory(windowId, inv.player.level(), data.readBlockPos(), inv, inv.player);
    }));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerNetworkInventoryExpanded>> REQUEST_EXPANDED = CONTAINERS.register("request_expanded", () -> IMenuTypeExtension.create((windowId, inv, data) -> {
      return new ContainerNetworkInventoryExpanded(windowId, inv.player.level(), data.readBlockPos(), inv, inv.player);
    }));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerNetworkRemote>> INVENTORY_REMOTE = CONTAINERS.register("inventory_remote", () -> IMenuTypeExtension.create((windowId, inv, data) -> {
      return new ContainerNetworkRemote(windowId, inv.player.getInventory());
    }));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerNetworkCraftingRemote>> CRAFTING_REMOTE = CONTAINERS.register("crafting_remote", () -> IMenuTypeExtension.create((windowId, inv, data) -> {
      return new ContainerNetworkCraftingRemote(windowId, inv.player.getInventory());
    }));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerNetworkExpandedRemote>> EXPANDED_REMOTE = CONTAINERS.register("expanded_remote", () -> IMenuTypeExtension.create((windowId, inv, data) -> {
      return new ContainerNetworkExpandedRemote(windowId, inv.player.getInventory());
    }));
  }
}
