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
                    levelCompletion.getTimeOfCompletionMillis() / 1000, // cannot use built in method, need to do int division
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
                    levelCompletion.getTimeOfCompletionMillis() / 1000, // cannot use built in method, need to do int division
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

    public static void removeCompletion(LevelCompletion levelCompletion, boolean async)
    {
        String query = "DELETE FROM " + DatabaseManager.LEVEL_COMPLETIONS_TABLE + " WHERE uuid=? AND completion_date=?";

        if (async)
            DatabaseQueries.runAsyncQuery(query, levelCompletion.getUUID(), levelCompletion.getTimeOfCompletionMillis());
        else
            DatabaseQueries.runQuery(query, levelCompletion.getUUID(), levelCompletion.getTimeOfCompletionMillis());
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
                            "r.uuid AS player_uuid, " +
                            "p.name AS player_name, " +
                            "r.level_name AS level_name, " +
                            "r.time_taken AS time_taken, " +
                            "r.completion_date AS completion_date " +
                        "FROM (" +
                            "SELECT " +
                                "*, " +
                                "ROW_NUMBER() OVER (PARTITION BY level_name ORDER BY time_taken) AS row_num " +
                            "FROM (" +
                                "SELECT " +
                                    "level_name, " +
                                    "uuid, " +
                                    "MIN(time_taken) AS time_taken, " +
                                    "MIN(completion_date) AS completion_date " +
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
            List<LevelCompletion> currentLB = new ArrayList<>();

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

                    LevelCompletion levelCompletion = Parkour.getLevelManager().createLevelCompletion(
                            results.getString("player_uuid"),
                            results.getString("player_name"),
                            levelName,
                            results.getLong("completion_date"),
                            results.getLong("time_taken")
                    );

                    currentLB.add(levelCompletion);
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

    public static List<LevelCompletion> getLeaderboard(String levelName)
    {

        ResultSet results = DatabaseQueries.getRawResults(
                "WITH min_times AS (" +
                      "     SELECT uuid, level_name, MIN(time_taken) AS fastest_completion FROM " + DatabaseManager.LEVEL_COMPLETIONS_TABLE + " " +
                      "     WHERE time_taken IS NOT NULL AND level_name=? GROUP BY uuid" +
                      ")" +
                      "SELECT p.uuid AS player_uuid, p.name AS player_name, m.fastest_completion AS fastest, completion_date " +
                      "FROM min_times m " +
                      "JOIN " + DatabaseManager.LEVEL_COMPLETIONS_TABLE + " lc ON lc.uuid=m.uuid AND lc.time_taken=m.fastest_completion " +
                      "JOIN " + DatabaseManager.PLAYERS_TABLE + " p ON p.uuid=m.uuid " +
                      "ORDER BY fastest ASC LIMIT 10",
                      levelName
        );

        List<LevelCompletion> leaderboard = new ArrayList<>();

        if (results != null)
        {
            try
            {
                LevelManager levelManager = Parkour.getLevelManager();
                while (results.next())
                {
                    // if not added already, add to leaderboard
                    LevelCompletion levelCompletion = levelManager.createLevelCompletion(
                            results.getString("player_uuid"),
                            results.getString("player_name"),
                            levelName,
                            results.getLong("completion_date"),
                            results.getLong("fastest")
                    );

                    leaderboard.add(levelCompletion);
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
}
