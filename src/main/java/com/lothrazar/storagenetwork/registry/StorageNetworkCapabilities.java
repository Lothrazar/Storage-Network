package com.lothrazar.storagenetwork.registry;


import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.api.network.ConnectableNode;
import com.lothrazar.storagenetwork.api.capabilities.CapabilityImportExport;
import com.lothrazar.storagenetwork.api.capabilities.CapabilityProcessing;
import com.lothrazar.storagenetwork.api.capabilities.CapabilityConnectable;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class StorageNetworkCapabilities {

  public static final BlockCapability<ConnectableNode,  Direction> CONNECTABLE =
      BlockCapability.createSided(ResourceLocation.fromNamespaceAndPath(StorageNetworkMod.MODID, "connectable"), ConnectableNode.class);

  public static final BlockCapability<CapabilityConnectable, Direction> CONNECTABLE_ITEM_STORAGE =
      BlockCapability.createSided(ResourceLocation.fromNamespaceAndPath(StorageNetworkMod.MODID, "connectable_item_storage"), CapabilityConnectable.class);

  public static final BlockCapability<CapabilityImportExport, Direction> CONNECTABLE_AUTO_IO =
      BlockCapability.createSided(ResourceLocation.fromNamespaceAndPath(StorageNetworkMod.MODID, "connectable_auto_io"), CapabilityImportExport.class);

  public static final BlockCapability<CapabilityProcessing, Direction> PROCESSING =
      BlockCapability.createSided(ResourceLocation.fromNamespaceAndPath(StorageNetworkMod.MODID, "connectable_processing"), CapabilityProcessing.class);

  @SubscribeEvent
  public static void registerCapabilities(RegisterCapabilitiesEvent event) {
    // CONNECTABLE - all network tile entities provide this (master is the hub, not a node)
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
    event.registerBlockEntity(CONNECTABLE, SsnRegistry.Tiles.STORAGE_CRADLE.get(), (be, side) -> be.getConnectable());

    // CONNECTABLE_ITEM_STORAGE - storage cable and filter cable
    event.registerBlockEntity(CONNECTABLE_ITEM_STORAGE, SsnRegistry.Tiles.STORAGE_KABEL.get(), (be, side) -> be.getItemStorage());
    event.registerBlockEntity(CONNECTABLE_ITEM_STORAGE, SsnRegistry.Tiles.FILTER_KABEL.get(), (be, side) -> be.getCapabilityLink());
    event.registerBlockEntity(CONNECTABLE_ITEM_STORAGE, SsnRegistry.Tiles.STORAGE_CRADLE.get(), (be, side) -> be.getLinkCapability());

    // CONNECTABLE_AUTO_IO - import and export cables
    event.registerBlockEntity(CONNECTABLE_AUTO_IO, SsnRegistry.Tiles.IMPORT_KABEL.get(), (be, side) -> be.getIoStorage());
    event.registerBlockEntity(CONNECTABLE_AUTO_IO, SsnRegistry.Tiles.IMPORT_FILTER_KABEL.get(), (be, side) -> be.getIoStorage());
    event.registerBlockEntity(CONNECTABLE_AUTO_IO, SsnRegistry.Tiles.EXPORT_KABEL.get(), (be, side) -> be.getIoStorage());

    // PROCESSING - processing cable
    event.registerBlockEntity(PROCESSING, SsnRegistry.Tiles.PROCESS_KABEL.get(), (be, side) -> be.getItemStorage());

    // ITEM_HANDLER - exchange and collector blocks proxy item access into the network
    event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, SsnRegistry.Tiles.EXCHANGE.get(), (be, side) -> be.getItemHandler());
    event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, SsnRegistry.Tiles.COLLECTOR.get(), (be, side) -> be.getItemHandler());
  }
}
