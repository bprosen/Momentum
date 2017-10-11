package com.parkourcraft.Parkour.data.levels;

import com.parkourcraft.Parkour.Parkour;
import com.parkourcraft.Parkour.data.levels.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class LevelManager {

    private List<LevelObject> levels = new ArrayList<>();
    private Map<String, LevelData> levelDataCache;

    public LevelManager(Plugin plugin) {
        load(); // Loads levels from configuration

        levelDataCache = Levels_DB.getDataCache();
        Levels_DB.syncDataCache();

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
            Levels_DB.syncData(levelObject, levelDataCache);

            if (exists)
                remove(levelName);

            levels.add(levelObject);
        }
    }

    private void startScheduler(Plugin plugin) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
            public void run() {
                if (Levels_DB.syncAllData()) {
                    levelDataCache = Levels_DB.getDataCache();
                    Levels_DB.syncDataCache();
                }
            }
        }, 0L, 4L);
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

        for (String levelName : getNames())
            levelNamesLower.put(levelName.toLowerCase(), levelName);

        return levelNamesLower;
    }

}
