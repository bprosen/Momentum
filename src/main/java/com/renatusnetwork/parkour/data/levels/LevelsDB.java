package com.renatusnetwork.parkour.data.levels;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.storage.mysql.DatabaseQueries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LevelsDB {

    public static HashMap<String, LevelData> getDataCache() {
        HashMap<String, LevelData> levelData = new HashMap<>();

        List<Map<String, String>> levelsResults = DatabaseQueries.getResults(
                "levels",
                "level_id, level_name, reward, score_modifier",
                ""
        );

        for (Map<String, String> levelResult : levelsResults)
            levelData.put(
                    levelResult.get("level_name"),
                    new LevelData(
                            Integer.parseInt(levelResult.get("level_id")),
                            Integer.parseInt(levelResult.get("reward")),
                            Integer.parseInt(levelResult.get("score_modifier")),
                            RatingDB.getAverageRating(Integer.parseInt(levelResult.get("level_id"))),
                            RatingDB.getTotalRatings(Integer.parseInt(levelResult.get("level_id")))
                    )
            );

        Parkour.getPluginLogger().info("Levels in data cache: " + levelData.size());
        return levelData;
    }

    public static void syncDataCache() {
        for (Map.Entry<String, Level> entry : Parkour.getLevelManager().getLevels().entrySet())
            syncDataCache(entry.getValue(), Parkour.getLevelManager().getLevelDataCache());
    }

    public static void syncDataCache(Level level, Map<String, LevelData> levelDataCache) {
        LevelData levelData = levelDataCache.get(level.getName());

        if (levelData != null) {
            level.setID(levelData.getID());
            level.setReward(levelData.getReward());
            level.setScoreModifier(levelData.getScoreModifier());
            level.setRating(levelData.getRating());
            level.setRatingsCount(levelData.getRatingsCount());
        }
    }

    public static boolean syncLevelData() {

        HashMap<String, LevelData> levelCache = Parkour.getLevelManager().getLevelDataCache();
        HashMap<String, Level> levels = Parkour.getLevelManager().getLevels();

        // if not equal size, then sort through
        if (levelCache.size() != levels.size()) {

            List<String> insertQueries = new ArrayList<>();
            for (Level level : levels.values())
                if (!levelCache.containsKey(level.getName()))
                    insertQueries.add(
                            "INSERT INTO levels " +
                                    "(level_name)" +
                                    " VALUES " +
                                    "('" + level.getName() + "')"
                    );

            if (insertQueries.size() > 0) {
                String finalQuery = "";
                for (String sql : insertQueries)
                    finalQuery = finalQuery + sql + "; ";

                Parkour.getDatabaseManager().run(finalQuery);
                return true;
            }
            return false;
        }
        return false;
    }

    public static void updateName(String levelName, String newLevelName) {
        String query = "UPDATE levels SET " +
                "level_name='" + newLevelName + "' WHERE " +
                "level_name='" + levelName + "'";

        Parkour.getDatabaseManager().add(query);
    }

    public static long getGlobalCompletions() {
        List<Map<String, String>> globalResults = DatabaseQueries.getResults("completions",
                "COUNT(*) AS total_completions", "");

        return Long.parseLong(globalResults.get(0).get("total_completions"));
    }

    public static long getCompletionsBetweenDates(int levelID, String start, String end)
    {
        List<Map<String, String>> globalResults = DatabaseQueries.getResults("completions",
                "COUNT(*) AS total_completions",
                " WHERE level_id=" + levelID + " AND completion_date >= '" + start + "' AND completion_date < '" + end + "'");

        return Long.parseLong(globalResults.get(0).get("total_completions"));
    }

    public static void updateReward(Level level) {
        String query = "UPDATE levels SET " +
                "reward=" + level.getReward() + " " +
                "WHERE level_id=" + level.getID() + ""
                ;

        LevelData levelData = Parkour.getLevelManager().getLevelDataCache().get(level.getName());
        if (levelData != null)
            levelData.setReward(level.getReward());

        Parkour.getDatabaseManager().add(query);
    }

    public static void updateScoreModifier(Level level) {
        String query = "UPDATE levels SET " +
                "score_modifier=" + level.getScoreModifier() + " " +
                "WHERE level_id=" + level.getID() + "";

        LevelData levelData = Parkour.getLevelManager().getLevelDataCache().get(level.getName());
        if (levelData != null)
            levelData.setScoreModifier(level.getScoreModifier());

        Parkour.getDatabaseManager().add(query);
    }
}
