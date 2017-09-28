package com.parkourcraft.Parkour.data;

import com.parkourcraft.Parkour.Parkour;
import com.parkourcraft.Parkour.data.levels.LevelObject;
import com.parkourcraft.Parkour.storage.mysql.DatabaseManager;
import com.parkourcraft.Parkour.storage.mysql.DatabaseQueries;
import com.parkourcraft.Parkour.data.levels.Levels_YAML;

import java.util.*;

public class LevelManager {

    private static List<LevelObject> levels = new ArrayList<>();

    public static LevelObject get(String levelName) {
        for (LevelObject levelObject : levels)
            if (levelObject.getName().equals(levelName))
                return levelObject;

        return null;
    }

    public static LevelObject get(int levelID) {
        for (LevelObject levelObject : levels)
            if (levelObject.getID() == levelID)
                return levelObject;

        return null;
    }

    public static List<LevelObject> getLevels() {
        return levels;
    }

    public static boolean exists(String levelName) {
        if (get(levelName) != null)
            return true;

        return false;
    }

    public static void remove(String levelName) {
        for (Iterator<LevelObject> iterator = levels.iterator(); iterator.hasNext();)
            if (iterator.next().getName().equals(levelName))
                iterator.remove();
    }

    public static void load(String levelName) {
        boolean exists = exists(levelName);

        if (!Levels_YAML.exists(levelName)
                && exists)
            remove(levelName);
        else {
            LevelObject levelObject = new LevelObject(levelName);

            if (exists)
                remove(levelName);

            levels.add(levelObject);
        }
    }

    public static void loadAll() {
        levels = new ArrayList<>();

        for (String levelName : Levels_YAML.getNames())
            load(levelName);

        Parkour.getPluginLogger().info("Levels loaded: " + levels.size());
    }

    public static void create(String levelName) {
        Levels_YAML.create(levelName);
    }

    public static List<String> getNames() {
        List<String> names = new ArrayList<>();

        for (LevelObject levelObject : levels)
            names.add(levelObject.getName());

        return names;
    }

    public static Map<String, String> getNamesLower() {
        Map<String, String> levelNamesLower = new HashMap<>();

        for (String levelName : getNames())
            levelNamesLower.put(levelName.toLowerCase(), levelName);

        return levelNamesLower;
    }

}
