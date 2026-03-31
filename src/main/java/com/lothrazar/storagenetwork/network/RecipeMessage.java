package com.lothrazar.storagenetwork.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.capability.handler.ItemStackMatcher;
import com.lothrazar.storagenetwork.gui.ContainerNetwork;
import com.lothrazar.storagenetwork.registry.PacketRegistry;
import com.lothrazar.storagenetwork.util.UtilInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class RecipeMessage {

  /** @formatter:off
   * Sample data structure can have list of items for each slot (example: ore dictionary)
   * {
   *  s0:[{id:"ic2:ingot",Count:1b,Damage:2s},{id:"immersiveengineering:metal",Count:1b,Damage:0s}],
   *  s1:[{id:"ic2:ingot",Count:1b,Damage:2s},{id:"immersiveengineering:metal",Count:1b,Damage:0s}],
   *  s2:[{id:"ic2:ingot",Count:1b,Damage:2s},{id:"immersiveengineering:metal",Count:1b,Damage:0s}],
   *  s3:[{id:"ic2:ingot",Count:1b,Damage:2s},{id:"immersiveengineering:metal",Count:1b,Damage:0s}],
   *  s4:[{id:"ic2:ingot",Count:1b,Damage:2s},{id:"immersiveengineering:metal",Count:1b,Damage:0s}],
   *  s5:[{id:"ic2:ingot",Count:1b,Damage:2s},{id:"immersiveengineering:metal",Count:1b,Damage:0s}],
   *  s6:[{id:"ic2:ingot",Count:1b,Damage:2s},{id:"immersiveengineering:metal",Count:1b,Damage:0s}],
   *  s7:[{id:"ic2:ingot",Count:1b,Damage:2s},{id:"immersiveengineering:metal",Count:1b,Damage:0s}],
   *  s8:[{id:"ic2:ingot",Count:1b,Damage:2s},{id:"immersiveengineering:metal",Count:1b,Damage:0s}]
   *  }
   * @formatter:on
   */
  private CompoundTag nbt;
  private int index = 0;

  private RecipeMessage() {}

  public RecipeMessage(CompoundTag nbt) {
    this.nbt = nbt;
  }

  public static RecipeMessage decode(FriendlyByteBuf buf) {
    RecipeMessage message = new RecipeMessage();
    message.index = buf.readInt();
    message.nbt = buf.readNbt();
    return message;
  }

  public static void encode(RecipeMessage msg, FriendlyByteBuf buf) {
    buf.writeInt(msg.index);
    buf.writeNbt(msg.nbt);
  }

  public static void handle(RecipeMessage message, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer player = ctx.get().getSender();
      if (player.containerMenu instanceof ContainerNetwork == false) {
        return;
      }
      ContainerNetwork ctr = (ContainerNetwork) player.containerMenu;
      TileMain main = ctr.getTileMain();
      if (main == null) {
        StorageNetworkMod.log("Recipe message cancelled, null tile " + ctr);
        return;
      }
      ClearRecipeMessage.clearContainerRecipe(player, false);
      CraftingContainer craftMatrix = ctr.getCraftMatrix();
      for (int slot = 0; slot < 9; slot++) {
        Map<Integer, ItemStack> map = new HashMap<>();
        //if its a string, then ore dict is allowed
        /*********
         * parse nbt of the slot, whether its ore dict, itemstack, ore empty
         **********/
        boolean isOreDict;
        isOreDict = false;
        ListTag invList = message.nbt.getList("s" + slot, Tag.TAG_COMPOUND);
        for (int i = 0; i < invList.size(); i++) {
          CompoundTag stackTag = invList.getCompound(i);
          ItemStack s = ItemStack.of(stackTag);
          map.put(i, s);
        }
        /********* end parse nbt of this current slot ******/
        /********** now start trying to fill in recipe **/
        for (int i = 0; i < map.size(); i++) {
          ItemStack stackCurrent = map.get(i);
          if (stackCurrent == null || stackCurrent.isEmpty()) {
            continue;
          }
          ItemStackMatcher itemStackMatcher = new ItemStackMatcher(stackCurrent);
          itemStackMatcher.setNbt(true);
          itemStackMatcher.setOre(isOreDict);
          ItemStack ex = UtilInventory.extractItem(new PlayerMainInvWrapper(player.getInventory()), itemStackMatcher, 1, true);
          /*********** First try and use the players inventory **/
          if (ex != null && !ex.isEmpty() && craftMatrix.getItem(slot).isEmpty()) {
            UtilInventory.extractItem(new PlayerMainInvWrapper(player.getInventory()), itemStackMatcher, 1, false);
            //make sure to add the real item after the nonsimulated withdrawl is complete https://github.com/PrinceOfAmber/Storage-Network/issues/16
            craftMatrix.setItem(slot, ex);
            break;
          }
          /********* now find it from the network ***/
          stackCurrent = main.request(!stackCurrent.isEmpty() ? itemStackMatcher : null, 1, false);
          if (!stackCurrent.isEmpty() && craftMatrix.getItem(slot).isEmpty()) {
            craftMatrix.setItem(slot, stackCurrent);
            break;
          }
        }
        /************** finished recipe population **/
        //        }
        //now make sure client sync happens.
        ctr.slotChanged();
        final int THRESH = 256;
        List<ItemStack> list = main.getNetwork().getSortedStacksUpTo(THRESH);
        PacketRegistry.INSTANCE.sendTo(new StackRefreshClientMessage(list, new ArrayList<>()),
            player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
      } //end run
    });
    ctx.get().setPacketHandled(true);
  }
}
