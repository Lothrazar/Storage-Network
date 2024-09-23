package mrriegel.storagenetwork.client;

import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.item.remote.ItemRemote;
import mrriegel.storagenetwork.network.OpenRemoteMessage;
import mrriegel.storagenetwork.proxy.ClientProxy;
import mrriegel.storagenetwork.registry.PacketRegistry;
import net.minecraft.client.Minecraft;
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
    if (ClientProxy.KEY_OPEN_REMOTE != null && ClientProxy.KEY_OPEN_REMOTE.isPressed()) {
      StorageNetwork.log("open remote key");
      Minecraft client = Minecraft.getMinecraft();
      EntityPlayer player = client.player;
      // check slots for first remote in inventory and try to open it
      // if the player is holding multiple remotes, too bad it just grabs the first one
      for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
        ItemStack slot = player.inventory.getStackInSlot(i);
        // if i have one bound and one unbound remote, the unbound has no tag so check for that
        if (slot.getItem() instanceof ItemRemote && slot.hasTagCompound()) {
          // the server needs to know that the player has a remote open
          PacketRegistry.INSTANCE.sendToServer(new OpenRemoteMessage());
          break;
        }
      }
    }
  }
}
