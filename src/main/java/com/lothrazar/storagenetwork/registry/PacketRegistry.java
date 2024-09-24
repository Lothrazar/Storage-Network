package com.lothrazar.storagenetwork.registry;

import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.network.CableDataMessage;
import com.lothrazar.storagenetwork.network.CableFacadeMessage;
import com.lothrazar.storagenetwork.network.CableIOMessage;
import com.lothrazar.storagenetwork.network.CableLimitMessage;
import com.lothrazar.storagenetwork.network.ClearRecipeMessage;
import com.lothrazar.storagenetwork.network.InsertMessage;
import com.lothrazar.storagenetwork.network.KeybindCurioMessage;
import com.lothrazar.storagenetwork.network.RecipeMessage;
import com.lothrazar.storagenetwork.network.RefreshFilterClientMessage;
import com.lothrazar.storagenetwork.network.RequestMessage;
import com.lothrazar.storagenetwork.network.SettingsSyncMessage;
import com.lothrazar.storagenetwork.network.SortClientMessage;
import com.lothrazar.storagenetwork.network.StackRefreshClientMessage;
import com.lothrazar.storagenetwork.network.StackResponseClientMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketRegistry {

  private static final String PROTOCOL_VERSION = Integer.toString(1);
  //??https://wiki.mcjty.eu/modding/index.php?title=Tut14_Ep10 
  public static final SimpleChannel INSTANCE = NetworkRegistry.ChannelBuilder
      .named(new ResourceLocation(StorageNetworkMod.MODID, "main_channel"))
      .clientAcceptedVersions(PROTOCOL_VERSION::equals)
      .serverAcceptedVersions(PROTOCOL_VERSION::equals)
      .networkProtocolVersion(() -> PROTOCOL_VERSION)
      .simpleChannel();

  public static void init() {
    //https://gist.github.com/williewillus/353c872bcf1a6ace9921189f6100d09a
    int id = 0;
    INSTANCE.registerMessage(id++, CableDataMessage.class, CableDataMessage::encode, CableDataMessage::decode, CableDataMessage::handle);
    INSTANCE.registerMessage(id++, CableIOMessage.class, CableIOMessage::encode, CableIOMessage::decode, CableIOMessage::handle);
    INSTANCE.registerMessage(id++, StackRefreshClientMessage.class, StackRefreshClientMessage::encode, StackRefreshClientMessage::decode, StackRefreshClientMessage::handle);
    INSTANCE.registerMessage(id++, InsertMessage.class, InsertMessage::encode, InsertMessage::decode, InsertMessage::handle);
    INSTANCE.registerMessage(id++, RequestMessage.class, RequestMessage::encode, RequestMessage::decode, RequestMessage::handle);
    INSTANCE.registerMessage(id++, ClearRecipeMessage.class, ClearRecipeMessage::encode, ClearRecipeMessage::decode, ClearRecipeMessage::handle);
    INSTANCE.registerMessage(id++, SettingsSyncMessage.class, SettingsSyncMessage::encode, SettingsSyncMessage::decode, SettingsSyncMessage::handle);
    INSTANCE.registerMessage(id++, RecipeMessage.class, RecipeMessage::encode, RecipeMessage::decode, RecipeMessage::handle);
    id++; //    INSTANCE.registerMessage(id++, CableFilterMessage.class, CableFilterMessage::encode, CableFilterMessage::decode, CableFilterMessage::handle);
    INSTANCE.registerMessage(id++, CableLimitMessage.class, CableLimitMessage::encode, CableLimitMessage::decode, CableLimitMessage::handle);
    INSTANCE.registerMessage(id++, StackResponseClientMessage.class, StackResponseClientMessage::encode, StackResponseClientMessage::decode, StackResponseClientMessage::handle);
    INSTANCE.registerMessage(id++, RefreshFilterClientMessage.class, RefreshFilterClientMessage::encode, RefreshFilterClientMessage::decode, RefreshFilterClientMessage::handle);
    INSTANCE.registerMessage(id++, SortClientMessage.class, SortClientMessage::encode, SortClientMessage::decode, SortClientMessage::handle);
    INSTANCE.registerMessage(id++, KeybindCurioMessage.class, KeybindCurioMessage::encode, KeybindCurioMessage::decode, KeybindCurioMessage::handle);
    INSTANCE.registerMessage(id++, CableFacadeMessage.class, CableFacadeMessage::encode, CableFacadeMessage::decode, CableFacadeMessage::handle);
  }
}
