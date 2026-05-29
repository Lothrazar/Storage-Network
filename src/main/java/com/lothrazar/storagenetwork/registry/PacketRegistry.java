package com.lothrazar.storagenetwork.registry;

import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.network.*;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class PacketRegistry {

  public static void registerPayloads(RegisterPayloadHandlersEvent event) {
    PayloadRegistrar reg = event.registrar(StorageNetworkMod.MODID);
    reg.playToServer(CableDataMessage.TYPE, CableDataMessage.STREAM_CODEC, CableDataMessage::handle);
    reg.playToServer(CableIOMessage.TYPE, CableIOMessage.STREAM_CODEC, CableIOMessage::handle);
    reg.playToServer(CableProcessMessage.TYPE, CableProcessMessage.STREAM_CODEC, CableProcessMessage::handle);
    reg.playToServer(InsertMessage.TYPE, InsertMessage.STREAM_CODEC, InsertMessage::handle);
    reg.playToServer(RequestMessage.TYPE, RequestMessage.STREAM_CODEC, RequestMessage::handle);
    reg.playToServer(ClearRecipeMessage.TYPE, ClearRecipeMessage.STREAM_CODEC, ClearRecipeMessage::handle);
    reg.playToServer(SettingsSyncMessage.TYPE, SettingsSyncMessage.STREAM_CODEC, SettingsSyncMessage::handle);
    reg.playToServer(RecipeMessage.TYPE, RecipeMessage.STREAM_CODEC, RecipeMessage::handle);
    reg.playToServer(CableLimitMessage.TYPE, CableLimitMessage.STREAM_CODEC, CableLimitMessage::handle);
    reg.playToServer(CableFacadeMessage.TYPE, CableFacadeMessage.STREAM_CODEC, CableFacadeMessage::handle);
    reg.playToServer(KeybindCurioMessage.TYPE, KeybindCurioMessage.STREAM_CODEC, KeybindCurioMessage::handle);
    reg.playToServer(KeybindCollectorToggleMessage.TYPE, KeybindCollectorToggleMessage.STREAM_CODEC, KeybindCollectorToggleMessage::handle);
    reg.playToClient(StackRefreshClientMessage.TYPE, StackRefreshClientMessage.STREAM_CODEC, StackRefreshClientMessage::handle);
    reg.playToClient(StackResponseClientMessage.TYPE, StackResponseClientMessage.STREAM_CODEC, StackResponseClientMessage::handle);
    reg.playToClient(RefreshFilterClientMessage.TYPE, RefreshFilterClientMessage.STREAM_CODEC, RefreshFilterClientMessage::handle);
    reg.playToClient(SortClientMessage.TYPE, SortClientMessage.STREAM_CODEC, SortClientMessage::handle);
  }
}
