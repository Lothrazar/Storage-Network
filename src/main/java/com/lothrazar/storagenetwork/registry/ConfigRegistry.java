package com.lothrazar.storagenetwork.registry;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.lothrazar.library.util.StringParseUtil;
import com.lothrazar.storagenetwork.StorageNetworkMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.registries.ForgeRegistries;

public class ConfigRegistry {

  private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
  private static ForgeConfigSpec COMMON_CONFIG;
  private static BooleanValue LOGSPAM;
  private static IntValue REFRESHTICKS;
  public static IntValue EXCHANGEBUFFER;
  private static BooleanValue RELOADONCHUNK;
  private static ConfigValue<List<String>> IGNORELIST;
  public static IntValue ITEMRANGE;
  public static IntValue RECIPEMAXTAGS;
  private static ConfigValue<List<String>> CABLEIGNORELIST;
  public static BooleanValue enableFacades;
  static {
    initConfig();
  }

  public static boolean isFacadeAllowed(ItemStack item) {
    ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item.getItem());
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
        .define("NotallowedBlocks",
            list);
    EXCHANGEBUFFER = COMMON_BUILDER.comment("\r\n How many itemstacks from the network are visible to external connections through the storagenetwork:exchange.  "
        + "Too low and not all items can pass through, too large and there will be packet/buffer overflows.")
        .defineInRange("exchangeBufferSize", 1024, 1, 5000);
    ITEMRANGE = COMMON_BUILDER.comment("\r\n Maximum range of the Storage Remote and Crafting Remote.   -1 means unlimited.")
        .defineInRange("remoteMaxRange", -1, -1, Integer.MAX_VALUE / 256);
    RECIPEMAXTAGS = COMMON_BUILDER.comment("\r\n When matching items to recipes in the JEI + button, this is the maximum number of tags to serialize over the network when on a server.  Reduce if you get errors relating to Packet Sizes being too large (Minecraft 1.12.2 had this hardcoded at 5).")
        .defineInRange("jeiMaximumRecipeTags", 64, 5, 128);
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
        .define("itemsNotAllowed", list);
    enableFacades = COMMON_BUILDER.comment("Change this to 'false' to disable facades.  The facade feature lets you hide cables with blocks (does not consume the item, use shift-left-click when not creative)")
        .define("enabled", true);
    COMMON_BUILDER.pop();
    COMMON_BUILDER.pop();
    COMMON_CONFIG = COMMON_BUILDER.build();
  }

  public ConfigRegistry(Path path) {
    final CommentedFileConfig configData = CommentedFileConfig.builder(path)
        .sync()
        .autosave()
        .writingMode(WritingMode.REPLACE)
        .build();
    configData.load();
    COMMON_CONFIG.setConfig(configData);
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

  public List<String> ignorelist() {
    return IGNORELIST.get();
  }
}
