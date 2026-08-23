package com.lothrazar.storagenetwork.registry;

import org.lwjgl.glfw.GLFW;
import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.block.cable.CableFacadeRenderer;
import com.lothrazar.storagenetwork.block.cradle.CradleRenderer;
import com.lothrazar.storagenetwork.block.drawer.DrawerRenderer;
import com.lothrazar.storagenetwork.registry.SsnRegistry.Tiles;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

public class ClientEventRegistry {

  public static final ResourceLocation SLOT = ResourceLocation.fromNamespaceAndPath(StorageNetworkMod.MODID, "textures/gui/slot.png");
  public static final KeyMapping INVENTORY_KEY = new KeyMapping("key.storagenetwork.remote", KeyConflictContext.UNIVERSAL, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_I, "key.categories.inventory");
  public static final KeyMapping COLLECTOR_TOGGLE_KEY = new KeyMapping("key.storagenetwork.collector_toggle", KeyConflictContext.UNIVERSAL, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_DELETE, "key.categories.inventory");

  @SubscribeEvent
  public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
    event.registerBlockEntityRenderer(Tiles.KABEL.get(), CableFacadeRenderer::new);
    event.registerBlockEntityRenderer(Tiles.EXPORT_KABEL.get(), CableFacadeRenderer::new);
    event.registerBlockEntityRenderer(Tiles.STORAGE_KABEL.get(), CableFacadeRenderer::new);
    event.registerBlockEntityRenderer(Tiles.IMPORT_FILTER_KABEL.get(), CableFacadeRenderer::new);
    event.registerBlockEntityRenderer(Tiles.IMPORT_KABEL.get(), CableFacadeRenderer::new);
    event.registerBlockEntityRenderer(Tiles.FILTER_KABEL.get(), CableFacadeRenderer::new);
    event.registerBlockEntityRenderer(Tiles.PROCESS_KABEL.get(), CableFacadeRenderer::new);
    event.registerBlockEntityRenderer(Tiles.CRADLE.get(), CradleRenderer::new);
    event.registerBlockEntityRenderer(Tiles.DRAWER.get(), DrawerRenderer::new);
  }
}
