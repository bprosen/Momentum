package com.renatusnetwork.parkour.data.levels;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.leaderboards.LevelLBPosition;
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
                "level_name, completion_date, time_taken, mastery",
                "WHERE uuid=?", playerStats.getUUID());

        LevelManager levelManager = Parkour.getLevelManager();
        for (Map<String, String> completionResult : completionsResults)
        {
            String levelName = completionResult.get("level_name");

            if (Integer.parseInt(completionResult.get("mastery")) == 1)
                playerStats.addMasteryCompletion(levelName);

            String timeTaken = completionResult.get("time_taken");
            long creationDate = Long.parseLong(completionResult.get("completion_date"));

            LevelCompletion completion = timeTaken != null ?
                    levelManager.createLevelCompletion(playerStats.getUUID(), playerStats.getName(), levelName, creationDate, Long.parseLong(timeTaken)) :
                    levelManager.createLevelCompletion(playerStats.getUUID(), playerStats.getName(), levelName, creationDate, -1);

            playerStats.levelCompletion(completion);
        }

        // get individual levels beaten by looping through list
        int individualLevelsBeaten = 0;
        for (Level level : Parkour.getLevelManager().getLevels().values())
            if (playerStats.hasCompleted(level))
                individualLevelsBeaten++;

        playerStats.setIndividualLevelsBeaten(individualLevelsBeaten);
    }

    public static void insertCompletion(LevelCompletion levelCompletion, boolean isMastery)
    {
        int masteryBit = isMastery ? 1 : 0;

        if (levelCompletion.wasTimed())
            DatabaseQueries.runAsyncQuery(
                    "INSERT INTO " + DatabaseManager.LEVEL_COMPLETIONS_TABLE +
                            " (uuid, completion_date, level_name, time_taken, mastery)" +
                            " VALUES (?,?,?,?,?)",
                    levelCompletion.getUUID(),
                    levelCompletion.getTimeOfCompletionMillis(), // cannot use built in method, need to do int division
                    levelCompletion.getLevelName(),
                    levelCompletion.getCompletionTimeElapsedMillis(),
                    masteryBit
            );
        else
            DatabaseQueries.runAsyncQuery(
                    "INSERT INTO " + DatabaseManager.LEVEL_COMPLETIONS_TABLE +
                            " (uuid, completion_date, level_name, mastery)" +
                            " VALUES (?,?,?,?)",
                    levelCompletion.getUUID(),
                    levelCompletion.getTimeOfCompletionMillis(), // cannot use built in method, need to do int division
                    levelCompletion.getLevelName(),
                    masteryBit
            );
    }

    public static void removeCompletions(String uuid, String levelName)
    {
        DatabaseQueries.runAsyncQuery(
                "DELETE FROM " + DatabaseManager.LEVEL_COMPLETIONS_TABLE + " WHERE uuid=? AND level_name=?",
                uuid,
                levelName);
    }

    public static void removeCompletionFromName(String playerName, String levelName, long timeTaken, boolean async)
    {
        String query = "DELETE lcOuter FROM " + DatabaseManager.LEVEL_COMPLETIONS_TABLE + " lcOuter " +
                            // this gets the single completion date
                            "JOIN (" +
                                "SELECT lc.uuid, completion_date " +
                                "FROM " + DatabaseManager.LEVEL_COMPLETIONS_TABLE + " lc " +
                                "JOIN " + DatabaseManager.PLAYERS_TABLE + " p ON p.uuid=lc.uuid " +
                                "WHERE p.name=? AND lc.level_name=? AND lc.time_taken=? " +
                                "ORDER BY lc.time_taken " +
                                "LIMIT 1" +
                            // deletes the SINGLE result
                            ") s ON s.uuid=lcOuter.uuid AND s.completion_date=lcOuter.completion_date";

        if (async)
            DatabaseQueries.runAsyncQuery(query, playerName, levelName, timeTaken);
        else
            DatabaseQueries.runQuery(query, playerName, levelName, timeTaken);
    }

    public static boolean hasCompleted(String uuid, String levelName)
    {
        Map<String, String> playerResult = DatabaseQueries.getResult(DatabaseManager.LEVEL_COMPLETIONS_TABLE, "*",
                " WHERE uuid=? AND level_name=?", uuid, levelName);

        return !playerResult.isEmpty();
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

    public static void loadLeaderboards()
    {
        Parkour.getLevelManager().setLoadingLeaderboards(true);

        ResultSet results = DatabaseQueries.getRawResults(
                "SELECT " +
                            "p.name AS player_name, " +
                            "r.level_name AS level_name, " +
                            "r.time_taken AS time_taken " +
                        "FROM (" +
                            "SELECT " +
                                "*, " +
                                "ROW_NUMBER() OVER (PARTITION BY level_name ORDER BY time_taken) AS row_num " +
                            "FROM (" +
                                "SELECT " +
                                    "level_name, " +
                                    "uuid, " +
                                    "MIN(time_taken) AS time_taken " +
                                "FROM " + DatabaseManager.LEVEL_COMPLETIONS_TABLE + " " +
                                "WHERE time_taken IS NOT NULL " +
                                "GROUP BY level_name, uuid " +
                            ") AS g " +
                        ") AS r " +
                    "JOIN " + DatabaseManager.PLAYERS_TABLE + " p ON r.uuid=p.uuid " +
                    "WHERE r.row_num <= 10 " +
                    "ORDER BY level_name, time_taken;"
        );

        if (results != null)
        {
            // default values
            Level currentLevel = null;
            List<LevelLBPosition> currentLB = new ArrayList<>();

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
                        currentLB = new ArrayList<>();
                        currentLevel = Parkour.getLevelManager().get(levelName);
                    }

                    LevelLBPosition lbPosition = new LevelLBPosition(
                            levelName,
                            results.getString("player_name"),
                            results.getLong("time_taken")
                    );

                    currentLB.add(lbPosition);
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
        Parkour.getLevelManager().setLoadingLeaderboards(false);
    }

    public static List<LevelLBPosition> getLeaderboard(String levelName)
    {

        ResultSet results = DatabaseQueries.getRawResults(
                "WITH min_times AS (" +
                      "     SELECT uuid, level_name, MIN(time_taken) AS fastest_completion FROM " + DatabaseManager.LEVEL_COMPLETIONS_TABLE +
                      "     WHERE time_taken IS NOT NULL AND level_name=? GROUP BY uuid" +
                      ")" +
                      "SELECT p.name AS player_name, m.fastest_completion AS fastest " +
                      "FROM min_times m " +
                      "JOIN " + DatabaseManager.PLAYERS_TABLE + " p ON p.uuid=m.uuid " +
                      "ORDER BY fastest ASC LIMIT 10",
                      levelName
        );

        List<LevelLBPosition> leaderboard = new ArrayList<>();

        if (results != null)
        {
            try
            {
                while (results.next())
                {
                    // if not added already, add to leaderboard
                    LevelLBPosition lbPosition = new LevelLBPosition(
                            levelName,
                            results.getString("player_name"),
                            results.getLong("fastest")
                    );

                    leaderboard.add(lbPosition);
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

    public static boolean isFasterThanRecord(LevelCompletion levelCompletion)
    {
        Map<String, String> singleResult = DatabaseQueries.getResult(
                DatabaseManager.LEVEL_COMPLETIONS_TABLE, "MIN(time_taken) AS fastest",
                "WHERE level_name=? AND time_taken IS NOT NULL", levelCompletion.getLevelName()
        );

        if (!singleResult.isEmpty())
        {
            long timeTaken = Long.parseLong(singleResult.get("fastest"));

            return timeTaken > levelCompletion.getCompletionTimeElapsedMillis();
        }

        return false;
    }

    public static void loadRecords(PlayerStats playerStats)
    {
        HashMap<Level, Long> records = new HashMap<>();

        ResultSet results = DatabaseQueries.getRawResults(
                "SELECT DISTINCT " +
                          "r.level_name AS level_name, " +
                          "r.time_taken AS time_taken " +
                      "FROM " +
                          "(" +
                              "SELECT " +
                                  "level_name, " +
                                  "MIN(time_taken) AS time_taken " +
                              "FROM " +
                                  "level_completions " +
                              "GROUP BY level_name" +
                          ") AS r " +
                      "JOIN level_completions ou ON " +
                          "ou.level_name=r.level_name AND " +
                          "ou.time_taken=r.time_taken " +
                      "WHERE ou.uuid=?", playerStats.getUUID());

        if (results != null)
        {
            try
            {
                while (results.next())
                    records.put(Parkour.getLevelManager().get(results.getString("level_name")), results.getLong("time_taken"));
            }
            catch (SQLException exception)
            {
                Parkour.getPluginLogger().info("Error on getLeaderboard()");
                exception.printStackTrace();
            }
        }
        playerStats.setRecords(records);
    }
}
