package com.parkourcraft.Parkour.stats.objects;


import com.parkourcraft.Parkour.storage.mysql.DatabaseManager;

import java.util.HashMap;
import java.util.Map;

public class PlayerStats {

    private String playerName;
    private String UUID;

    private Map<String, LevelStats> levelStatsMap = new HashMap<>();

    public PlayerStats(String playerName, String UUID) {
        this.playerName = playerName;
        this.UUID = UUID;
    }

    public void levelCompletion(String levelName, long timeOfCompletion, long completionTimeElapsed) {
        if (!levelStatsMap.containsKey(levelName))
            levelStatsMap.put(levelName, new LevelStats(levelName));

        LevelCompletion levelCompletion = new LevelCompletion(timeOfCompletion, completionTimeElapsed, false);

        levelStatsMap.get(levelName).levelCompletion(levelCompletion);
    }

    public String getUUID() {
        return UUID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void updateIntoDatabase() {
        // update player information in the players table
        String playerDataSQL = "REPLACE INTO " +
                "players (uuid, player_name) " +
                "VALUES (" + UUID + ", " + playerName + ")"
                ;

        DatabaseManager.addUpdateQuery(playerDataSQL);

        for (Map.Entry<String, LevelStats> entry : levelStatsMap.entrySet()) {
            for (LevelCompletion levelCompletion : entry.getValue().getCompletionsNotInDatabase()) {
                String updateQuery = "INSERT INTO " +
                        "completions (uuid, level_name, completion_time, date) " +
                        "VALUES (" +
                        UUID + ", " +
                        entry.getKey() + ", " +
                        levelCompletion.getCompletionTimeElapsed() + ", " +
                        "FROM_UNIXTIME(" + (levelCompletion.getTimeOfCompletion() / 1000) + ")" +
                        ")"
                        ;

                DatabaseManager.addUpdateQuery(updateQuery);
            }
        }
    }

}
