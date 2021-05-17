package com.parkourcraft.parkour.data.levels;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.events.EventType;
import com.parkourcraft.parkour.data.menus.Menu;
import com.parkourcraft.parkour.data.menus.MenuItem;
import com.parkourcraft.parkour.data.menus.MenuPage;
import com.parkourcraft.parkour.data.menus.MenusYAML;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class LevelManager {

    private Set<Level> levels = new HashSet<>();
    private Map<String, LevelData> levelDataCache;
    private String featuredLevel = null;

    public LevelManager(Plugin plugin) {
        this.levelDataCache = LevelsDB.getDataCache();

        load(); // Loads levels from configuration
        pickFeatured();
        startScheduler(plugin);
    }

    public void load() {
        levels = new HashSet<>();

        for (String levelName : LevelsYAML.getNames())
            load(levelName);

        Parkour.getPluginLogger().info("Levels loaded: " + levels.size());
    }

    public void load(String levelName) {
        boolean exists = exists(levelName);

        if (!LevelsYAML.exists(levelName) && exists)
            remove(levelName);
        else {
            Level level = new Level(levelName);
            LevelsDB.syncDataCache(level, levelDataCache);

            if (exists)
                remove(levelName);

            levels.add(level);
        }
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
    public Set<Level> getLevelsInMenus() {

        Set<Level> temporaryList = new HashSet<>();

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
                                        temporaryList.add(menuLevel);
                                }
                            }
                        }
                    }
                }
            } else {
                Parkour.getPluginLogger().info("Null Menu on getLevelsInMenus()");
            }
        }
        return temporaryList;
    }

    public Level getFeaturedLevel() {
        Level level = Parkour.getLevelManager().get(featuredLevel);

        if (level != null)
            return level;
        return null;
    }

    public List<Level> getEventLevels() {
        List<Level> tempList = new ArrayList<>();

        for (Level level : levels)
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

    public List<Level> getHalfHeartEventLevels() {
        List<Level> tempList = new ArrayList<>();

        for (Level level : getEventLevels())
            if (level.getEventType() == EventType.HALF_HEART)
                tempList.add(level);

        return tempList;
    }

    public List<String> getRaceLevels() {

        List<String> temporaryRaceLevelList = new ArrayList<>();

        for (Level level : levels) {
            if (level.isRaceLevel())
                temporaryRaceLevelList.add(level.getName());
        }
        return temporaryRaceLevelList;
    }

    private void startScheduler(Plugin plugin) {
        new BukkitRunnable() {
            public void run() {
                if (LevelsDB.syncLevelData()) {
                    setLevelDataCache(LevelsDB.getDataCache());
                    LevelsDB.syncDataCache();
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0, 10);
    }

    void setLevelDataCache(Map<String, LevelData> levelDataCache) {
        this.levelDataCache = levelDataCache;
    }

    Map<String, LevelData> getLevelDataCache() {
        return levelDataCache;
    }

    public Level get(String levelName) {
        for (Level level : levels)
            if (level.getName().equals(levelName))
                return level;

        return null;
    }

    public Level get(int levelID) {
        for (Level level : levels)
            if (level.getID() == levelID)
                return level;

        return null;
    }

    public Set<Level> getLevels() {
        return levels;
    }

    public boolean exists(String levelName) {
        return get(levelName) != null;
    }

    public void remove(String levelName) {
        for (Iterator<Level> iterator = levels.iterator(); iterator.hasNext();) {
            if (iterator.next().getName().equals(levelName)) {
                LevelsYAML.remove(iterator.getClass().getName());
                iterator.remove();
            }
        }
    }

    public void create(String levelName) {
        LevelsYAML.create(levelName);
    }

    public Set<String> getNames() {
        Set<String> names = new HashSet<>();

        for (Level level : levels)
            names.add(level.getName());

        return names;
    }

    public Map<String, String> getNamesLower() {
        Map<String, String> levelNamesLower = new HashMap<>();

        for (Level level : levels)
            levelNamesLower.put(level.getName().toLowerCase(), level.getName());

        return levelNamesLower;
    }
}
