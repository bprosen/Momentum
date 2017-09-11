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

    public static List<String> getLevelNames(){
        List<String> levelNames = new ArrayList<>(levels.keySet());

        return levelNames;
    }

}
