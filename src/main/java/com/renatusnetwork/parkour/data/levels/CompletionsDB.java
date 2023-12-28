package com.renatusnetwork.parkour.data.levels;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.storage.mysql.DatabaseManager;
import com.renatusnetwork.parkour.storage.mysql.DatabaseQueries;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class CompletionsDB
{
    public static void loadCompletions(PlayerStats playerStats)
    {
        List<Map<String, String>> completionsResults = DatabaseQueries.getResults(
                DatabaseManager.LEVEL_COMPLETIONS_TABLE,
                "*",
                "WHERE uuid=?", playerStats.getUUID());

        for (Map<String, String> completionResult : completionsResults)
            playerStats.levelCompletion(
                    playerStats.getUUID(),
                    playerStats.getPlayerName(),
                    completionResult.get("level_name"),
                    Long.parseLong(completionResult.get("date")),
                    Long.parseLong(completionResult.get("time_taken"))
            );

        // get individual levels beaten by looping through list
        int individualLevelsBeaten = 0;
        for (Level level : Parkour.getLevelManager().getLevels().values())
            if (playerStats.hasCompleted(level.getName()))
                individualLevelsBeaten++;

        playerStats.setIndividualLevelsBeaten(individualLevelsBeaten);
    }

    public static void insertCompletion(LevelCompletion levelCompletion, boolean isMastery)
    {
        int masteryBit = isMastery ? 1 : 0;

        DatabaseQueries.runAsyncQuery(
                "INSERT INTO " + DatabaseManager.LEVEL_COMPLETIONS_TABLE +
                        " (uuid, level_name, completion_date, time_taken, mastery, record)" +
                        " VALUES (?,?,FROM_UNIXTIME(?),?,?)",
                levelCompletion.getUUID(),
                levelCompletion.getLevelName(),
                levelCompletion.getTimeOfCompletionSeconds(),
                levelCompletion.getCompletionTimeElapsedMillis(),
                masteryBit
        );
    }

    public static void updateRecord(LevelCompletion levelCompletion)
    {
        DatabaseQueries.runQuery(
                "UPDATE " + DatabaseManager.LEVEL_COMPLETIONS_TABLE +
                        " SET record=NOT record WHERE " +
                        "uuid=? AND level_name=? AND completion_date=FROM_UNIXTIME(?)",
                levelCompletion.getUUID(),
                levelCompletion.getLevelName(),
                levelCompletion.getTimeOfCompletionSeconds()
        );
    }

    public static void removeCompletions(String uuid, String levelName)
    {
        DatabaseQueries.runAsyncQuery(
                "DELETE FROM " + DatabaseManager.LEVEL_COMPLETIONS_TABLE + " WHERE uuid=? AND level_name=?",
                uuid,
                levelName);
    }

    public static void removeCompletion(LevelCompletion levelCompletion, boolean async)
    {
        String query = "DELETE FROM " + DatabaseManager.LEVEL_COMPLETIONS_TABLE + " " +
                       "WHERE uuid=? AND level_name=? AND completion_date=FROM_UNIXTIME(?)";

        if (async)
            DatabaseQueries.runAsyncQuery(query, levelCompletion.getUUID(), levelCompletion.getLevelName(), levelCompletion.getTimeOfCompletionSeconds());
        else
            DatabaseQueries.runQuery(query, levelCompletion.getUUID(), levelCompletion.getLevelName(), levelCompletion.getTimeOfCompletionSeconds());
    }

    public static boolean hasCompleted(String uuid, String levelName)
    {
        List<Map<String, String>> playerResults = DatabaseQueries.getResults(DatabaseManager.LEVEL_COMPLETIONS_TABLE, "*",
                " WHERE uuid=? AND level_name=?", uuid, levelName);

        return !playerResults.isEmpty();
    }

    public static int getNumRecords(PlayerStats playerStats)
    {
        List<Map<String, String>> recordResults = DatabaseQueries.getResults(DatabaseManager.LEVEL_COMPLETIONS_TABLE,
                "COUNT(level_name) AS num_records",
                "WHERE uuid=? AND record=(1)",
             playerStats.getUUID());

        for (Map<String, String> recordResult : recordResults)
            return Integer.parseInt(recordResult.get("num_records"));

        return -1;
    }

    public static int getNumRecordsFromName(String name)
    {
        List<Map<String, String>> recordResults = DatabaseQueries.getResults(DatabaseManager.LEVEL_COMPLETIONS_TABLE + " lr",
                "COUNT(level_name) AS num_records",
                "JOIN " + DatabaseManager.PLAYERS_TABLE + " p ON p.uuid=lr.uuid " +
                        "WHERE p.name=? AND record=(1)", name);

        for (Map<String, String> recordResult : recordResults)
            return Integer.parseInt(recordResult.get("num_records"));

        return -1;
    }

    /*
     * Leader Board Section
     */
    public static void loadTotalCompletions()
    {
        List<Map<String, String>> levelsResults = DatabaseQueries.getResults(
                DatabaseManager.LEVEL_COMPLETIONS_TABLE,
                "level_name, COUNT(*) AS total_completions",
                "GROUP BY level_name"
        );

        for (Map<String, String> levelResult : levelsResults)
        {
            Level level = Parkour.getLevelManager().get(levelResult.get("level_name"));

            if (level != null)
                level.setTotalCompletionsCount(Integer.parseInt(levelResult.get("total_completions")));
        }
    }

    // can run complete async, only when all levels are loaded
    public static int getTotalCompletions(String levelName)
    {
        int completions;

        List<Map<String, String>> levelsResults = DatabaseQueries.getResults(
                DatabaseManager.LEVEL_COMPLETIONS_TABLE,
                "COUNT(*) AS total_completions",
                "WHERE level_name=?", levelName
        );

        completions = Integer.parseInt(levelsResults.get(0).get("total_completions"));

        return completions;
    }

    public static void loadLeaderboards()
    {
        Parkour.getStatsManager().toggleLoadingLeaderboards(true);

        ResultSet results = DatabaseQueries.getRawResults(
                "SELECT l.name AS level_name, p.uuid AS player_uuid, p.name AS player_name, c.time_taken AS time_taken, c.completion_date AS completion_date " +
                        "FROM (" +
                        "  SELECT *, ROW_NUMBER() OVER (PARTITION BY l.name ORDER BY time_taken) AS row_num" +
                        "  FROM (" +
                        "    SELECT l.name, p.uuid, MIN(time_taken) AS time_taken, MIN(completion_date) AS completion_date" +
                        "    FROM " + DatabaseManager.LEVEL_COMPLETIONS_TABLE +
                        "    WHERE time_taken > 0" +
                        "    GROUP BY l.name, p.uuid" +
                        "  ) AS grouped_completions" +
                        ") AS c " +
                        "JOIN " + DatabaseManager.PLAYERS_TABLE + " p ON c.uuid=p.uuid " +
                        "JOIN " + DatabaseManager.LEVELS_TABLE + " l ON c.level_name=l.level_name " +
                        "WHERE c.row_num <= 10 " +
                        "ORDER BY c.level_name, c.time_taken;"
        );

        if (results != null)
        {
            // default values
            Level currentLevel = null;
            HashMap<Integer, LevelCompletion> currentLB = new HashMap<>();
            int lbPlace = 1;

            try
            {
                while (results.next())
                {
                    String levelName = results.getString("level_name");

                    if (currentLevel == null || !currentLevel.getName().equalsIgnoreCase(levelName))
                    {
                        // if not at the start (level is null), set LB
                        if (currentLevel != null)
                            currentLevel.setLeaderboard(currentLB);

                        // initialize and adjust
                        lbPlace = 1;
                        currentLB.clear();
                        currentLevel = Parkour.getLevelManager().get(levelName);
                    }

                    // create completion
                    LevelCompletion levelCompletion = new LevelCompletion(
                            levelName,
                            results.getString("player_uuid"),
                            results.getString("player_name"),
                            results.getLong("completion_date"),
                            results.getLong("time_taken")
                    );

                    currentLB.put(lbPlace, levelCompletion);
                    lbPlace++;
                }

                // this makes it so the last level in the results will still get the leaderboard set
                if (currentLevel != null)
                    currentLevel.setLeaderboard(currentLB);
            }
            catch (SQLException exception)
            {
                exception.printStackTrace();
            }
        }
        Parkour.getStatsManager().toggleLoadingLeaderboards(false);
    }

    public static HashMap<Integer, LevelCompletion> getLeaderboard(String levelName)
    {

        ResultSet results = DatabaseQueries.getRawResults(
                "WITH min_times AS (" +
                      "     SELECT uuid, level_name, MIN(time_taken) AS fastest_completion FROM " + DatabaseManager.LEVEL_COMPLETIONS_TABLE + " " +
                      "     WHERE time_taken > 0 AND level_name=? GROUP BY uuid" +
                      ")" +
                      "SELECT p.uuid AS player_uuid, p.name AS player_name, min_times.fastest_completion AS fastest, (UNIX_TIMESTAMP(completion_date) * 1000) AS date " +
                      "JOIN " + DatabaseManager.LEVEL_COMPLETIONS_TABLE + " c " +
                      "ON c.uuid=min_times.uuid AND c.level_name=min_times.level_name AND c.time_taken=min_times.fastest_completion " +
                      "JOIN " + DatabaseManager.PLAYERS_TABLE + " p ON p.uuid=min_times.uuid ORDER BY min_times.fastest_time ASC LIMIT 10",
                      levelName
        );

        HashMap<Integer, LevelCompletion> leaderboard = new HashMap<>();
        int lbPlace = 1;

        if (results != null)
        {
            try
            {
                while (results.next())
                {
                    // if not added already, add to leaderboard
                    LevelCompletion levelCompletion = new LevelCompletion(
                            levelName,
                            results.getString("player_uuid"),
                            results.getString("player_name"),
                            results.getLong("date"),
                            results.getLong("time_taken")
                    );

                    leaderboard.put(lbPlace, levelCompletion);
                    lbPlace++;
                }
            }
            catch (SQLException exception)
            {
                Parkour.getPluginLogger().info("Error on getLeaderboard()");
                exception.printStackTrace();
            }
        }

        return leaderboard;
    }
}
