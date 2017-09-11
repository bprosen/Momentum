package com.parkourcraft.Parkour.utils.storage;


import com.parkourcraft.Parkour.storage.SaveManager;
import com.parkourcraft.Parkour.storage.local.FileManager;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class Levels_YAML {

    private static FileConfiguration levels = FileManager.getFileConfig("levels");

    public static void create(String levelName) {
        if (!levels.isSet(levelName))
            levels.set(levelName + ".reward", 0);

        FileManager.save("levels");
    }

    public static boolean levelExists(String levelName) {
        if (levels.isSet(levelName))
            return true;
        return false;
    }

    public static List<String> getLevelNames() {
        List<String> levelNames = new ArrayList<String>(levels.getKeys(false));

        return levelNames;
    }

    public static void setTitle(String levelName, String title) {
        if (levels.isSet(levelName))
            levels.set(levelName + ".title", title);

        FileManager.save("levels");
    }

    public static void setReward(String levelName, int reward) {
        if (levels.isSet(levelName))
            levels.set(levelName + ".reward", reward);

        FileManager.save("levels");
    }

    public static void setStartLocation(String levelName, String locationName) {
        if (levels.isSet(levelName))
            levels.set(levelName + ".start_location", locationName);

        FileManager.save("levels");
    }

    public static void setRespawnLocation(String levelName, String locationName) {
        if (levels.isSet(levelName))
            levels.set(levelName + ".respawn_location", locationName);

        FileManager.save("levels");
    }

    public static void setMessage(String levelName, String message) {
        if (levels.isSet(levelName))
            levels.set(levelName + ".message", message);

        FileManager.save("levels");
    }

    public static void setMaxCompletions(String levelName, int maxCompletions) {
        if (levels.isSet(levelName)) {
            if (maxCompletions == -1)
                levels.set(levelName + ".max_completions", null);
            else
                levels.set(levelName + ".max_completions", maxCompletions);
        }

        FileManager.save("levels");
    }

}
