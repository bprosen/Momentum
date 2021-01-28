package com.parkourcraft.parkour.data.stats;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.clans.Clan;
import com.parkourcraft.parkour.data.levels.LevelObject;
import com.parkourcraft.parkour.data.perks.Perks_DB;
import com.parkourcraft.parkour.storage.mysql.DatabaseQueries;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Stats_DB {

    /*
     * Player Stats Section
     */

    public static void loadPlayerStats(PlayerStats playerStats) {
        loadPlayerID(playerStats);
        loadCompletions(playerStats);
        Perks_DB.loadPerks(playerStats);
    }

    private static void loadPlayerID(PlayerStats playerStats) {
        List<Map<String, String>> playerResults = DatabaseQueries.getResults(
                "players",
                "player_id, player_name, spectatable, clan_id",
                " WHERE uuid='" + playerStats.getUUID() + "'"
        );

        if (playerResults.size() > 0) {
            for (Map<String, String> playerResult : playerResults) {
                playerStats.setPlayerID(Integer.parseInt(playerResult.get("player_id")));

                if (!playerResult.get("player_name").equals(playerStats.getPlayerName()))
                    updatePlayerName(playerStats);

                int spectatable = Integer.parseInt(playerResult.get("spectatable"));
                if (spectatable == 1)
                    playerStats.isSpectatable(true);
                else
                    playerStats.isSpectatable(false);

                int clanID = Integer.parseInt(playerResult.get("clan_id"));

                if (clanID > 0) {
                    Clan clan = Parkour.getClansManager().get(clanID);
                    if (clan != null)
                        playerStats.setClan(clan);
                }
            }
        } else {
            insertPlayerID(playerStats);
            loadPlayerStats(playerStats);
        }
    }

    private static void insertPlayerID(PlayerStats playerStats) {
        String query = "INSERT INTO players " +
                "(uuid, player_name)" +
                " VALUES " +
                "('" +
                playerStats.getUUID() + "', '" +
                playerStats.getPlayerName() +
                "')"
                ;

        Parkour.getDatabaseManager().run(query);
    }

    private static void updatePlayerName(PlayerStats playerStats) {
        String query = "UPDATE players SET " +
                "player_name='" + playerStats.getPlayerName() + "' " +
                "WHERE player_id=" + playerStats.getPlayerID()
                ;

        Parkour.getDatabaseManager().add(query);
    }

    public static void updatePlayerSpectatable(PlayerStats playerStats) {
        int spectatable = 0;

        if (playerStats.isSpectatable())
            spectatable = 1;

        String query = "UPDATE players SET " +
                "spectatable=" + spectatable + " " +
                "WHERE player_id=" + playerStats.getPlayerID()
                ;

        Parkour.getDatabaseManager().add(query);
    }

    /*
     * Completions Section
     */

    private static void loadCompletions(PlayerStats playerStats) {
        List<Map<String, String>> completionsResults = DatabaseQueries.getResults(
                "completions",
                "level.level_name, " +
                        "time_taken, " +
                        "(UNIX_TIMESTAMP(completion_date) * 1000) AS date",
                "JOIN levels level" +
                        " ON level.level_id=completions.level_id" +
                        " WHERE player_id=" + playerStats.getPlayerID()
        );

        for (Map<String, String> completionResult : completionsResults)
            playerStats.levelCompletion(
                    completionResult.get("level_name"),
                    Long.parseLong(completionResult.get("date")),
                    Long.parseLong(completionResult.get("time_taken"))
            );
    }

    public static void insertCompletion(PlayerStats playerStats, LevelObject level, LevelCompletion levelCompletion) {
        Long time = levelCompletion.getCompletionTimeElapsed();

        if (!level.getLeaderboard().isEmpty() &&
            playerStats.getQuickestCompletions(level.getName()).get(0).getCompletionTimeElapsed()
            < levelCompletion.getCompletionTimeElapsed()) {

            time = 0L;
        }
        Parkour.getDatabaseManager().add(
                "INSERT INTO completions " +
                        "(player_id, level_id, time_taken, completion_date)" +
                        " VALUES (" +
                        playerStats.getPlayerID() + ", " +
                        level.getID() + ", " +
                        time + ", " +
                        "FROM_UNIXTIME(" + (levelCompletion.getTimeOfCompletion() / 1000) + ")" +
                        ")"
        );
    }

    /*
     * Leader Board Section
     */

    public static void loadTotalCompletions() {
        List<Map<String, String>> levelsResults = DatabaseQueries.getResults(
                "completions",
                "level.level_name, " +
                        "COUNT(*) AS total_completions",
                "JOIN levels level" +
                        " ON level.level_id=completions.level_id" +
                        " GROUP BY level_name"
        );

        for (Map<String, String> levelResult : levelsResults) {
            LevelObject levelObject = Parkour.getLevelManager().get(levelResult.get("level_name"));

            if (levelObject != null)
                levelObject.setTotalCompletionsCount(Integer.parseInt(levelResult.get("total_completions")));
        }
    }

    public static void loadLeaderboards() {
        for (String levelName : Parkour.getLevelManager().getEnabledLeaderboards()) {
            LevelObject levelObject = Parkour.getLevelManager().get(levelName);
            if (levelObject != null)
                loadLeaderboard(levelObject);
        }
    }

    public static void loadLeaderboard(LevelObject levelObject) {
        List<Map<String, String>> levelsResults = DatabaseQueries.getResults(
                "completions",
                "player.player_name, " +
                        "time_taken, " +
                        "(UNIX_TIMESTAMP(completion_date) * 1000) AS date",
                "JOIN players player" +
                        " on completions.player_id=player.player_id" +
                        " WHERE completions.level_id=" + levelObject.getID() +
                        " AND time_taken > 0" +
                        " ORDER BY time_taken" +
                        " ASC LIMIT 10"

        );

        List<LevelCompletion> levelCompletions = new ArrayList<>();

        for (Map<String, String> levelResult : levelsResults) {
            LevelCompletion levelCompletion = new LevelCompletion(
                    Long.parseLong(levelResult.get("date")),
                    Long.parseLong(levelResult.get("time_taken"))
            );

            levelCompletion.setPlayerName(levelResult.get("player_name"));

            levelCompletions.add(levelCompletion);
        }

        levelObject.setLeaderboardCache(levelCompletions);
    }

    public static void removeCompletion(int levelId, int timeTaken) {

        Parkour.getDatabaseManager().add("DELETE FROM completions WHERE level_id=" + levelId +
                " AND time_taken=" + timeTaken);
    }
}
