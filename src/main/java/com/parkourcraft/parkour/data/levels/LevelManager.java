package com.parkourcraft.parkour.data.levels;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.utils.dependencies.WorldGuardUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class LevelManager {

    private List<LevelObject> levels = new ArrayList<>();
    private Map<String, LevelData> levelDataCache;
    private List<String> enabledLeaderboards = new ArrayList<>();
    private HashMap<String, String> inLevelRegions = new HashMap<>();

    public LevelManager(Plugin plugin) {
        this.levelDataCache = Levels_DB.getDataCache();

        load(); // Loads levels from configuration
        startScheduler(plugin);
    }

    public void load() {
        levels = new ArrayList<>();

        for (String levelName : Levels_YAML.getNames())
            load(levelName);

        Parkour.getPluginLogger().info("Levels loaded: " + levels.size());
    }

    public void load(String levelName) {
        boolean exists = exists(levelName);

        if (!Levels_YAML.exists(levelName)
                && exists)
            remove(levelName);
        else {
            LevelObject levelObject = new LevelObject(levelName);
            Levels_DB.syncDataCache(levelObject, levelDataCache);

            if (exists)
                remove(levelName);

            levels.add(levelObject);
        }
    }

    private void startScheduler(Plugin plugin) {
        new BukkitRunnable() {
            public void run() {
                if (Levels_DB.syncLevelData()) {
                    setLevelDataCache(Levels_DB.getDataCache());
                    Levels_DB.syncDataCache();
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

    public LevelObject get(String levelName) {
        for (LevelObject levelObject : levels)
            if (levelObject.getName().equals(levelName))
                return levelObject;

        return null;
    }

    public LevelObject get(int levelID) {
        for (LevelObject levelObject : levels)
            if (levelObject.getID() == levelID)
                return levelObject;

        return null;
    }

    public List<LevelObject> getLevels() {
        return levels;
    }

    public boolean exists(String levelName) {
        return get(levelName) != null;
    }

    public void remove(String levelName) {
        for (Iterator<LevelObject> iterator = levels.iterator(); iterator.hasNext();) {
            if (iterator.next().getName().equals(levelName)) {
                Levels_YAML.remove(iterator.getClass().getName());
                iterator.remove();
            }
        }
    }

    public void create(String levelName) {
        Levels_YAML.create(levelName);
    }

    public List<String> getNames() {
        List<String> names = new ArrayList<>();

        for (LevelObject levelObject : levels)
            names.add(levelObject.getName());

        return names;
    }

    public Map<String, String> getNamesLower() {
        Map<String, String> levelNamesLower = new HashMap<>();

        for (LevelObject level : levels)
            levelNamesLower.put(level.getName().toLowerCase(), level.getName());

        return levelNamesLower;
    }

    public List<String> getEnabledLeaderboards() {
        return enabledLeaderboards;
    }


    public HashMap<String, String> getPlayerRegionMap() {
        return inLevelRegions;
    }

    public void addToLevelMap(String playerName, String levelName) {
        inLevelRegions.put(playerName, levelName.toLowerCase());
    }

    public void removeFromLevelMap(String playerName) {
        inLevelRegions.remove(playerName);
    }
}