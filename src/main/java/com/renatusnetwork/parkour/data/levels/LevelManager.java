package com.renatusnetwork.parkour.data.levels;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.events.types.EventType;
import com.renatusnetwork.parkour.data.locations.LocationsYAML;
import com.renatusnetwork.parkour.data.menus.*;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.gameplay.handlers.PracticeHandler;
import com.renatusnetwork.parkour.utils.Utils;
import com.renatusnetwork.parkour.utils.dependencies.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class LevelManager {

    private HashMap<String, Level> levels = new HashMap<>();
    private HashMap<String, LevelData> levelDataCache;
    private HashMap<Menu, Set<Level>> menuLevels = new HashMap<>();
    private Level featuredLevel;
    private Level tutorialLevel;
    private long totalLevelCompletions;
    private HashMap<Integer, Level> globalLevelCompletionsLB = new HashMap<>
            (Parkour.getSettingsManager().max_global_level_completions_leaderboard_size);
    private HashMap<Integer, Level> topRatedLevelsLB = new HashMap<>
            (Parkour.getSettingsManager().max_rated_levels_leaderboard_size);

    private HashMap<String, HashMap<Integer, Level>> buyingLevels = new HashMap<>();

    private HashMap<String, LevelCooldown> cooldowns = new HashMap<>();

    public LevelManager(Plugin plugin) {
        this.levelDataCache = LevelsDB.getDataCache();

        load(); // Loads levels from configuration
        loadRatings();
        loadLevelsInMenus();
        pickFeatured();
        totalLevelCompletions = LevelsDB.getGlobalCompletions();
        tutorialLevel = get(Parkour.getSettingsManager().tutorial_level_name);
        startScheduler(plugin);
    }

    public void load() {
        levels = new HashMap<>();

        for (String levelName : LevelsYAML.getNames())
            load(levelName);

        tutorialLevel = get(Parkour.getSettingsManager().tutorial_level_name);
        Parkour.getPluginLogger().info("Levels loaded: " + levels.size());
    }

    public void loadGlobalLevelCompletions()
    {
        totalLevelCompletions = LevelsDB.getGlobalCompletions();
    }

    public void load(String levelName) {
        boolean exists = exists(levelName);

        Level level = null;

        if (!LevelsYAML.exists(levelName) && exists)
            levels.remove(levelName);
        else {
            level = new Level(levelName);
            LevelsDB.syncDataCache(level, levelDataCache);

            if (exists)
                levels.remove(levelName);

            levels.put(levelName, level);
        }

        // refresh featured here
        if (featuredLevel != null && level != null && level.getName().equalsIgnoreCase(featuredLevel.getName()))
            featuredLevel = level;

        // need to add final copy for outside inner class access
        Level finalLevel = level;
        new BukkitRunnable() {
            @Override
            public void run() {
                // loop through to update stats
                for (PlayerStats playerStats : Parkour.getStatsManager().getPlayerStats().values())
                    if (playerStats.getLevel() != null && playerStats.getLevel().getName().equalsIgnoreCase(levelName))
                        if (finalLevel != null)
                            playerStats.setLevel(finalLevel);
                        else
                            // if level was just removed from only config, then reset their level
                            playerStats.resetLevel();
            }
        }.runTaskAsynchronously(Parkour.getPlugin());
    }

    private void loadRatings()
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                for (Level level : levels.values())
                {
                    level.setRating(RatingDB.getAverageRating(level.getID()));
                    level.setRatingsCount(RatingDB.getTotalRatings(level.getID()));
                }
            }
        }.runTaskAsynchronously(Parkour.getPlugin());
    }

    private void startScheduler(Plugin plugin) {

        // update player count in levels every 60 seconds
        new BukkitRunnable() {
            @Override
            public void run() {

            // loop through levels then all players online to determine how many are in each level
            for (Level level : getLevelsInAllMenus()) {
                if (level != null) {
                    int amountInLevel = 0;
                    for (PlayerStats playerStats : Parkour.getStatsManager().getPlayerStats().values()) {

                        if (playerStats != null && playerStats.inLevel() && playerStats.getLevel().getName().equalsIgnoreCase(level.getName()))
                            amountInLevel++;
                        }
                        level.setPlayersInLevel(amountInLevel);
                    }
                }
            }
        }.runTaskTimerAsynchronously(plugin, 20 * 60, 20 * 60);

        // update global level completions and top rated completions lb every 3 minutes
        new BukkitRunnable() {
            @Override
            public void run() {
                loadGlobalLevelCompletionsLB();
                loadTopRatedLevelsLB();
            }
        }.runTaskTimerAsynchronously(Parkour.getPlugin(), 20 * 15, 20 * 180);
    }

    public void pickFeatured() {

        List<Level> temporaryList = new ArrayList<>();

        /*
           sort through all levels that are in menus
           to make sure it does not have required levels and has more than 0 coin reward
         */
        for (Level level : getLevelsInAllMenus()) {
            if (level.getRequiredLevels().isEmpty() && !level.hasPermissionNode() && !level.isAscendanceLevel() && !level.isRankUpLevel() && !level.isDropperLevel() && level.getReward() > 0 && level.getReward() < 50000)
                temporaryList.add(level);
        }

        Random ran = new Random();
        Level level = temporaryList.get(ran.nextInt(temporaryList.size()));

        if (level != null) {
            featuredLevel = level;
            Parkour.getPluginLogger().info("Featured Level: " + level.getName());
        }
    }

    /*
        this method first loops through the menus in config, then the pages, then the items, to find ALL levels that
        originate from the GUI so no featured level is picked from something like ascendance, etc
     */
    public void loadLevelsInMenus() {

        menuLevels.clear();
        for (String menuName : MenusYAML.getNames()) {
            Menu menu = Parkour.getMenuManager().getMenu(menuName);
            // null check menu
            if (menu != null) {
                Set<Level> levelsInMenu = new HashSet<>();
                boolean menuHasLevels = false;

                // then loop through each page in the menu
                for (int i = 1; i <= menu.getPageCount(); i++) {

                    MenuPage menuPage = menu.getPage(i);
                    // null check the page
                    if (menuPage != null) {
                        // finally, loop through each item in the gui
                        for (int j = 0; j < (menuPage.getRowCount() * 9); j++) {
                            MenuItem menuItem = menuPage.getMenuItem(j);

                            // null check the item
                            if (menuItem != null) {
                                // then check if the item is a level type, if so, add to list
                                if (menuItem.getType().equalsIgnoreCase("level")) {
                                    menuHasLevels = true; // enable it as a menu with levels
                                    Level menuLevel = get(menuItem.getTypeValue());

                                    // last step! null check level
                                    if (menuLevel != null)
                                        levelsInMenu.add(menuLevel);
                                }
                            }
                        }
                    }
                }
                if (menuHasLevels)
                    // add to menu map
                    menuLevels.put(menu, levelsInMenu);
            }
        }
    }

    public boolean isBuyingLevel(String playerName, Level level)
    {
        boolean result = false;

        if (buyingLevels.containsKey(playerName))
        {
            HashMap<Integer, Level> levelsSet = buyingLevels.get(playerName);
            result = levelsSet.containsValue(level);
        }

        return result;
    }

    public void addBuyingLevel(String playerName, Level level, int slot)
    {
        HashMap<Integer, Level> newMap;

        if (buyingLevels.containsKey(playerName))
        {
            newMap = buyingLevels.get(playerName);
            newMap.put(slot, level);
        }
        else
        {
            newMap = new HashMap<Integer, Level>() {{
                put(slot, level);
            }};
        }

        buyingLevels.put(playerName, newMap);
    }

    public int getTotalBuyingLevelsCost(String playerName)
    {
        HashMap<Integer, Level> levels = buyingLevels.get(playerName);
        int totalCost = 0;

        if (levels != null)
            for (Level level : levels.values())
                totalCost += level.getPrice();

        return totalCost;
    }

    public void removeBuyingLevel(String playerName)
    {
        buyingLevels.remove(playerName);
    }

    public boolean isBuyingLevelMenu(String playerName)
    {
        return buyingLevels.containsKey(playerName);
    }

    public boolean inCooldownMap(String playerName)
    {
        return cooldowns.containsKey(playerName);
    }

    public LevelCooldown getLevelCooldown(String playerName)
    {
        return cooldowns.get(playerName);
    }

    public void addLevelCooldown(String playerName, Level level)
    {
        if (level.hasCooldown())
        {
            // add to both conditions
            if (cooldowns.containsKey(playerName))
                cooldowns.get(playerName).addCompletion();
            else
                cooldowns.put(playerName, new LevelCooldown(level));
        }
    }

    public List<Integer> getBuyingLevelsSlots(String playerName)
    {
        HashMap<Integer, Level> levels = buyingLevels.get(playerName);
        List<Integer> levelsToBuy = new ArrayList<>();

        // if non null, add all from keyset
        if (levels != null)
            levels.keySet().forEach(value -> levelsToBuy.add(value));

        return levelsToBuy;
    }

    public Level getTutorialLevel() { return tutorialLevel; }

    public HashMap<Integer, Level> getBuyingLevels(String playerName)
    {
        return buyingLevels.get(playerName);
    }

    public void clearBuyingLevels(String playerName)
    {
        buyingLevels.remove(playerName);
    }

    public Set<Level> getLevelsInAllMenus() {
        Set<Level> levelsInMenus = new HashSet<>();

        // loop through then add to new hashset
        for (Set<Level> levels : menuLevels.values())
            for (Level level : levels)
                levelsInMenus.add(level);

        return levelsInMenus;
    }

    public Set<Level> getLevelsFromMenu(Menu menu) {
        if (menuLevels.containsKey(menu))
            return menuLevels.get(menu);
        return null;
    }

    public Level getFeaturedLevel() {
        return featuredLevel;
    }

    public List<Level> getEventLevels() {
        List<Level> tempList = new ArrayList<>();

        for (Level level : levels.values())
            if (level.isEventLevel())
                tempList.add(level);

        return tempList;
    }

    public List<Level> getEventLevelsFromType(EventType eventType)
    {
        List<Level> tempList = new ArrayList<>();

        for (Level level : getEventLevels())
            if (level.getEventType() == eventType)
                tempList.add(level);

        return tempList;
    }

    public List<String> getRaceLevels() {

        List<String> temporaryRaceLevelList = new ArrayList<>();

        for (Level level : levels.values()) {
            if (level.isRaceLevel())
                temporaryRaceLevelList.add(level.getName());
        }
        return temporaryRaceLevelList;
    }

    public void setLevelDataCache(HashMap<String, LevelData> levelDataCache) {
        this.levelDataCache = levelDataCache;
    }

    public HashMap<String, LevelData> getLevelDataCache() {
        return levelDataCache;
    }

    public Level get(String levelName) {
        return levels.get(levelName);
    }

    public Level get(int levelID) {
        for (Level level : levels.values())
            if (level.getID() == levelID)
                return level;

        return null;
    }

    public Level getFromTitle(String levelTitle) {
        levelTitle = ChatColor.stripColor(levelTitle);

        for (Level level : levels.values())
            if (ChatColor.stripColor(level.getFormattedTitle()).equalsIgnoreCase(levelTitle))
                return level;

        return null;
    }

    public long getTotalLevelCompletions() { return totalLevelCompletions; }

    public void addTotalLevelCompletion() { totalLevelCompletions++; }

    public void removeTotalLevelCompletion() { totalLevelCompletions--; }

    // top rated levels lb
    public void loadTopRatedLevelsLB() {
        try {

            Level highestLevel = null;
            Set<String> addedLevels = new HashSet<>();
            int lbSize = 0;
            topRatedLevelsLB.clear();
            int leaderboardPos = 1;

            while (Parkour.getSettingsManager().max_global_level_completions_leaderboard_size > lbSize) {
                for (Level level : levels.values())
                    if (level.getRatingsCount() >= 5 && (highestLevel == null || (!addedLevels.contains(level.getName()) &&
                        level.getRating() >= highestLevel.getRating())))
                        highestLevel = level;

                // add 1 and add to added levels
                lbSize++;

                if (highestLevel != null) {
                    addedLevels.add(highestLevel.getName());
                    // add to temp lb
                    if (highestLevel.getRating() > 0.00)
                    {
                        topRatedLevelsLB.put(leaderboardPos, highestLevel);
                        leaderboardPos++;
                    }

                    highestLevel = null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HashMap<Integer, Level> getTopRatedLevelsLB() { return topRatedLevelsLB; }

    public HashMap<Integer, Level> getGlobalLevelCompletionsLB() {
        return globalLevelCompletionsLB;
    }

    public void loadGlobalLevelCompletionsLB() {
        try {
            Level highestLevel = null;
            Set<String> addedLevels = new HashSet<>();
            int lbSize = 0;
            int leaderboardPos = 1;

            globalLevelCompletionsLB.clear();

            while (Parkour.getSettingsManager().max_global_level_completions_leaderboard_size > lbSize) {

                for (Level level : levels.values())
                    if (level.getTotalCompletionsCount() > 0 &&
                       (highestLevel == null ||
                       (!addedLevels.contains(level.getName()) && level.getTotalCompletionsCount() >= highestLevel.getTotalCompletionsCount())))
                        highestLevel = level;

                // null check jic
                if (highestLevel != null)
                {
                    globalLevelCompletionsLB.put(leaderboardPos, highestLevel);
                    addedLevels.add(highestLevel.getName());
                    highestLevel = null;
                    leaderboardPos++;
                }
                lbSize++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, Level> getLevels() {
        return levels;
    }

    public boolean exists(String levelName) {
        return get(levelName) != null;
    }

    public void remove(String levelName) {
        boolean foundLevel = false;

        for (String name : levels.keySet()) {
            if (name.equals(levelName)) {
                foundLevel = true;
                break;
            }
        }
        // once level found, remove from config, db and cache
        if (foundLevel) {
            Level level = get(levelName);

            LevelsYAML.remove(levelName);
            LocationsYAML.remove(levelName + "-spawn");
            LocationsYAML.remove(levelName + "-completion");
            // remove from levels, checkpoints, ratings and completions to clean up database
            Parkour.getDatabaseManager().add("DELETE FROM levels WHERE level_name='" + levelName + "'");
            Parkour.getDatabaseManager().add("DELETE FROM checkpoints WHERE level_name='" + levelName + "'");
            Parkour.getDatabaseManager().add("DELETE FROM completions WHERE level_id=" + level.getID());
            Parkour.getDatabaseManager().add("DELETE FROM ratings WHERE level_id=" + level.getID());

            // loop through and reset if applicable
            for (PlayerStats playerStats : Parkour.getStatsManager().getPlayerStats().values())
                if (playerStats.inLevel() && playerStats.getLevel().getName().equalsIgnoreCase(levelName))
                {
                    playerStats.resetLevel();
                    PracticeHandler.resetDataOnly(playerStats);
                    playerStats.resetCurrentCheckpoint();

                    // toggle off elytra armor
                    Parkour.getStatsManager().toggleOffElytra(playerStats);
                }

            levelDataCache.remove(levelName);
            levels.remove(levelName);
        }
    }

    public void teleportToLevel(PlayerStats playerStats, Level level)
    {
        Player player = playerStats.getPlayer();

        // since ascendance is a free-roam map...
        if (!level.isAscendanceLevel())
        {
            if (!level.isRankUpLevel())
            {
                if (!level.isEventLevel())
                {
                    if (!level.isRaceLevel())
                    {
                        boolean teleport = true;

                        // not all levels have a price, so do a boolean switch
                        if (level.getPrice() > 0 && !playerStats.hasBoughtLevel(level.getName()) && playerStats.getLevelCompletionsCount(level.getName()) <= 0)
                        {
                            teleport = false;
                            player.sendMessage(Utils.translate("&cYou first need to buy " + level.getFormattedTitle() + " &cbefore teleporting to it"));
                        }

                        // if still allowed, tp them!
                        if (teleport)
                            MenuItemAction.performLevelTeleport(playerStats, player, level);
                    }
                    else
                    {
                        player.sendMessage(Utils.translate("&cYou cannot teleport to a Race level"));
                    }
                }
                else
                {
                    player.sendMessage(Utils.translate("&cYou cannot teleport to an Event level"));
                }
            }
            else
            {
                player.sendMessage(Utils.translate("&cYou cannot teleport to a Rankup level"));
            }
        }
        else
        {
            player.sendMessage(Utils.translate("&cYou cannot teleport to an Ascendance level"));
        }
    }

    public void regionLevelCheck(PlayerStats playerStats, Location location)
    {
        // region null check
        ProtectedRegion region = WorldGuard.getRegion(location);
        if (region != null) {

            Level level = Parkour.getLevelManager().get(region.getId());

            // make sure the area they are spawning in is a level
            if (level != null)
            {
                playerStats.setLevel(level);

                // if elytra level, toggle on
                if (playerStats.getLevel().isElytraLevel())
                    Parkour.getStatsManager().toggleOnElytra(playerStats);

                // if they have a cp, load it
                Location checkpoint = playerStats.getCheckpoint(level.getName());
                if (checkpoint != null)
                    playerStats.setCurrentCheckpoint(checkpoint);
            }
        }
    }

    public void create(String levelName) {
        LevelsYAML.create(levelName);
    }

    public Set<String> getNames() {
        return levels.keySet();
    }

    public void shutdown() {
        for (PlayerStats playerStats : Parkour.getStatsManager().getPlayerStats().values())
            Parkour.getStatsManager().toggleOffElytra(playerStats);
    }
}