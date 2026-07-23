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
import com.lothrazar.storagenetwork.block.cable.processing.ContainerCableProcess;
import com.lothrazar.storagenetwork.block.cable.processing.TileCableProcess;
import com.lothrazar.storagenetwork.block.collection.BlockCollection;
import com.lothrazar.storagenetwork.block.collection.ContainerCollectionFilter;
import com.lothrazar.storagenetwork.block.collection.TileCollection;
import com.lothrazar.storagenetwork.block.cradle.BlockCradle;
import com.lothrazar.storagenetwork.block.cradle.ContainerCradle;
import com.lothrazar.storagenetwork.block.cradle.TileCradle;
import com.lothrazar.storagenetwork.block.drawer.BlockDrawer;
import com.lothrazar.storagenetwork.block.drawer.TileDrawer;
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
import com.lothrazar.storagenetwork.block.receiver.BlockNetworkReceiver;
import com.lothrazar.storagenetwork.block.receiver.ContainerNetworkReceiver;
import com.lothrazar.storagenetwork.block.receiver.TileNetworkReceiver;
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
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
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
import java.util.function.UnaryOperator;

public class SsnRegistry {

  public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(StorageNetworkMod.MODID);
  public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(StorageNetworkMod.MODID);
  public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, StorageNetworkMod.MODID);
  public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(Registries.MENU, StorageNetworkMod.MODID);

  public static class Blocks {

    public static void init() {}

    public static final DeferredBlock<Block> REQUEST = BLOCKS.register("request", id -> new BlockRequest(id));
    public static final DeferredBlock<Block> KABEL = BLOCKS.register("kabel", id -> new BlockCable(id));
    public static final DeferredBlock<Block> MASTER = BLOCKS.register("master", id -> new BlockMain(id));
    public static final DeferredBlock<Block> STORAGE_KABEL = BLOCKS.register("storage_kabel", id -> new BlockCableLink(id));
    public static final DeferredBlock<Block> IMPORT_KABEL = BLOCKS.register("import_kabel", id -> new BlockCableIO(id));
    public static final DeferredBlock<Block> IMPORT_FILTER_KABEL = BLOCKS.register("import_filter_kabel", id -> new BlockCableImportFilter(id));
    public static final DeferredBlock<Block> FILTER_KABEL = BLOCKS.register("filter_kabel", id -> new BlockCableFilter(id));
    public static final DeferredBlock<Block> EXPORT_KABEL = BLOCKS.register("export_kabel", id -> new BlockCableExport(id));
    public static final DeferredBlock<Block> PROCESS_KABEL = BLOCKS.register("process_kabel", id -> new BlockCableProcess(id));
    public static final DeferredBlock<Block> INVENTORY = BLOCKS.register("inventory", id -> new BlockInventory(id));
    public static final DeferredBlock<Block> REQUEST_EXPANDED = BLOCKS.register("request_expanded", id -> new BlockInventoryExpanded(id));
    public static final DeferredBlock<Block> EXCHANGE = BLOCKS.register("exchange", id -> new BlockExchange(id));
    public static final DeferredBlock<Block> COLLECTOR = BLOCKS.register("collector", id -> new BlockCollection(id));
    public static final DeferredBlock<Block> CRADLE = BLOCKS.register("cradle", id -> new BlockCradle(id));
    public static final DeferredBlock<Block> RECEIVER = BLOCKS.register("receiver", id -> new BlockNetworkReceiver(id));
    public static final DeferredBlock<Block> DRAWER = BLOCKS.register("drawer", id -> new BlockDrawer(id));
  }

  public static class Items {

    public static void init() {}

    public static final DeferredItem<BlockItem> REQUEST = ITEMS.registerSimpleBlockItem("request", Blocks.REQUEST, UnaryOperator.identity());
    public static final DeferredItem<BlockItem> KABEL = ITEMS.registerSimpleBlockItem("kabel", Blocks.KABEL, UnaryOperator.identity());
    public static final DeferredItem<BlockItem> INVENTORY = ITEMS.registerSimpleBlockItem("inventory", Blocks.INVENTORY, UnaryOperator.identity());
    public static final DeferredItem<BlockItem> REQUEST_EXPANDED = ITEMS.registerSimpleBlockItem("request_expanded", Blocks.REQUEST_EXPANDED, UnaryOperator.identity());
    public static final DeferredItem<BlockItem> MAS = ITEMS.registerSimpleBlockItem("master", Blocks.MASTER, UnaryOperator.identity());
    public static final DeferredItem<BlockItem> SK = ITEMS.registerSimpleBlockItem("storage_kabel", Blocks.STORAGE_KABEL, UnaryOperator.identity());
    public static final DeferredItem<BlockItem> IK = ITEMS.registerSimpleBlockItem("import_kabel", Blocks.IMPORT_KABEL, UnaryOperator.identity());
    public static final DeferredItem<BlockItem> IFK = ITEMS.registerSimpleBlockItem("import_filter_kabel", Blocks.IMPORT_FILTER_KABEL, UnaryOperator.identity());
    public static final DeferredItem<BlockItem> FK = ITEMS.registerSimpleBlockItem("filter_kabel", Blocks.FILTER_KABEL, UnaryOperator.identity());
    public static final DeferredItem<BlockItem> EK = ITEMS.registerSimpleBlockItem("export_kabel", Blocks.EXPORT_KABEL, UnaryOperator.identity());
    public static final DeferredItem<BlockItem> PK = ITEMS.registerSimpleBlockItem("process_kabel", Blocks.PROCESS_KABEL, UnaryOperator.identity());
    public static final DeferredItem<BlockItem> EXCHANGE = ITEMS.registerSimpleBlockItem("exchange", Blocks.EXCHANGE, UnaryOperator.identity());
    public static final DeferredItem<BlockItem> COL = ITEMS.registerSimpleBlockItem("collector", Blocks.COLLECTOR, UnaryOperator.identity());
    public static final DeferredItem<BlockItem> CRADLE = ITEMS.registerSimpleBlockItem("cradle", Blocks.CRADLE, UnaryOperator.identity());
    public static final DeferredItem<BlockItem> DRAWER = ITEMS.registerSimpleBlockItem("drawer", Blocks.DRAWER, UnaryOperator.identity());
    public static final DeferredItem<BlockItem> RECEIVER = ITEMS.registerSimpleBlockItem("receiver", Blocks.RECEIVER, UnaryOperator.identity());
    public static final DeferredItem<ItemUpgrade> STACK_UPGRADE = ITEMS.register("stack_upgrade", id -> new ItemUpgrade(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<ItemUpgrade> SPEED_UPGRADE = ITEMS.register("speed_upgrade", id -> new ItemUpgrade(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<ItemUpgrade> SLOW_UPGRADE = ITEMS.register("slow_upgrade", id -> new ItemUpgrade(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<ItemUpgrade> STOCK_UPGRADE = ITEMS.register("stock_upgrade", id -> new ItemUpgrade(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<ItemUpgrade> OP_U = ITEMS.register("operation_upgrade", id -> new ItemUpgrade(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<ItemUpgrade> SINGLE_UPGRADE = ITEMS.register("single_upgrade", id -> new ItemUpgrade(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<ItemUpgrade> VOID_UPGRADE = ITEMS.register("void_upgrade", id -> new ItemUpgrade(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<ItemUpgrade> CHUNKLOAD_UPGRADE = ITEMS.register("chunkload_upgrade", id -> new ItemUpgrade(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<ItemRemote> INVENTORY_REMOTE = ITEMS.register("inventory_remote", id -> new ItemRemote(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<ItemRemote> CRAFTING_REMOTE = ITEMS.register("crafting_remote", id -> new ItemRemote(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<Item> PICKER_REMOTE = ITEMS.register("picker_remote", id -> new ItemPicker(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<ItemCollector> COLLECTOR_REMOTE = ITEMS.register("collector_remote", id -> new ItemCollector(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<Item> BUILDER_REMOTE = ITEMS.register("builder_remote", id -> new ItemBuilder(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    public static final DeferredItem<ItemRemote> EXPANDED_REMOTE = ITEMS.register("expanded_remote", id -> new ItemRemote(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
  }

  public static class Tiles {

    public static void init() {}

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileMain>> MASTER = TILES.register("master", () -> new BlockEntityType<>(TileMain::new, Blocks.MASTER.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileInventory>> INVENTORY = TILES.register("inventory", () -> new BlockEntityType<>(TileInventory::new, Blocks.INVENTORY.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileInventoryExpanded>> REQUEST_EXPANDED = TILES.register("request_expanded", () -> new BlockEntityType<>(TileInventoryExpanded::new, Blocks.REQUEST_EXPANDED.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileRequest>> REQUEST = TILES.register("request", () -> new BlockEntityType<>(TileRequest::new, Blocks.REQUEST.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileCable>> KABEL = TILES.register("kabel", () -> new BlockEntityType<>(TileCable::new, Blocks.KABEL.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileCableLink>> STORAGE_KABEL = TILES.register("storage_kabel", () -> new BlockEntityType<>(TileCableLink::new, Blocks.STORAGE_KABEL.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileCableIO>> IMPORT_KABEL = TILES.register("import_kabel", () -> new BlockEntityType<>(TileCableIO::new, Blocks.IMPORT_KABEL.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileCableImportFilter>> IMPORT_FILTER_KABEL = TILES.register("import_filter_kabel", () -> new BlockEntityType<>(TileCableImportFilter::new, Blocks.IMPORT_FILTER_KABEL.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileCableFilter>> FILTER_KABEL = TILES.register("filter_kabel", () -> new BlockEntityType<>(TileCableFilter::new, Blocks.FILTER_KABEL.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileCableExport>> EXPORT_KABEL = TILES.register("export_kabel", () -> new BlockEntityType<>(TileCableExport::new, Blocks.EXPORT_KABEL.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileExchange>> EXCHANGE = TILES.register("exchange", () -> new BlockEntityType<>(TileExchange::new, Blocks.EXCHANGE.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileCollection>> COLLECTOR = TILES.register("collector", () -> new BlockEntityType<>(TileCollection::new, Blocks.COLLECTOR.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileCableProcess>> PROCESS_KABEL = TILES.register("process_kabel", () -> new BlockEntityType<>(TileCableProcess::new, Blocks.PROCESS_KABEL.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileCradle>> CRADLE = TILES.register("cradle", () -> new BlockEntityType<>(TileCradle::new, Blocks.CRADLE.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileNetworkReceiver>> RECEIVER = TILES.register("receiver", () -> new BlockEntityType<>(TileNetworkReceiver::new, Blocks.RECEIVER.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileDrawer>> DRAWER = TILES.register("drawer", () -> new BlockEntityType<>(TileDrawer::new, Blocks.DRAWER.get()));
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
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerCableProcess>> PROCESS_KABEL = CONTAINERS.register("process_kabel", () -> IMenuTypeExtension.create((windowId, inv, data) -> {
      return new ContainerCableProcess(windowId, inv.player.level(), data.readBlockPos(), inv, inv.player);
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
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerCradle>> CRADLE = CONTAINERS.register("cradle", () -> IMenuTypeExtension.create((windowId, inv, data) -> {
      return new ContainerCradle(windowId, inv.player.level(), data.readBlockPos(), inv, inv.player);
    }));
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerNetworkReceiver>> RECEIVER = CONTAINERS.register("receiver", () -> IMenuTypeExtension.create((windowId, inv, data) -> {
      return new ContainerNetworkReceiver(windowId, inv.player.level(), data.readBlockPos(), inv, inv.player);
    }));
  }
}
