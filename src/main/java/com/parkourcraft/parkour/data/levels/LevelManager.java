package com.parkourcraft.parkour.data.levels;

import com.parkourcraft.parkour.Parkour;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class LevelManager {

    private List<LevelObject> levels = new ArrayList<>();
    private Map<String, LevelData> levelDataCache;
    private String featuredLevel = null;

    public LevelManager(Plugin plugin) {
        this.levelDataCache = Levels_DB.getDataCache();

        load(); // Loads levels from configuration
        pickFeatured();
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

    public void pickFeatured() {

        List<LevelObject> temporaryList = new ArrayList<>();

        // sort through all levels to make sure it does not have required levels and has more than 0 coin reward
        for (LevelObject level : levels) {
            if (level.getRequiredLevels().isEmpty() && level.getReward() > 0)
                temporaryList.add(level);
        }

        Random ran = new Random();
        LevelObject level = temporaryList.get(ran.nextInt(temporaryList.size()));

        if (level != null) {
            featuredLevel = level.getName();
            Parkour.getPluginLogger().info("Featured Level: " + level.getName());
        }
    }

    public LevelObject getFeaturedLevel() {
        LevelObject levelObject = Parkour.getLevelManager().get(featuredLevel);

        if (levelObject != null)
            return levelObject;
        return null;
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

    public List<String> getRaceLevels() {

        List<String> temporaryRaceLevelList = new ArrayList<>();

        for (LevelObject levelObject : levels) {
            if (levelObject.isRaceLevel())
                temporaryRaceLevelList.add(levelObject.getName());
        }
        return temporaryRaceLevelList;
    }
}
