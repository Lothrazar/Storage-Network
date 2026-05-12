package com.lothrazar.storagenetwork.registry;

import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.network.*;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public class PacketRegistry {

  public static void registerPayloads(RegisterPayloadHandlersEvent event) {
    var reg = event.registrar(StorageNetworkMod.MODID);
    reg.serverbound(CableDataMessage.TYPE, CableDataMessage.STREAM_CODEC, CableDataMessage::handle);
    reg.serverbound(CableIOMessage.TYPE, CableIOMessage.STREAM_CODEC, CableIOMessage::handle);
    reg.serverbound(InsertMessage.TYPE, InsertMessage.STREAM_CODEC, InsertMessage::handle);
    reg.serverbound(RequestMessage.TYPE, RequestMessage.STREAM_CODEC, RequestMessage::handle);
    reg.serverbound(ClearRecipeMessage.TYPE, ClearRecipeMessage.STREAM_CODEC, ClearRecipeMessage::handle);
    reg.serverbound(SettingsSyncMessage.TYPE, SettingsSyncMessage.STREAM_CODEC, SettingsSyncMessage::handle);
    reg.serverbound(RecipeMessage.TYPE, RecipeMessage.STREAM_CODEC, RecipeMessage::handle);
    reg.serverbound(CableLimitMessage.TYPE, CableLimitMessage.STREAM_CODEC, CableLimitMessage::handle);
    reg.serverbound(CableFacadeMessage.TYPE, CableFacadeMessage.STREAM_CODEC, CableFacadeMessage::handle);
    reg.serverbound(KeybindCurioMessage.TYPE, KeybindCurioMessage.STREAM_CODEC, KeybindCurioMessage::handle);
    reg.serverbound(KeybindCollectorToggleMessage.TYPE, KeybindCollectorToggleMessage.STREAM_CODEC, KeybindCollectorToggleMessage::handle);
    reg.clientbound(StackRefreshClientMessage.TYPE, StackRefreshClientMessage.STREAM_CODEC, StackRefreshClientMessage::handle);
    reg.clientbound(StackResponseClientMessage.TYPE, StackResponseClientMessage.STREAM_CODEC, StackResponseClientMessage::handle);
    reg.clientbound(RefreshFilterClientMessage.TYPE, RefreshFilterClientMessage.STREAM_CODEC, RefreshFilterClientMessage::handle);
    reg.clientbound(SortClientMessage.TYPE, SortClientMessage.STREAM_CODEC, SortClientMessage::handle);
  }
}
