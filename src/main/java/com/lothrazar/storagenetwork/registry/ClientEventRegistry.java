package com.lothrazar.storagenetwork.registry;

import org.lwjgl.glfw.GLFW;
import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.block.facade.TesrCable;
import com.lothrazar.storagenetwork.block.facade.TesrExportCable;
import com.lothrazar.storagenetwork.block.facade.TesrImportCable;
import com.lothrazar.storagenetwork.block.facade.TesrImportFilterCable;
import com.lothrazar.storagenetwork.block.facade.TesrStorageCable;
import com.lothrazar.storagenetwork.registry.SsnRegistry.Tiles;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEventRegistry {

  public static final ResourceLocation SLOT = new ResourceLocation(StorageNetworkMod.MODID, "textures/gui/slot.png");
  public static final KeyMapping INVENTORY_KEY = new KeyMapping("key.storagenetwork.remote", KeyConflictContext.UNIVERSAL, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_I, "key.categories.inventory");

  @SubscribeEvent
  public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
    event.registerBlockEntityRenderer(Tiles.KABEL.get(), TesrCable::new);
    event.registerBlockEntityRenderer(Tiles.EXPORT_KABEL.get(), TesrExportCable::new);
    event.registerBlockEntityRenderer(Tiles.STORAGE_KABEL.get(), TesrStorageCable::new);
    event.registerBlockEntityRenderer(Tiles.IMPORT_FILTER_KABEL.get(), TesrImportFilterCable::new);
    event.registerBlockEntityRenderer(Tiles.IMPORT_KABEL.get(), TesrImportCable::new);
  }
}
