package mrriegel.storagenetwork;

import mrriegel.storagenetwork.item.remote.ItemRemote;
import mrriegel.storagenetwork.network.OpenRemoteMessage;
import mrriegel.storagenetwork.proxy.ClientProxy;
import mrriegel.storagenetwork.registry.PacketRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@EventBusSubscriber(modid = StorageNetwork.MODID)
public class KeyInputHandler {

  @SideOnly(Side.CLIENT)
  @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
  public static void onEvent(KeyInputEvent event) {
    // check if the remote keybind is pressed
    KeyBinding[] keyBindings = ClientProxy.keyBindings;
    if (keyBindings[0].isPressed()) {
      StorageNetwork.log("open remote key");
      Minecraft client = Minecraft.getMinecraft();
      EntityPlayer player = client.player;
      // check slots for first remote in inventory and try to open it
      for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
        ItemStack slot = player.inventory.getStackInSlot(i);
        if (slot.getItem() instanceof ItemRemote) {
          ItemRemote.tryOpenGui(client.world, player, slot, i);
          // the server needs to know that the player has a remote open too
          // I sure wonder what would happen if there was a desync here and the server can't find a remote
          PacketRegistry.INSTANCE.sendToServer(new OpenRemoteMessage());
          break;
        }
      }
    }
  }
}
