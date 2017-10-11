package com.parkourcraft.Parkour.data.levels;

import com.parkourcraft.Parkour.Parkour;
import com.parkourcraft.Parkour.storage.mysql.DatabaseManager;
import com.parkourcraft.Parkour.storage.mysql.DatabaseQueries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Levels_DB {

    static Map<String, LevelData> getDataCache() {
        Map<String, LevelData> levelData = new HashMap<>();

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
                            Integer.parseInt(levelResult.get("score_modifier"))
                    )
            );

        return levelData;
    }

    static void syncDataCache() {
        for (LevelObject levelObject : Parkour.levels.getLevels())
            syncData(levelObject, Parkour.levels.getLevelDataCache());
    }

    static void syncData(LevelObject level, Map<String, LevelData> levelDataCache) {
        LevelData levelData = levelDataCache.get(level.getName());

        if (levelData != null) {
            level.setID(levelData.getID());
            level.setReward(levelData.getReward());
            level.setScoreModifier(levelData.getScoreModifier());
        }
    }

    static boolean syncAllData() {
        List<String> insertQueries = new ArrayList<>();

        for (LevelObject level : Parkour.levels.getLevels())
            if (!Parkour.levels.getLevelDataCache().containsKey(level.getName()))
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

            Parkour.database.run(finalQuery);
            return true;
        }

        return false;
    }

    public static void updateReward(LevelObject level) {
        String query = "UPDATE levels SET " +
                "reward=" + level.getReward() + " " +
                "WHERE level_id=" + level.getID() + ""
                ;

        LevelData levelData = Parkour.levels.getLevelDataCache().get(level.getName());
        if (levelData != null)
            levelData.setReward(level.getReward());

        Parkour.database.add(query);
    }

    public static void updateScoreModifier(LevelObject level) {
        String query = "UPDATE levels SET " +
                "score_modifier=" + level.getScoreModifier() + " " +
                "WHERE level_id=" + level.getID() + ""
                ;

        LevelData levelData = Parkour.levels.getLevelDataCache().get(level.getName());
        if (levelData != null)
            levelData.setScoreModifier(level.getScoreModifier());

        Parkour.database.add(query);
    }

}
