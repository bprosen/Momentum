package com.parkourcraft.Parkour.data.stats;

import com.parkourcraft.Parkour.data.LevelManager;
import com.parkourcraft.Parkour.storage.mysql.DatabaseManager;
import com.parkourcraft.Parkour.storage.mysql.DatabaseQueries;

import java.text.SimpleDateFormat;
import java.util.*;

public class PlayerStats {

    private String UUID;
    private String playerName;
    private int playerID;

    private long levelStartTime = 0;

    private Map<String, LevelStats> levelStatsMap = new HashMap<>();

    public PlayerStats(String UUID, String playerName) {
        this.UUID = UUID;
        this.playerName = playerName;

        loadFromDatabase();
    }

    public void levelCompletion(String levelName, long timeOfCompletion, long completionTimeElapsed, boolean inDatabase) {
        if (!levelStatsMap.containsKey(levelName))
            levelStatsMap.put(levelName, new LevelStats(levelName));

        LevelCompletion levelCompletion = new LevelCompletion(timeOfCompletion, completionTimeElapsed, inDatabase);

        levelStatsMap.get(levelName).levelCompletion(levelCompletion);
    }

    public String getUUID() {
        return UUID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void startedLevel() {
        levelStartTime = System.currentTimeMillis();
    }

    public void disableLevelStartTime() {
        levelStartTime = 0;
    }

    public long getLevelStartTime() {
        return levelStartTime;
    }

    public int getLevelCompletionsCount(String levelName) {
        if (levelStatsMap.containsKey(levelName))
            return levelStatsMap.get(levelName).getCompletionsCount();

        return 0;
    }

    public List<LevelCompletion> getQuickestCompletions(String levelName) {
        if (levelStatsMap.containsKey(levelName))
            return levelStatsMap.get(levelName).getQuickestCompletions();

        return new ArrayList<>();
    }


    public Map<String, LevelStats> getLevelStatsMap() {
        return levelStatsMap;
    }

    public void loadFromDatabase() {
        List<Map<String, String>> playerResults = DatabaseQueries.getResults(
                "players",
                "*",
                " WHERE uuid='" + UUID + "'"
        );

        if (playerResults.size() == 0) {
            DatabaseManager.runQuery("INSERT INTO players " +
                    "(uuid, player_name)" +
                    " VALUES " +
                    "('" + UUID + "', '" + playerName + "')"
            );

            loadFromDatabase();
        } else {
            for (Map<String, String> playerResult : playerResults)
                playerID = Integer.parseInt(playerResult.get("player_id"));

            List<Map<String, String>> completionsResults = DatabaseQueries.getResults(
                    "completions",
                    "*",
                    "WHERE player_id=" + playerID + ""
            );

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");

            for (Map<String, String> completionsResult : completionsResults) {
                String levelName = LevelManager.getName(Integer.parseInt(completionsResult.get("level_id")));

                Date date;
                try {
                    date = dateFormat.parse(completionsResult.get("completion_date"));
                } catch (Exception e) {
                    continue;
                }

                levelCompletion(
                        levelName,
                        date.getTime(),
                        Long.parseLong(completionsResult.get("time_taken")),
                        true
                );
            }
        }
    }

    public void updateIntoDatabase() {
        String playersQuery = "UPDATE players SET " +
                "player_name='" + playerName + "' " +
                "WHERE uuid='" + UUID + "'"
                ;

        DatabaseManager.addUpdateQuery(playersQuery);

        for (Map.Entry<String, LevelStats> levelStatsEntry : levelStatsMap.entrySet()) {
            for (LevelCompletion levelCompletion : levelStatsEntry.getValue().getCompletionsNotInDatabase()) {
                String updateQuery = "INSERT INTO completions " +
                        "(player_id, level_id, time_taken, completion_date)" +
                        " VALUES (" +
                        playerID + ", " +
                        LevelManager.getID(levelStatsEntry.getKey()) + ", " +
                        levelCompletion.getCompletionTimeElapsed() + ", " +
                        "FROM_UNIXTIME(" + (levelCompletion.getTimeOfCompletion() / 1000) + ")" +
                        ")"
                        ;

                DatabaseManager.addUpdateQuery(updateQuery);
                levelCompletion.enteredIntoDatabase();
            }
        }
    }

}
