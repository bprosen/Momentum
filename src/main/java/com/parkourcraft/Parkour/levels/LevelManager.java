package com.parkourcraft.Parkour.levels;


import com.parkourcraft.Parkour.utils.storage.Levels_YAML;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LevelManager {

    private static Map<String, LevelObject> levels = new HashMap<>();

    public static void loadLevels() {
        levels = new HashMap<>();

        for (String levelName : Levels_YAML.getLevelNames())
            levels.put(levelName, new LevelObject(levelName));
    }

    public static void loadLevel(String levelName) {
        if (Levels_YAML.levelExists(levelName))
            levels.put(levelName, new LevelObject(levelName));
    }

    public static void unloadLevel(String levelName) {
        if (levels.containsKey(levelName))
            levels.remove(levelName);
    }

    public static List<String> getLevelNames(){
        List<String> levelNames = new ArrayList<>(levels.keySet());

        return levelNames;
    }

    public static Map<String, String> getLevelNamesLower() {
        List<String> levelNames = LevelManager.getLevelNames();
        Map<String, String> levelNamesLower = new HashMap<>();

        for (String levelName : levelNames)
            levelNamesLower.put(levelName.toLowerCase(), levelName);

        return levelNamesLower;
    }

    public static LevelObject getLevel(String levelName) {
        return levels.get(levelName);
    }

    public static boolean levelConfigured(String levelName) {
        return levels.containsKey(levelName);
    }

}
