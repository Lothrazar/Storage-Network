package com.lothrazar.storagenetwork.gui;

import com.lothrazar.storagenetwork.api.gui.GuiNetwork;
import com.lothrazar.storagenetwork.network.SettingsSyncMessage;
import net.neoforged.neoforge.network.PacketDistributor;

public interface DefaultGuiNetwork extends GuiNetwork {


  default void syncDataToServer() {
    PacketDistributor.sendToServer(new SettingsSyncMessage(getPos(), getDownwards(), getSort(), isJeiSearchSynced(), getAutoFocus(), isFullStackCraft()));
  }

}
