package com.lothrazar.storagenetwork.registry;


import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.api.IConnectable;
import com.lothrazar.storagenetwork.api.IConnectableItemAutoIO;
import com.lothrazar.storagenetwork.api.IConnectableItemProcessing;
import com.lothrazar.storagenetwork.api.IConnectableLink;
import com.lothrazar.storagenetwork.block.cable.export.TileCableExport;
import com.lothrazar.storagenetwork.block.cable.input.TileCableIO;
import com.lothrazar.storagenetwork.block.cable.inputfilter.TileCableImportFilter;
import com.lothrazar.storagenetwork.block.cable.link.TileCableLink;
import com.lothrazar.storagenetwork.block.cable.linkfilter.TileCableFilter;
import com.lothrazar.storagenetwork.block.cable.processing.TileCableProcess;
import com.lothrazar.storagenetwork.block.cable.TileCable;
import com.lothrazar.storagenetwork.block.collection.TileCollection;
import com.lothrazar.storagenetwork.block.exchange.TileExchange;
import com.lothrazar.storagenetwork.block.expand.TileInventoryExpanded;
import com.lothrazar.storagenetwork.block.inventory.TileInventory;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.block.request.TileRequest;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class StorageNetworkCapabilities {

  public static final BlockCapability<IConnectable,  Direction> CONNECTABLE =
      BlockCapability.createSided(ResourceLocation.fromNamespaceAndPath(StorageNetworkMod.MODID, "connectable"), IConnectable.class);

  public static final BlockCapability<IConnectableLink, Direction> CONNECTABLE_ITEM_STORAGE =
      BlockCapability.createSided(ResourceLocation.fromNamespaceAndPath(StorageNetworkMod.MODID, "connectable_item_storage"), IConnectableLink.class);

  public static final BlockCapability<IConnectableItemAutoIO, Direction> CONNECTABLE_AUTO_IO =
      BlockCapability.createSided(ResourceLocation.fromNamespaceAndPath(StorageNetworkMod.MODID, "connectable_auto_io"), IConnectableItemAutoIO.class);

  public static final BlockCapability<IConnectableItemProcessing, Direction> PROCESSING =
      BlockCapability.createSided(ResourceLocation.fromNamespaceAndPath(StorageNetworkMod.MODID, "connectable_processing"), IConnectableItemProcessing.class);

  @SubscribeEvent
  public static void registerCapabilities(RegisterCapabilitiesEvent event) {
    // CONNECTABLE — all network tile entities provide this (master is the hub, not a node)
    event.registerBlockEntity(CONNECTABLE, SsnRegistry.Tiles.INVENTORY.get(), (be, side) -> be.getConnectable());
    event.registerBlockEntity(CONNECTABLE, SsnRegistry.Tiles.REQUEST_EXPANDED.get(), (be, side) -> be.getConnectable());
    event.registerBlockEntity(CONNECTABLE, SsnRegistry.Tiles.REQUEST.get(), (be, side) -> be.getConnectable());
    event.registerBlockEntity(CONNECTABLE, SsnRegistry.Tiles.KABEL.get(), (be, side) -> be.getConnectable());
    event.registerBlockEntity(CONNECTABLE, SsnRegistry.Tiles.STORAGE_KABEL.get(), (be, side) -> be.getConnectable());
    event.registerBlockEntity(CONNECTABLE, SsnRegistry.Tiles.IMPORT_KABEL.get(), (be, side) -> be.getConnectable());
    event.registerBlockEntity(CONNECTABLE, SsnRegistry.Tiles.IMPORT_FILTER_KABEL.get(), (be, side) -> be.getConnectable());
    event.registerBlockEntity(CONNECTABLE, SsnRegistry.Tiles.FILTER_KABEL.get(), (be, side) -> be.getConnectable());
    event.registerBlockEntity(CONNECTABLE, SsnRegistry.Tiles.EXPORT_KABEL.get(), (be, side) -> be.getConnectable());
    event.registerBlockEntity(CONNECTABLE, SsnRegistry.Tiles.EXCHANGE.get(), (be, side) -> be.getConnectable());
    event.registerBlockEntity(CONNECTABLE, SsnRegistry.Tiles.COLLECTOR.get(), (be, side) -> be.getConnectable());
    event.registerBlockEntity(CONNECTABLE, SsnRegistry.Tiles.PROCESS_KABEL.get(), (be, side) -> be.getConnectable());

    // CONNECTABLE_ITEM_STORAGE — storage cable and filter cable
    event.registerBlockEntity(CONNECTABLE_ITEM_STORAGE, SsnRegistry.Tiles.STORAGE_KABEL.get(), (be, side) -> be.getItemStorage());
    event.registerBlockEntity(CONNECTABLE_ITEM_STORAGE, SsnRegistry.Tiles.FILTER_KABEL.get(), (be, side) -> be.getCapabilityLink());

    // CONNECTABLE_AUTO_IO — import and export cables
    event.registerBlockEntity(CONNECTABLE_AUTO_IO, SsnRegistry.Tiles.IMPORT_KABEL.get(), (be, side) -> be.getIoStorage());
    event.registerBlockEntity(CONNECTABLE_AUTO_IO, SsnRegistry.Tiles.IMPORT_FILTER_KABEL.get(), (be, side) -> be.getIoStorage());
    event.registerBlockEntity(CONNECTABLE_AUTO_IO, SsnRegistry.Tiles.EXPORT_KABEL.get(), (be, side) -> be.getIoStorage());

    // PROCESSING — processing cable
    event.registerBlockEntity(PROCESSING, SsnRegistry.Tiles.PROCESS_KABEL.get(), (be, side) -> be.getItemStorage());

    // ITEM_HANDLER — exchange and collector blocks proxy item access into the network
    event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, SsnRegistry.Tiles.EXCHANGE.get(), (be, side) -> be.getItemHandler());
    event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, SsnRegistry.Tiles.COLLECTOR.get(), (be, side) -> be.getItemHandler());
  }
}
