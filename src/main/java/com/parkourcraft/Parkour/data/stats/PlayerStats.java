package com.parkourcraft.Parkour.data.stats;

import com.parkourcraft.Parkour.data.LevelManager;
import com.parkourcraft.Parkour.data.levels.LevelObject;
import com.parkourcraft.Parkour.data.perks.Perk;
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
    private Map<Integer, Long> perks = new HashMap<>();

    public PlayerStats(String UUID, String playerName) {
        this.UUID = UUID;
        this.playerName = playerName;

        load();
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

    public int getPlayerID() {
        return playerID;
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

    public void load() {
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

            load();
        } else {
            for (Map<String, String> playerResult : playerResults)
                playerID = Integer.parseInt(playerResult.get("player_id"));

            loadCompletionsData();
            loadPerksData();
        }

    }

    public void loadCompletionsData() {
        List<Map<String, String>> completionsResults = DatabaseQueries.getResults(
                "completions",
                "level_id, time_taken, UNIX_TIMESTAMP(completion_date) AS date",
                "WHERE player_id=" + playerID + ""
        );

        for (Map<String, String> completionsResult : completionsResults) {
            LevelObject levelObject = LevelManager.get(Integer.parseInt(completionsResult.get("level_id")));

            if (levelObject != null)
                levelCompletion(
                        levelObject.getName(),
                        (Long.parseLong(completionsResult.get("date")) * 1000),
                        Long.parseLong(completionsResult.get("time_taken")),
                        true
                );
        }
    }

    public void loadPerksData() {
        List<Map<String, String>> completionsResults = DatabaseQueries.getResults(
                "ledger",
                "perk_id, UNIX_TIMESTAMP(date) AS date",
                "WHERE player_id=" + playerID + ""
        );

        for (Map<String, String> completionsResult : completionsResults)
            perks.put(
                    Integer.parseInt(completionsResult.get("perk_id")),
                    Long.parseLong(completionsResult.get("date"))
            );
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
                        LevelManager.getIDFromCache(levelStatsEntry.getKey()) + ", " +
                        levelCompletion.getCompletionTimeElapsed() + ", " +
                        "FROM_UNIXTIME(" + (levelCompletion.getTimeOfCompletion() / 1000) + ")" +
                        ")"
                        ;

                DatabaseManager.addUpdateQuery(updateQuery);
                levelCompletion.enteredIntoDatabase();
            }
        }
    }

    public void addPerk(int perkID, Long time) {
        perks.put(perkID, time);
    }

    public boolean hasPerkID(int perkID) {
        return perks.containsKey(perkID);
    }



}
