package mrriegel.storagenetwork.registry;

import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.api.data.EnumStorageDirection;
import mrriegel.storagenetwork.api.data.EnumUpgradeType;
import mrriegel.storagenetwork.block.cable.BlockCable;
import mrriegel.storagenetwork.block.cable.TileCable;
import mrriegel.storagenetwork.block.cable.io.BlockCableIO;
import mrriegel.storagenetwork.block.cable.io.TileCableIO;
import mrriegel.storagenetwork.block.cable.link.BlockCableLink;
import mrriegel.storagenetwork.block.cable.link.TileCableLink;
import mrriegel.storagenetwork.block.cable.linksimple.BlockCableLinkSimple;
import mrriegel.storagenetwork.block.cable.linksimple.TileCableLinkSimple;
import mrriegel.storagenetwork.block.cable.processing.BlockCableProcessing;
import mrriegel.storagenetwork.block.cable.processing.TileCableProcess;
import mrriegel.storagenetwork.block.control.BlockControl;
import mrriegel.storagenetwork.block.control.TileControl;
import mrriegel.storagenetwork.block.inventory.BlockInventory;
import mrriegel.storagenetwork.block.inventory.TileInventory;
import mrriegel.storagenetwork.block.master.BlockMaster;
import mrriegel.storagenetwork.block.master.TileMaster;
import mrriegel.storagenetwork.block.request.BlockRequest;
import mrriegel.storagenetwork.block.request.TileRequest;
import mrriegel.storagenetwork.item.ItemCollector;
import mrriegel.storagenetwork.item.ItemPicker;
import mrriegel.storagenetwork.item.ItemUpgrade;
import mrriegel.storagenetwork.item.remote.ItemRemote;
import mrriegel.storagenetwork.item.remote.RemoteType;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

public class RegistryEvents {

  @SubscribeEvent
  public void onRegistryBlock(RegistryEvent.Register<Block> event) {
    IForgeRegistry<Block> reg = event.getRegistry();
    reg.register(new BlockMaster("master"));
    reg.register(new BlockRequest("request"));
    reg.register(new BlockInventory("inventory"));
    reg.register(new BlockCable("kabel"));
    reg.register(new BlockCableLink("storage_kabel"));
    reg.register(new BlockCableIO("ex_kabel", EnumStorageDirection.OUT));
    reg.register(new BlockCableIO("im_kabel", EnumStorageDirection.IN));
    reg.register(new BlockCableProcessing("process_kabel"));
    reg.register(new BlockControl("controller"));
    reg.register(new BlockCableLinkSimple("simple_kabel"));
    GameRegistry.registerTileEntity(TileCable.class, new ResourceLocation(StorageNetwork.MODID, "tileKabel"));
    GameRegistry.registerTileEntity(TileCableLink.class, new ResourceLocation(StorageNetwork.MODID, "tileKabelLink"));
    GameRegistry.registerTileEntity(TileCableIO.class, new ResourceLocation(StorageNetwork.MODID, "tileKabelIO"));
    GameRegistry.registerTileEntity(TileCableProcess.class, new ResourceLocation(StorageNetwork.MODID, "tileKabelProcess"));
    GameRegistry.registerTileEntity(TileMaster.class, new ResourceLocation(StorageNetwork.MODID, "tileMaster"));
    GameRegistry.registerTileEntity(TileRequest.class, new ResourceLocation(StorageNetwork.MODID, "tileRequest"));
    GameRegistry.registerTileEntity(TileInventory.class, new ResourceLocation(StorageNetwork.MODID, "tileInventory"));
    GameRegistry.registerTileEntity(TileControl.class, new ResourceLocation(StorageNetwork.MODID, "tileControl"));
    GameRegistry.registerTileEntity(TileCableLinkSimple.class, new ResourceLocation(StorageNetwork.MODID, "tileSimple"));
  }

  @SubscribeEvent
  public void onRegistryEvent(RegistryEvent.Register<Item> event) {
    IForgeRegistry<Item> registry = event.getRegistry();
    registry.register(new ItemBlock(ModBlocks.master).setRegistryName(ModBlocks.master.getRegistryName()));
    registry.register(new ItemBlock(ModBlocks.request).setRegistryName(ModBlocks.request.getRegistryName()));
    registry.register(new ItemBlock(ModBlocks.inventory).setRegistryName(ModBlocks.inventory.getRegistryName()));
    registry.register(new ItemBlock(ModBlocks.kabel).setRegistryName(ModBlocks.kabel.getRegistryName()));
    registry.register(new ItemBlock(ModBlocks.simple_kabel).setRegistryName(ModBlocks.simple_kabel.getRegistryName()));
    registry.register(new ItemBlock(ModBlocks.storageKabel).setRegistryName(ModBlocks.storageKabel.getRegistryName()));
    registry.register(new ItemBlock(ModBlocks.exKabel).setRegistryName(ModBlocks.exKabel.getRegistryName()));
    registry.register(new ItemBlock(ModBlocks.imKabel).setRegistryName(ModBlocks.imKabel.getRegistryName()));
    registry.register(new ItemBlock(ModBlocks.processKabel).setRegistryName(ModBlocks.processKabel.getRegistryName()));
    registry.register(new ItemBlock(ModBlocks.controller).setRegistryName(ModBlocks.controller.getRegistryName()));
    registry.register(new ItemUpgrade());
    registry.register(new ItemRemote());
    registry.register(new ItemPicker());
    registry.register(new ItemCollector());
  }

  @SubscribeEvent
  public void registerModels(ModelRegistryEvent event) {
    ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.kabel), 0, new ModelResourceLocation(StorageNetwork.MODID + ":kabel", "inventory"));
    ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.simple_kabel), 0, new ModelResourceLocation(StorageNetwork.MODID + ":simple_kabel", "inventory"));
    ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.exKabel), 0, new ModelResourceLocation(StorageNetwork.MODID + ":ex_kabel", "inventory"));
    ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.storageKabel), 0, new ModelResourceLocation(StorageNetwork.MODID + ":storage_kabel", "inventory"));
    ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.imKabel), 0, new ModelResourceLocation(StorageNetwork.MODID + ":im_kabel", "inventory"));
    ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.processKabel), 0, new ModelResourceLocation(StorageNetwork.MODID + ":process_kabel", "inventory"));
    ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.master), 0, new ModelResourceLocation(StorageNetwork.MODID + ":master", "inventory"));
    ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.request), 0, new ModelResourceLocation(StorageNetwork.MODID + ":request", "inventory"));
    ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.controller), 0, new ModelResourceLocation(StorageNetwork.MODID + ":controller", "inventory"));
    ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.inventory), 0, new ModelResourceLocation(StorageNetwork.MODID + ":inventory", "inventory"));
    ModelLoader.setCustomModelResourceLocation(ModItems.collector_remote, 0, new ModelResourceLocation(StorageNetwork.MODID + ":collector_remote", "inventory"));
    ModelLoader.setCustomModelResourceLocation(ModItems.picker_remote, 0, new ModelResourceLocation(StorageNetwork.MODID + ":picker_remote", "inventory"));
    for (EnumUpgradeType type : EnumUpgradeType.values()) {
      ModelLoader.setCustomModelResourceLocation(ModItems.upgrade, type.ordinal(), new ModelResourceLocation(StorageNetwork.MODID + ":upgrade_" + type.ordinal(), "inventory"));
    }
    for (RemoteType type : RemoteType.values()) {
      ModelLoader.setCustomModelResourceLocation(ModItems.remote, type.ordinal(), new ModelResourceLocation(StorageNetwork.MODID + ":remote_" + type.ordinal(), "inventory"));
    }
  }
}
