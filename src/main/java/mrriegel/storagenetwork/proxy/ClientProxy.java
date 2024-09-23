package mrriegel.storagenetwork.proxy;

import org.lwjgl.input.Keyboard;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.block.cable.TesrCable;
import mrriegel.storagenetwork.block.cable.TileCable;
import mrriegel.storagenetwork.registry.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

  public static KeyBinding KEY_OPEN_REMOTE;

  @Override
  public EntityPlayer getClientPlayer() {
    return Minecraft.getMinecraft().player;
  }

  @Override
  public void preInit(FMLPreInitializationEvent event) {
    super.preInit(event);
    ClientRegistry.bindTileEntitySpecialRenderer(TileCable.class, new TesrCable());
  }

  @Override
  public void init(FMLInitializationEvent event) {
    super.init(event);
    initCableRendering();
    initKeyBindings();
  }

  private void initKeyBindings() {
    KEY_OPEN_REMOTE = new KeyBinding("key.remote.desc", Keyboard.KEY_R, "key.storagenetwork.category");
    KeyBinding[] keyBindings = new KeyBinding[1];
    keyBindings[0] = KEY_OPEN_REMOTE;
    for (KeyBinding binding : keyBindings) {
      ClientRegistry.registerKeyBinding(binding);
    }
  }

  private void initCableRendering() {
    TesrCable.addCableRender(ModBlocks.kabel, new ResourceLocation(StorageNetwork.MODID, "textures/tile/link.png"));
    TesrCable.addCableRender(ModBlocks.exKabel, new ResourceLocation(StorageNetwork.MODID, "textures/tile/ex.png"));
    TesrCable.addCableRender(ModBlocks.imKabel, new ResourceLocation(StorageNetwork.MODID, "textures/tile/im.png"));
    TesrCable.addCableRender(ModBlocks.storageKabel, new ResourceLocation(StorageNetwork.MODID, "textures/tile/storage.png"));
    TesrCable.addCableRender(ModBlocks.processKabel, new ResourceLocation(StorageNetwork.MODID, "textures/tile/process.png"));
    TesrCable.addCableRender(ModBlocks.simple_kabel, new ResourceLocation(StorageNetwork.MODID, "textures/tile/simple.png"));
  }
}
