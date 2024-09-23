package mrriegel.storagenetwork.config;

import java.io.File;
import mrriegel.storagenetwork.block.master.TileMaster;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;

public class ConfigHandler {

  public static Configuration config;
  public static int rangeWirelessAccessor;
  public static long refreshTicks;
  public static boolean allowFastWorkBenchIntegration;
  public static boolean logEverything;
  public static boolean reloadNetworkWhenUnloadChunk;
  private static String[] cableBlacklist;

  public static boolean isFacadeAllowed(ItemStack item) {
    String itemId = item.getItem().getRegistryName().toString();
    for (String s : cableBlacklist) {
      if (itemId.equals(s)) {
        return false;
      }
    }
    return true;
  }

  public static void refreshConfig(File file) {
    config = new Configuration(file);
    config.load();
    syncConfig();
    if (config.hasChanged()) {
      config.save();
    }
  }

  private static void syncConfig() {
    //    default 200 ticks aka 10 seconds
    String category = Configuration.CATEGORY_GENERAL;
    refreshTicks = config.getInt("AutoRefreshTicks", category, 200, 1, 10000, "How often to auto-refresh a network (one second is 20 ticks)");
    rangeWirelessAccessor = config.getInt("StorageRemoteRange", category, 128, 1, 10000, "How far the Remote item can reach (non-advanced)");
    allowFastWorkBenchIntegration = config.getBoolean("allowFastWorkBenchIntegration", category, true, "Allow 'fastworkbench' project to integrate into storage network crafting grids.  Turning off lets you disable integration without uninstalling mod.  Client and server should match for best outcome.");
    ConfigHandler.logEverything = config.getBoolean("LogSpamAllTheThings", category, false, "Log lots of events, some with systemtime benchmarking. WARNING: VERY SPAMMY. Only use when debugging lag or other issues.");
    reloadNetworkWhenUnloadChunk = config.getBoolean("ReloadNetworkWhenUnloadChunk", category, true, "If this is true, reload network when a chunk unloads, this keeps your network always up to date.  It has been reported that this cause lag and chunk load issues on servers, so disable if you have any problems. ");
    TileMaster.blacklist = config.getStringList("BlacklistBlocks", category, new String[] {
        "extrautils2:playerchest"
    }, "Disable these blocks from ever being able to connect to the network, they will be treated as a non-inventory.");
    // just a few to get it started. other flowers are kinda weird but leave that up to uesr
    cableBlacklist = config.getStringList("BlacklistFacadeCableItems", category, new String[] {
        "minecraft:double_plant", "minecraft:ladder", "minecraft:rail", "minecraft:detector_rail", "minecraft:activator_rail", "minecraft:golden_rail",
        "minecraft:waterlily"
    }, "Disable these blocks from being used as a facade for a cable block (sneak-left-click feature). Note this is the ID of the item held by the player when setting the facade, not the block ID");
  }
}
