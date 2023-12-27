package com.renatusnetwork.parkour.data.levels;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.stats.LevelCompletion;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.storage.mysql.DatabaseManager;
import com.renatusnetwork.parkour.storage.mysql.DatabaseQueries;

import java.sql.Connection;
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

    public static void insertLevel(String levelName)
    {
        DatabaseQueries.runAsyncQuery("INSERT INTO " + DatabaseManager.LEVELS_TABLE + "(name) VALUES (?)", levelName);
    }

    public static void updateName(String levelName, String newLevelName)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.LEVELS_TABLE + " SET name=? WHERE name=?", newLevelName, levelName);
    }

    public static long getGlobalCompletions()
    {
        List<Map<String, String>> globalResults = DatabaseQueries.getResults(DatabaseManager.LEVEL_COMPLETIONS_TABLE,
                "COUNT(*) AS total_completions", "");

        return Long.parseLong(globalResults.get(0).get("total_completions"));
    }

    public static long getCompletionsBetweenDates(String levelName, String start, String end)
    {
        List<Map<String, String>> globalResults = DatabaseQueries.getResults(DatabaseManager.LEVEL_COMPLETIONS_TABLE,
                "COUNT(*) AS total_completions",
                " WHERE name=? AND completion_date BETWEEN ? AND ?", levelName, start, end);

        return Long.parseLong(globalResults.get(0).get("total_completions"));
    }

    public static void updateReward(String levelName, int reward)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.LEVELS_TABLE + " SET reward=? WHERE name=?", reward, levelName);
    }

    public static void updateTitle(String levelName, String title)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.LEVELS_TABLE + " SET title=? WHERE name=?", title, levelName);
    }

    public static void updateMaxCompletions(String levelName, int maxCompletions)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.LEVELS_TABLE + " SET max_completions=? WHERE name=?", maxCompletions, levelName);
    }

    public static void updateBroadcast(String levelName)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.LEVELS_TABLE + " SET broadcast=NOT broadcast WHERE name=?", levelName);
    }

    public static void insertLevelRequired(String levelName, String requiredLevelName)
    {
        DatabaseQueries.runAsyncQuery(
                "INSERT INTO " + DatabaseManager.LEVEL_REQUIRED_LEVELS_TABLE + " (level_name, required_level_name) VALUES (?,?)",
                levelName, requiredLevelName
        );
    }

    public static void removeLevelRequired(String levelName, String requiredLevelName)
    {
        DatabaseQueries.runAsyncQuery(
                "DELETE FROM " + DatabaseManager.LEVEL_REQUIRED_LEVELS_TABLE + " WHERE level_name=? AND required_level_name=?",
                levelName, requiredLevelName
        );
    }

    public static void removeLevel(String levelName)
    {
        Connection connection = Parkour.getDatabaseManager().getConnection().get();

        try
        {
            connection.setAutoCommit(false);

            DatabaseQueries.runAsyncQuery("DELETE FROM " + DatabaseManager.LEVELS_TABLE + " WHERE name='" + levelName + "'");

            // this is just for extra clean up since they are not foreign key relationships
            DatabaseQueries.runAsyncQuery("DELETE FROM " + DatabaseManager.LOCATIONS_TABLE + " WHERE name='" + levelName + "-spawn'");
            DatabaseQueries.runAsyncQuery("DELETE FROM " + DatabaseManager.LOCATIONS_TABLE + " WHERE name='" + levelName + "-completion'");

            // commit
            connection.commit();

            // fix auto commit
            connection.setAutoCommit(true);
        }
        catch (SQLException exception)
        {
            try
            {
                connection.rollback();
                Parkour.getPluginLogger().info("Transaction failed on LevelsDB.removeLevel(), rolling back");
                exception.printStackTrace();
            }
            catch (SQLException rollbackException)
            {
                Parkour.getPluginLogger().info("Failure rolling back transaction on LevelsDB.removeLevel()");
                rollbackException.printStackTrace();
            }
        }
    }
}
