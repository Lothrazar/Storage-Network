package mrriegel.storagenetwork.proxy;

import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.capabilities.StorageNetworkCapabilities;
import mrriegel.storagenetwork.config.ConfigHandler;
import mrriegel.storagenetwork.gui.GuiHandler;
import mrriegel.storagenetwork.jei.JeiHooks;
import mrriegel.storagenetwork.registry.PacketRegistry;
import mrriegel.storagenetwork.util.UtilTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

@Mod.EventBusSubscriber
public class CommonProxy {

  public EntityPlayer getClientPlayer() {
    return null;
  }

  public void preInit(FMLPreInitializationEvent event) {
    StorageNetworkCapabilities.initCapabilities();
    ConfigHandler.refreshConfig(event.getSuggestedConfigurationFile());
    JeiHooks.setJeiLoaded(Loader.isModLoaded("jei"));
    PacketRegistry.init();
  }

  public void init(FMLInitializationEvent event) {
    NetworkRegistry.INSTANCE.registerGuiHandler(StorageNetwork.instance, new GuiHandler());
  }

  public void postInit(FMLPostInitializationEvent event) {
    UtilTileEntity.init();
  }
}
