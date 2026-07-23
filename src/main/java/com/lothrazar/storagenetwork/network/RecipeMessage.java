package com.lothrazar.storagenetwork.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.lothrazar.storagenetwork.StorageNetworkMod;
import com.lothrazar.storagenetwork.block.main.TileMain;
import com.lothrazar.storagenetwork.api.capabilities.ItemStackMatcherDefault;
import com.lothrazar.storagenetwork.gui.ContainerNetwork;
import com.lothrazar.storagenetwork.api.util.UtilInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.wrapper.PlayerMainInvWrapper;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class RecipeMessage implements CustomPacketPayload {

  public static final CustomPacketPayload.Type<RecipeMessage> TYPE =
      new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(StorageNetworkMod.MODID, "recipe"));

  public static final StreamCodec<RegistryFriendlyByteBuf, RecipeMessage> STREAM_CODEC = StreamCodec.of(
      RecipeMessage::write,
      RecipeMessage::read
  );

  private final CompoundTag nbt;
  private final int index;

  public RecipeMessage(CompoundTag nbt) {
    this.nbt = nbt;
    this.index = 0;
  }

  private RecipeMessage(CompoundTag nbt, int index) {
    this.nbt = nbt;
    this.index = index;
  }

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  private static void write(RegistryFriendlyByteBuf buf, RecipeMessage msg) {
    buf.writeInt(msg.index);
    buf.writeNbt(msg.nbt);
  }

  private static RecipeMessage read(RegistryFriendlyByteBuf buf) {
    int index = buf.readInt();
    CompoundTag nbt = buf.readNbt();
    return new RecipeMessage(nbt != null ? nbt : new CompoundTag(), index);
  }

  public static void handle(RecipeMessage message, IPayloadContext ctx) {
    ctx.enqueueWork(() -> {
      ServerPlayer player = (ServerPlayer) ctx.player();
      if (player.containerMenu instanceof ContainerNetwork == false) {
        return;
      }
      ContainerNetwork ctr = (ContainerNetwork) player.containerMenu;
      TileMain main = ctr.getTileMain();
      if (main == null) {
        StorageNetworkMod.LOGGER.debug("Recipe message cancelled, null tile " + ctr);
        return;
      }
      ClearRecipeMessage.clearContainerRecipe(player, false);
      CraftingContainer craftMatrix = ctr.getCraftMatrix();
      for (int slot = 0; slot < 9; slot++) {
        Map<Integer, ItemStack> map = new HashMap<>();
        boolean isOreDict = false;
        ListTag invList = message.nbt.getListOrEmpty("s" + slot);
        for (int i = 0; i < invList.size(); i++) {
          CompoundTag stackTag = invList.getCompoundOrEmpty(i);
          ItemStack s = ItemStack.CODEC.parse(player.registryAccess().createSerializationContext(NbtOps.INSTANCE), stackTag).result().orElse(ItemStack.EMPTY);
          map.put(i, s);
        }
        for (int i = 0; i < map.size(); i++) {
          ItemStack stackCurrent = map.get(i);
          if (stackCurrent == null || stackCurrent.isEmpty()) {
            continue;
          }
          ItemStackMatcherDefault itemStackMatcher = new ItemStackMatcherDefault(stackCurrent);
          itemStackMatcher.setNbt(true);
          itemStackMatcher.setOre(isOreDict);
          ItemStack ex = UtilInventory.extractItem(new PlayerMainInvWrapper(player.getInventory()), itemStackMatcher, 1, true);
          if (ex != null && !ex.isEmpty() && craftMatrix.getItem(slot).isEmpty()) {
            UtilInventory.extractItem(new PlayerMainInvWrapper(player.getInventory()), itemStackMatcher, 1, false);
            craftMatrix.setItem(slot, ex);
            break;
          }
          stackCurrent = main.request(!stackCurrent.isEmpty() ? itemStackMatcher : null, 1, false);
          if (!stackCurrent.isEmpty() && craftMatrix.getItem(slot).isEmpty()) {
            craftMatrix.setItem(slot, stackCurrent);
            break;
          }
        }
        ctr.slotChanged();
        List<ItemStack> list = main.getNetwork().getStacks();
        PacketDistributor.sendToPlayer(player, new StackRefreshClientMessage(list, new ArrayList<>()));
      }
    });
  }
}
