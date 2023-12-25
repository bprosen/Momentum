package com.renatusnetwork.parkour.data.levels;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.storage.mysql.DatabaseManager;
import com.renatusnetwork.parkour.storage.mysql.DatabaseQueries;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LevelsDB {

    public static HashMap<String, Level> getLevels()
    {

    }

    public static void updateName(String levelName, String newLevelName) {
        String query = "UPDATE levels SET " +
                "level_name=? WHERE level_name=?";

        DatabaseQueries.runAsyncQuery(query, newLevelName, levelName);
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
                " WHERE level_id=" + levelID + " AND completion_date >= ? AND completion_date < ?", start, end);

        return Long.parseLong(globalResults.get(0).get("total_completions"));
    }

    public static void updateReward(Level level) {
        String query = "UPDATE levels SET " +
                "reward=" + level.getReward() + " " +
                "WHERE level_id=" + level.getID()
                ;

        LevelData levelData = Parkour.getLevelManager().getLevelDataCache().get(level.getName());
        if (levelData != null)
            levelData.setReward(level.getReward());

        DatabaseQueries.runAsyncQuery(query);
    }

    public static void updateScoreModifier(Level level) {
        String query = "UPDATE levels SET " +
                "score_modifier=" + level.getScoreModifier() + " " +
                "WHERE level_id=" + level.getID();

        LevelData levelData = Parkour.getLevelManager().getLevelDataCache().get(level.getName());
        if (levelData != null)
            levelData.setScoreModifier(level.getScoreModifier());

        DatabaseQueries.runAsyncQuery(query);
    }

    public static void insertLevelRecord(String levelName, String uuid)
    {
        DatabaseQueries.runAsyncQuery(
        "INSERT INTO " + DatabaseManager.LEVEL_RECORDS_TABLE + " (level_name, uuid) VALUES ('" + levelName + "', '" + uuid + "')"
        );
    }

    public static void updateLevelRecord(String levelName, String uuid)
    {
        DatabaseQueries.runAsyncQuery(
        "UPDATE " + DatabaseManager.LEVEL_RECORDS_TABLE + " SET uuid='" + uuid + "' WHERE level_name='" + levelName + "'"
        );
    }

    public static void removeLevel(String levelName)
    {
        DatabaseQueries.runAsyncQuery("DELETE FROM levels WHERE level_name='" + levelName + "'");

        // this is just for extra clean up since they are not foreign key relationships
        DatabaseQueries.runAsyncQuery("DELETE FROM locations WHERE name='" + levelName + "-spawn'");
        DatabaseQueries.runAsyncQuery("DELETE FROM locations WHERE name='" + levelName + "-completion'");
    }
}
