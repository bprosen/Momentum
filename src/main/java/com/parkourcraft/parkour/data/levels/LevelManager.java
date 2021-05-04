package com.parkourcraft.parkour.data.levels;

import com.parkourcraft.parkour.Parkour;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class LevelManager {

    private List<Level> levels = new ArrayList<>();
    private Map<String, LevelData> levelDataCache;
    private String featuredLevel = null;

    public LevelManager(Plugin plugin) {
        this.levelDataCache = LevelsDB.getDataCache();

        load(); // Loads levels from configuration
        pickFeatured();
        startScheduler(plugin);
    }

    public void load() {
        levels = new ArrayList<>();

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

        // sort through all levels to make sure it does not have required levels and has more than 0 coin reward
        for (Level level : levels) {
            if (level.getRequiredLevels().isEmpty() && level.getReward() > 0)
                temporaryList.add(level);
        }

        Random ran = new Random();
        Level level = temporaryList.get(ran.nextInt(temporaryList.size()));

        if (level != null) {
            featuredLevel = level.getName();
            Parkour.getPluginLogger().info("Featured Level: " + level.getName());
        }
    }

    public Level getFeaturedLevel() {
        Level level = Parkour.getLevelManager().get(featuredLevel);

        if (level != null)
            return level;
        return null;
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

    public List<Level> getLevels() {
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

    public List<String> getNames() {
        List<String> names = new ArrayList<>();

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

    public List<String> getRaceLevels() {

        List<String> temporaryRaceLevelList = new ArrayList<>();

        for (Level level : levels) {
            if (level.isRaceLevel())
                temporaryRaceLevelList.add(level.getName());
        }
        return temporaryRaceLevelList;
    }
}
