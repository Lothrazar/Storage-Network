package mrriegel.storagenetwork.jei;
public class SearchSettings {

  private static boolean jeiSearchSync = true;
  private static boolean keepSearch = true;
  private static String search = "";

  public static boolean isJeiSearchSynced() {
    return jeiSearchSync;
  }

  public static void setJeiSearchSync(boolean jeiSearch) {
    SearchSettings.jeiSearchSync = jeiSearch;
  }

  public static boolean isSearchKept() {
    return keepSearch;
  }

  public static void setKeepSearch(boolean keepSearch) {
    SearchSettings.keepSearch = keepSearch;
  }

  public static String getSearch() {
    if (JeiHooks.isJeiLoaded() && jeiSearchSync) {
      if (keepSearch)
        return JeiHooks.getFilterText();
      else
        JeiHooks.setFilterText("");
    }
    else if (keepSearch) {
      return search;
    }
    return "";
  }

  public static void setSearch(String search) {
    if (JeiHooks.isJeiLoaded() && jeiSearchSync) {
      JeiHooks.setFilterText(search);
    }
    if (keepSearch) {
      SearchSettings.search = search;
    }
  }
}
