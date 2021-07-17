package com.parkourcraft.parkour.data.levels;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.events.EventType;
import com.parkourcraft.parkour.data.locations.LocationsYAML;
import com.parkourcraft.parkour.data.menus.Menu;
import com.parkourcraft.parkour.data.menus.MenuItem;
import com.parkourcraft.parkour.data.menus.MenuPage;
import com.parkourcraft.parkour.data.menus.MenusYAML;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.data.stats.StatsDB;
import com.parkourcraft.parkour.utils.Utils;
import com.sk89q.minecraft.util.commands.Link;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public class LevelManager {

    private HashMap<String, Level> levels = new HashMap<>();
    private Map<String, LevelData> levelDataCache;
    private Set<Level> levelsInMenus = new HashSet<>();
    private String featuredLevel = null;
    private long totalLevelCompletions;
    private LinkedHashSet<Level> globalLevelCompletionsLB = new LinkedHashSet<>
            (Parkour.getSettingsManager().max_global_level_completions_leaderboard_size);
    private LinkedHashSet<Level> topRatedLevelsLB = new LinkedHashSet<>
            (Parkour.getSettingsManager().max_rated_levels_leaderboard_size);

    public LevelManager(Plugin plugin) {
        this.levelDataCache = LevelsDB.getDataCache();

        load(); // Loads levels from configuration
        loadLevelsInMenus();
        pickFeatured();
        totalLevelCompletions = LevelsDB.getGlobalCompletions();
        startScheduler(plugin);
    }

    public void load() {
        levels = new HashMap<>();

        for (String levelName : LevelsYAML.getNames())
            load(levelName);

        Parkour.getPluginLogger().info("Levels loaded: " + levels.size());
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

    private void startScheduler(Plugin plugin) {
        new BukkitRunnable() {
            public void run() {
                if (LevelsDB.syncLevelData()) {
                    setLevelDataCache(LevelsDB.getDataCache());
                    LevelsDB.syncDataCache();
                }
            }
        }.runTaskTimerAsynchronously(plugin, 60 * 20, 10);

        // update player count in levels every 60 seconds
        new BukkitRunnable() {
            public void run() {

                // loop through levels then all players online to determine how many are in each level
                for (Level level : levelsInMenus) {
                    if (level != null) {
                        int amountInLevel = 0;
                        for (PlayerStats playerStats : Parkour.getStatsManager().getPlayerStats().values()) {

                            if (playerStats != null &&
                                    playerStats.getLevel() != null &&
                                    playerStats.getLevel().getName().equalsIgnoreCase(level.getName()))
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
        }.runTaskTimerAsynchronously(Parkour.getPlugin(), 20 * 10, 20 * 180);
    }

    public void pickFeatured() {

        List<Level> temporaryList = new ArrayList<>();

        /*
           sort through all levels that are in menus
           to make sure it does not have required levels and has more than 0 coin reward
         */
        for (Level level : getLevelsInMenus()) {
            if (level.getRequiredLevels().isEmpty() && level.getReward() > 0)
                temporaryList.add(level);
        }

        Random ran = new Random();
        Level level = temporaryList.get(ran.nextInt(temporaryList.size()));

        if (level != null) {
            featuredLevel = level.getName();
            // proper casting
            level.setReward((int) (level.getReward() * Parkour.getSettingsManager().featured_level_reward_multiplier));
            Parkour.getPluginLogger().info("Featured Level: " + level.getName());
        }
    }

    /*
        this method first loops through the menus in config, then the pages, then the items, to find ALL levels that
        originate from the GUI so no featured level is picked from something like ascendance, etc
     */
    public void loadLevelsInMenus() {

        // clear for each time its loaded, just in case
        levelsInMenus.clear();

        // first loop through each menu
        for (String menuName : MenusYAML.getNames()) {
            Menu menu = Parkour.getMenuManager().getMenu(menuName);
            // null check menu
            if (menu != null) {
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
                                    Level menuLevel = get(menuItem.getTypeValue());

                                    // last step! null check level
                                    if (menuLevel != null)
                                        levelsInMenus.add(menuLevel);
                                }
                            }
                        }
                    }
                }
            } else {
                Parkour.getPluginLogger().info("Null Menu on getLevelsInMenus()");
            }
        }
    }

    public Set<Level> getLevelsInMenus() {
        return levelsInMenus;
    }

    public Level getFeaturedLevel() {
        Level level = get(featuredLevel);

        if (level != null)
            return level;
        return null;
    }

    public List<Level> getEventLevels() {
        List<Level> tempList = new ArrayList<>();

        for (Level level : levels.values())
            if (level.isEventLevel())
                tempList.add(level);

        return tempList;
    }

    public List<Level> getPvPEventLevels() {
        List<Level> tempList = new ArrayList<>();

        for (Level level : getEventLevels())
            if (level.getEventType() == EventType.PVP)
                tempList.add(level);

        return tempList;
    }

    public List<Level> getRisingWaterEventLevels() {
        List<Level> tempList = new ArrayList<>();

        for (Level level : getEventLevels())
            if (level.getEventType() == EventType.RISING_WATER)
                tempList.add(level);

        return tempList;
    }

    public List<Level> getFallingAnvilEventLevels() {
        List<Level> tempList = new ArrayList<>();

        for (Level level : getEventLevels())
            if (level.getEventType() == EventType.FALLING_ANVIL)
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

    public void setLevelDataCache(Map<String, LevelData> levelDataCache) {
        this.levelDataCache = levelDataCache;
    }

    public Map<String, LevelData> getLevelDataCache() {
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
        for (Level level : levels.values())
            if (ChatColor.stripColor(level.getFormattedTitle()).equalsIgnoreCase(levelTitle))
                return level;

        return null;
    }

    public long getTotalLevelCompletions() { return totalLevelCompletions; }

    public void addTotalLevelCompletion() { totalLevelCompletions++; }

    // top rated levels lb
    public void loadTopRatedLevelsLB() {
        try {

            LinkedHashSet<Level> temporaryLB = new LinkedHashSet<>();
            Level highestLevel = null;
            Set<String> addedLevels = new HashSet<>();
            int lbSize = 0;

            while (Parkour.getSettingsManager().max_global_level_completions_leaderboard_size > lbSize) {
                for (Level level : levels.values())
                    if (highestLevel == null || (!addedLevels.contains(level.getName()) &&
                        level.getRating() > highestLevel.getRating() &&
                        level.getRatingsCount() >= 5))
                        highestLevel = level;

                // add 1 and add to added levels
                lbSize++;
                addedLevels.add(highestLevel.getName());
                // add to temp lb
                if (highestLevel.getRating() > 0.00)
                    temporaryLB.add(highestLevel);

                highestLevel = null;
            }
            // quickly swap
            topRatedLevelsLB.clear();
            topRatedLevelsLB.addAll(temporaryLB);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public LinkedHashSet<Level> getTopRatedLevelsLB() { return topRatedLevelsLB; }

    /*
        section for global LEVEL completions (level, not personal based)
    */
    public boolean isGlobalLevelCompletionsLBSpot(Level level) {
        return globalLevelCompletionsLB.contains(level);
    }

    public LinkedHashSet<Level> getGlobalLevelCompletionsLB() {
        return globalLevelCompletionsLB;
    }

    public void loadGlobalLevelCompletionsLB() {
        try {

            LinkedHashSet<Level> temporaryLB = new LinkedHashSet<>();
            Level highestLevel = null;
            Set<String> addedLevels = new HashSet<>();
            int lbSize = 0;

            while (Parkour.getSettingsManager().max_global_level_completions_leaderboard_size > lbSize) {

                for (Level level : levels.values())
                    if (highestLevel == null ||
                       (!addedLevels.contains(level.getName()) && level.getTotalCompletionsCount() > highestLevel.getTotalCompletionsCount()))
                        highestLevel = level;

                temporaryLB.add(highestLevel);
                addedLevels.add(highestLevel.getName());
                highestLevel = null;
                lbSize++;
            }
            // quickly swap
            globalLevelCompletionsLB.clear();
            globalLevelCompletionsLB.addAll(temporaryLB);
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
                if (playerStats.getLevel() != null && playerStats.getLevel().getName().equalsIgnoreCase(levelName)) {
                    playerStats.resetLevel();

                    if (playerStats.getPracticeLocation() != null)
                        playerStats.resetPracticeMode();

                    if (playerStats.getCheckpoint() != null)
                        playerStats.resetCheckpoint();

                    // toggle off elytra armor
                    Parkour.getStatsManager().toggleOffElytra(playerStats);
                }

            levels.remove(levelName);
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