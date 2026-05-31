package com.lothrazar.storagenetwork.registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.lothrazar.library.util.StringParseUtil;
import com.lothrazar.storagenetwork.StorageNetworkMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public class ConfigRegistry {

  private static final ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();
  public static ModConfigSpec COMMON_CONFIG;
  private static BooleanValue LOGSPAM;
  private static IntValue REFRESHTICKS;
  public static IntValue EXCHANGEBUFFER;
  private static BooleanValue RELOADONCHUNK;
  private static ConfigValue<List<? extends String>> IGNORELIST;
  public static IntValue ITEMRANGE;
  public static IntValue RECIPEMAXTAGS;
  public static IntValue IO_DEFAULT_SPEED;
  public static IntValue DRAWER_POLL_INTERVAL;
  public static IntValue CHUNKLOADER_REFRESH_TICKS;
  private static ConfigValue<List<? extends String>> CABLEIGNORELIST;
  public static BooleanValue enableFacades;
  static {
    initConfig();
  }

  public static boolean isFacadeAllowed(ItemStack item) {
    ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item.getItem());
    if (StringParseUtil.isInList(CABLEIGNORELIST.get(), itemId)) {
      return false;
    }
    return true;
  }

  private static void initConfig() {
    COMMON_BUILDER.comment("General settings").push(StorageNetworkMod.MODID);
    LOGSPAM = COMMON_BUILDER.comment("Enable very spammy logs.  Sometimes useful for debugging. ").define("logSpam", false);
    RELOADONCHUNK = COMMON_BUILDER.comment(
        "\r\n If this is true, reload network when a chunk unloads, this keeps your network always up to date.  It has been reported that this cause lag and chunk load issues on servers, "
            + "so disable if you have any problems. ")
        .define("reloadNetworkWhenUnloadChunk", false);
    REFRESHTICKS = COMMON_BUILDER.comment("\r\n How often to auto-refresh a network (one second is 20 ticks)").defineInRange("autoRefreshTicks", 20, 2, 4096);
    List<String> list = new ArrayList<String>();
    list.add("extrautils2:playerchest");
    IGNORELIST = COMMON_BUILDER.comment("\r\n Disable these blocks from ever being able to connect to the network, they will be treated as a non-inventory.")
        .defineListAllowEmpty("NotallowedBlocks", list, () -> "", o -> o instanceof String);
    EXCHANGEBUFFER = COMMON_BUILDER.comment("\r\n How many itemstacks from the network are visible to external connections through the storagenetwork:exchange.  "
        + "Too low and not all items can pass through, too large and there will be packet/buffer overflows.")
        .defineInRange("exchangeBufferSize", 1024, 1, 5000);
    ITEMRANGE = COMMON_BUILDER.comment("\r\n Maximum range of the Storage Remote and Crafting Remote.   -1 means unlimited.")
        .defineInRange("remoteMaxRange", -1, -1, Integer.MAX_VALUE / 256);
    RECIPEMAXTAGS = COMMON_BUILDER.comment("\r\n When matching items to recipes in the JEI + button, this is the maximum number of tags to serialize over the network when on a server.  Reduce if you get errors relating to Packet Sizes being too large (Minecraft 1.12.2 had this hardcoded at 5).")
        .defineInRange("jeiMaximumRecipeTags", 64, 5, 128);
    IO_DEFAULT_SPEED = COMMON_BUILDER.comment("\r\n Base tick interval between import/export cable operations with no upgrades.  Speed upgrades divide this value, slow upgrades multiply it.  Lower = faster cables but more server load.")
        .defineInRange("ioDefaultSpeed", 30, 1, 4096);
    DRAWER_POLL_INTERVAL = COMMON_BUILDER.comment("\r\n How often (in ticks) each Network Drawer polls the master for its cached item count.  Lower = more responsive displays but more server load with many drawers.")
        .defineInRange("drawerPollInterval", 10, 1, 4096);
    CHUNKLOADER_REFRESH_TICKS = COMMON_BUILDER.comment("\r\n How often (in ticks) cables and receivers refresh their chunk-load tickets and registration housekeeping.  Mostly relevant for forced-chunk / cross-dimension setups.")
        .defineInRange("chunkLoaderRefreshTicks", 20, 1, 4096);
    //
    COMMON_BUILDER.push("facades");
    list = Arrays.asList("minecraft:ladder", "minecraft:double_plant", "minecraft:waterlily",
        "minecraft:torch", "minecraft:*_torch", "minecraft:redstone", "minecraft:iron_bars",
        "minecraft:chest", "minecraft:ender_chest", "minecraft:sculk_vein", "minecraft:string", "minecraft:vine",
        "minecraft:rail",
        "minecraft:*_rail",
        "minecraft:brewing_stand",
        "minecraft:*_dripleaf",
        "minecraft:*_pane",
        "minecraft:*_sapling", "minecraft:*_sign",
        "minecraft:*_door",
        "minecraft:*_banner", "minecraft:*_shulker_box",
        "cyclic:*_pipe", "cyclic:*_bars",
        "storagenetwork:*");
    CABLEIGNORELIST = COMMON_BUILDER.comment("\r\n These items are not able to be used as Facade blocks for cables (shift-left-click to add or remove block facades while in not-creative)")
        .defineListAllowEmpty("itemsNotAllowed", list, () -> "", o -> o instanceof String);
    enableFacades = COMMON_BUILDER.comment("Change this to 'false' to disable facades.  The facade feature lets you hide cables with blocks (does not consume the item, use shift-left-click when not creative)")
        .define("enabled", true);
    COMMON_BUILDER.pop();
    COMMON_BUILDER.pop();
    COMMON_CONFIG = COMMON_BUILDER.build();
  }

  public ConfigRegistry() {
  }

  public boolean logspam() {
    return LOGSPAM.get();
  }

  public boolean doReloadOnChunk() {
    return RELOADONCHUNK.get();
  }

  public int refreshTicks() {
    return REFRESHTICKS.get();
  }

  public List<? extends String> ignorelist() {
    return IGNORELIST.get();
  }


  public static boolean isTargetAllowed(BlockState state) {
    if (state.getBlock() == Blocks.AIR) {
      return false;
    }
    String blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
    for (String s : StorageNetworkMod.CONFIG.ignorelist()) {
      if (blockId.equals(s)) {
        return false;
      }
    }
    return true;
  }
}
