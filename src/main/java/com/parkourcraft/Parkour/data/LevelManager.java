package com.parkourcraft.Parkour.data;

import com.parkourcraft.Parkour.data.levels.LevelObject;
import com.parkourcraft.Parkour.storage.mysql.DatabaseManager;
import com.parkourcraft.Parkour.storage.mysql.DatabaseQueries;
import com.parkourcraft.Parkour.data.levels.Levels_YAML;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LevelManager {

    private static Map<String, LevelObject> levelsMap = new HashMap<>();
    private static Map<String, Integer> levelIDMap = new HashMap<>();

    public static void load(String levelName) {
        if (!Levels_YAML.exists(levelName)
                && exists(levelName))
            levelsMap.remove(levelName);
        else
            levelsMap.put(levelName, new LevelObject(levelName));
    }

    public static void loadLevels() {
        levelsMap = new HashMap<>();

        for (String levelName : Levels_YAML.getNames())
            load(levelName);
    }

    public static void loadLevelIDs() {
        List<Map<String, String>> levelsResults = DatabaseQueries.getResults(
                "levels",
                "*",
                ""
        );

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

        for (String levelName : levelsMap.keySet()) {
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

    public static boolean exists(String levelName) {
        return levelsMap.containsKey(levelName);
    }

    public static void create(String levelName) {
        Levels_YAML.create(levelName);
    }

    public static void remove(String levelName) {
        if (exists(levelName))
            Levels_YAML.remove(levelName);
    }

    public static List<String> getNames(){
        return new ArrayList<>(levelsMap.keySet());
    }

    public static Map<String, String> getNamesLower() {
        Map<String, String> levelNamesLower = new HashMap<>();

        for (String levelName : getNames())
            levelNamesLower.put(levelName.toLowerCase(), levelName);

        return levelNamesLower;
    }

    public static String getName(int levelID) {
        for (Map.Entry<String, Integer> entry : levelIDMap.entrySet()) {
            if (entry.getValue() == levelID)
                return entry.getKey();
        }

        return "";
    }

    public static int getID(String levelName) {
        return levelIDMap.get(levelName);
    }

    public static LevelObject getLevel(String levelName) {
        return levelsMap.get(levelName);
    }

}
