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
                "level_name, (UNIX_TIMESTAMP(completion_date) * 1000) AS date, time_taken, mastery",
                "WHERE uuid=?", playerStats.getUUID());

        for (Map<String, String> completionResult : completionsResults)
        {
            String levelName = completionResult.get("level_name");

            if (Integer.parseInt(completionResult.get("mastery")) == 1)
                playerStats.addMasteryCompletion(levelName);

            playerStats.levelCompletion(
                    levelName,
                    Long.parseLong(completionResult.get("date")),
                    Long.parseLong(completionResult.get("time_taken"))
            );
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

        DatabaseQueries.runAsyncQuery(
                "INSERT INTO " + DatabaseManager.LEVEL_COMPLETIONS_TABLE +
                        " (uuid, level_name, completion_date, time_taken, mastery)" +
                        " VALUES (?,?,FROM_UNIXTIME(?),?,?)",
                levelCompletion.getUUID(),
                levelCompletion.getLevelName(),
                levelCompletion.getTimeOfCompletionMillis() / 1000, // cannot use built in method, need to do int division
                levelCompletion.getCompletionTimeElapsedMillis(),
                masteryBit
        );
    }

    public static void updateRecord(LevelCompletion levelCompletion, boolean value)
    {
        int recordInt = value ? 1 : 0;

        DatabaseQueries.runQuery(
                "UPDATE " + DatabaseManager.LEVEL_COMPLETIONS_TABLE +
                        " SET record=? WHERE " +
                        "uuid=? AND level_name=? AND completion_date=FROM_UNIXTIME(?)",
                recordInt,
                levelCompletion.getUUID(),
                levelCompletion.getLevelName(),
                levelCompletion.getTimeOfCompletionMillis() / 1000
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
        String query = "DELETE FROM " + DatabaseManager.LEVEL_COMPLETIONS_TABLE + " WHERE uuid=? AND level_name=? AND completion_date=FROM_UNIXTIME(?)";

        if (async)
            DatabaseQueries.runAsyncQuery(query, levelCompletion.getUUID(), levelCompletion.getLevelName(), levelCompletion.getTimeOfCompletionMillis() / 1000);
        else
            DatabaseQueries.runQuery(query, levelCompletion.getUUID(), levelCompletion.getLevelName(), levelCompletion.getTimeOfCompletionMillis() / 1000);
    }

    public static boolean hasCompleted(String uuid, String levelName)
    {
        Map<String, String> playerResult = DatabaseQueries.getResult(DatabaseManager.LEVEL_COMPLETIONS_TABLE, "*",
                " WHERE uuid=? AND level_name=?", uuid, levelName);

        return !playerResult.isEmpty();
    }

    public static void loadRecords(PlayerStats playerStats)
    {
        HashSet<LevelCompletion> levels = new HashSet<>();

        List<Map<String, String>> recordResults = DatabaseQueries.getResults(DatabaseManager.LEVEL_COMPLETIONS_TABLE,
                "level_name",
                "WHERE uuid=? AND record=1",
             playerStats.getUUID());

        for (Map<String, String> recordResult : recordResults)
        {
            LevelCompletion fastest = playerStats.getQuickestCompletion(Parkour.getLevelManager().get(recordResult.get("level_name")));
            levels.add(fastest);
        }
        playerStats.setRecords(levels);
    }

    public static HashMap<Level, Long> getRecordsFromName(String name)
    {
        // for offline records

        List<Map<String, String>> recordResults = DatabaseQueries.getResults(DatabaseManager.LEVEL_COMPLETIONS_TABLE + " lr",
                "level_name, time_taken",
                "JOIN " + DatabaseManager.PLAYERS_TABLE + " p ON p.uuid=lr.uuid " +
                        "WHERE p.name=? AND record=1", name);

        HashMap<Level, Long> levels = new HashMap<>();

        for (Map<String, String> recordResult : recordResults)
            levels.put(Parkour.getLevelManager().get(recordResult.get("level_name")), Long.parseLong(recordResult.get("time_taken")));

        return levels;
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
                "SELECT level_name, p.uuid AS player_uuid, p.name AS player_name, c.time_taken AS time_taken, c.completion_date AS completion_date " +
                        "FROM (" +
                        "  SELECT *, ROW_NUMBER() OVER (PARTITION BY level_name ORDER BY time_taken) AS row_num" +
                        "  FROM (" +
                        "    SELECT level_name, uuid, MIN(time_taken) AS time_taken, MIN(UNIX_TIMESTAMP(completion_date) * 1000) AS completion_date" +
                        "    FROM " + DatabaseManager.LEVEL_COMPLETIONS_TABLE +
                        "    WHERE time_taken > 0" +
                        "    GROUP BY level_name, uuid" +
                        "  ) AS grouped_completions" +
                        ") AS c " +
                        "JOIN " + DatabaseManager.PLAYERS_TABLE + " p ON c.uuid=p.uuid " +
                        "WHERE c.row_num <= 10 " +
                        "ORDER BY c.level_name, c.time_taken;"
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

                    // create completion
                    LevelCompletion levelCompletion = new LevelCompletion(
                            levelName,
                            results.getString("player_uuid"),
                            results.getString("player_name"),
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
        Parkour.getStatsManager().toggleLoadingLeaderboards(false);
    }

    public static List<LevelCompletion> getLeaderboard(String levelName)
    {

        ResultSet results = DatabaseQueries.getRawResults(
                "WITH min_times AS (" +
                      "     SELECT uuid, level_name, MIN(time_taken) AS fastest_completion FROM " + DatabaseManager.LEVEL_COMPLETIONS_TABLE + " " +
                      "     WHERE time_taken > 0 AND level_name=? GROUP BY uuid" +
                      ")" +
                      "SELECT p.uuid AS player_uuid, p.name AS player_name, m.fastest_completion AS fastest, (UNIX_TIMESTAMP(completion_date) * 1000) AS date " +
                      "FROM min_times m " +
                      "JOIN " + DatabaseManager.LEVEL_COMPLETIONS_TABLE + " c ON c.uuid=m.uuid AND c.level_name=m.level_name AND c.time_taken=m.fastest_completion " +
                      "JOIN " + DatabaseManager.PLAYERS_TABLE + " p ON p.uuid=m.uuid " +
                      "ORDER BY fastest ASC LIMIT 10",
                      levelName
        );

        List<LevelCompletion> leaderboard = new ArrayList<>();

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
}
