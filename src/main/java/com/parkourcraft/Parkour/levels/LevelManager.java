package com.parkourcraft.Parkour.levels;


import com.parkourcraft.Parkour.storage.mysql.DatabaseManager;
import com.parkourcraft.Parkour.storage.mysql.DatabaseQueries;
import com.parkourcraft.Parkour.utils.storage.Levels_YAML;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LevelManager {

    private static Map<String, LevelObject> levelMap = new HashMap<>();
    private static Map<String, Integer> levelIDMap = new HashMap<>();

    public static void loadLevels() {
        levelMap = new HashMap<>();

        for (String levelName : Levels_YAML.getLevelNames())
            levelMap.put(levelName, new LevelObject(levelName));
    }

    public static void loadLevelIDs() {
        List<Map<String, String>> levelsResults = DatabaseQueries.getResults("levels", "");

        if (levelsResults.size() > 0) {
            levelIDMap = new HashMap<>();

            for (Map<String, String> levelsResult : levelsResults) {
                levelIDMap.put(
                        levelsResult.get("level_name"),
                        Integer.parseInt(levelsResult.get("level_id"))
                );
            }
        }
    }

    public static void syncLevelIDs() {
        List<String> levelsMissingID = new ArrayList<>();

        for (String levelName : levelMap.keySet()) {
            if (!levelIDMap.containsKey(levelName))
                levelsMissingID.add(levelName);
        }

        if (levelsMissingID.size() > 0) {
            List<String> insertQueries = new ArrayList<>();

            for (String levelName : levelsMissingID) {
                String query = "INSERT INTO levels " +
                        "(level_name)" +
                        " VALUES " +
                        "('" + levelName + "')";

                insertQueries.add(query);
            }

            String finalQuery = "";
            for (String sql : insertQueries)
                finalQuery = finalQuery + sql + "; ";

            DatabaseManager.runQuery(finalQuery);
            loadLevelIDs();
        }
    }

    public static void loadLevel(String levelName) {
        if (Levels_YAML.levelExists(levelName))
            levelMap.put(levelName, new LevelObject(levelName));
    }

    public static void unloadLevel(String levelName) {
        if (levelMap.containsKey(levelName))
            levelMap.remove(levelName);
    }

    public static List<String> getLevelNames(){
        List<String> levelNames = new ArrayList<>(levelMap.keySet());

        return levelNames;
    }

    public static Map<String, String> getLevelNamesLower() {
        List<String> levelNames = LevelManager.getLevelNames();
        Map<String, String> levelNamesLower = new HashMap<>();

        for (String levelName : levelNames)
            levelNamesLower.put(levelName.toLowerCase(), levelName);

        return levelNamesLower;
    }

    public static String getLevelNameFromID(int levelID) {
        for (Map.Entry<String, Integer> entry : levelIDMap.entrySet()) {
            if (entry.getValue() == levelID)
                return entry.getKey();
        }

        return "";
    }

    public static int getLevelID(String levelName) {
        return levelIDMap.get(levelName);
    }

    public static LevelObject getLevel(String levelName) {
        return levelMap.get(levelName);
    }

    public static boolean levelConfigured(String levelName) {
        return levelMap.containsKey(levelName);
    }

}
