package com.parkourcraft.parkour.data.stats;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.clans.Clan;
import com.parkourcraft.parkour.data.levels.Level;
import com.parkourcraft.parkour.data.perks.PerksDB;
import com.parkourcraft.parkour.data.rank.Rank;
import com.parkourcraft.parkour.storage.mysql.DatabaseQueries;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class StatsDB {

    /*
     * Player Stats Section
     */

    public static void loadPlayerStats(PlayerStats playerStats) {
        loadPlayerID(playerStats);
        loadCompletions(playerStats);
        PerksDB.loadPerks(playerStats);
    }

    private static void loadPlayerID(PlayerStats playerStats) {

        List<Map<String, String>> playerResults = DatabaseQueries.getResults(
                "players",
                "player_id, player_name, spectatable, clan_id, rank_id, rankup_stage, rank_prestiges, infinitepk_score, level_completions",
                " WHERE uuid='" + playerStats.getUUID() + "'"
        );

        if (playerResults.size() > 0) {
            for (Map<String, String> playerResult : playerResults) {
                playerStats.setPlayerID(Integer.parseInt(playerResult.get("player_id")));

                if (!playerResult.get("player_name").equals(playerStats.getPlayerName()))
                    updatePlayerName(playerStats);

                int spectatable = Integer.parseInt(playerResult.get("spectatable"));
                if (spectatable == 1)
                    playerStats.setSpectatable(true);
                else
                    playerStats.setSpectatable(false);

                int clanID = Integer.parseInt(playerResult.get("clan_id"));

                if (clanID > 0) {
                    Clan clan = Parkour.getClansManager().get(clanID);
                    if (clan != null)
                        playerStats.setClan(clan);
                }

                int rankID = Integer.parseInt(playerResult.get("rank_id"));
                Rank rank = Parkour.getRanksManager().get(rankID);
                if (rank != null)
                    playerStats.setRank(rank);

                int prestiges = Integer.parseInt(playerResult.get("rank_prestiges"));
                playerStats.setPrestiges(prestiges);

                // add +1 so it is normal stage 1/2 out of database
                int rankUpStage = Integer.parseInt(playerResult.get("rankup_stage")) + 1;
                playerStats.setRankUpStage(rankUpStage);

                int infinitePKScore = Integer.parseInt(playerResult.get("infinitepk_score"));
                playerStats.setInfinitePKScore(infinitePKScore);

                // set total completions count
                int completions = Integer.parseInt(playerResult.get("level_completions"));
                playerStats.setTotalLevelCompletions(completions);
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

    public static boolean isPlayerInDatabase(String playerName) {

        List<Map<String, String>> playerResults = DatabaseQueries.getResults(
                "players",
                "uuid",
                " WHERE player_name='" + playerName + "'"
        );

        if (!playerResults.isEmpty())
            return true;
        return false;
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

    public static void insertCompletion(PlayerStats playerStats, Level level, LevelCompletion levelCompletion) {

        Parkour.getDatabaseManager().add(
                "INSERT INTO completions " +
                        "(player_id, level_id, time_taken, completion_date)" +
                        " VALUES (" +
                        playerStats.getPlayerID() + ", " +
                        level.getID() + ", " +
                        levelCompletion.getCompletionTimeElapsed() + ", " +
                        "FROM_UNIXTIME(" + (levelCompletion.getTimeOfCompletion() / 1000) + ")" +
                        ")"
        );

        int totalCompletions = getTotalCompletions(playerStats.getPlayerName());
        if (totalCompletions > -1)
            Parkour.getDatabaseManager().add("UPDATE players SET level_completions=" + (totalCompletions + 1) + " WHERE player_name='" + playerStats.getPlayerName() + "'");
    }

    public static int getTotalCompletions(String playerName) {

        List<Map<String, String>> playerResults = DatabaseQueries.getResults("players", "level_completions", " WHERE player_name='" + playerName + "'");

        for (Map<String, String> playerResult : playerResults)
            return Integer.parseInt(playerResult.get("level_completions"));

        return -1;
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
            Level level = Parkour.getLevelManager().get(levelResult.get("level_name"));

            if (level != null)
                level.setTotalCompletionsCount(Integer.parseInt(levelResult.get("total_completions")));
        }
    }

    // can run complete async, only when all levels are loaded
    public static void loadTotalCompletions(Level level) {

        if (level != null) {
            List<Map<String, String>> levelsResults = DatabaseQueries.getResults(
                    "completions",
                    "COUNT(*) AS total_completions",
                    " WHERE level_id='" + level.getID() + "'"
            );

            for (Map<String, String> levelResult : levelsResults)
                level.setTotalCompletionsCount(Integer.parseInt(levelResult.get("total_completions")));
        }
    }

    public static void loadLeaderboards() {
        for (Map.Entry<String, Level> entry : Parkour.getLevelManager().getLevels().entrySet()) {
            if (entry.getValue() != null)
                loadLeaderboard(entry.getValue());
        }
    }

    public static void loadLeaderboard(Level level) {
        // Artificial limit of 500
        List<Map<String, String>> levelsResults = DatabaseQueries.getResults(
                "completions",
                "player.player_name, " +
                        "time_taken, " +
                        "(UNIX_TIMESTAMP(completion_date) * 1000) AS date",
                "JOIN players player" +
                        " on completions.player_id=player.player_id" +
                        " WHERE completions.level_id=" + level.getID() +
                        " AND time_taken > 0" +
                        " ORDER BY time_taken" +
                        " ASC LIMIT 500"

        );

        List<LevelCompletion> levelCompletions = new ArrayList<>();

        for (Map<String, String> levelResult : levelsResults) {

            if (levelCompletions.size() >= 10)
                break;

            LevelCompletion levelCompletion = new LevelCompletion(
                    Long.parseLong(levelResult.get("date")),
                    Long.parseLong(levelResult.get("time_taken"))
            );

            levelCompletion.setPlayerName(levelResult.get("player_name"));
            levelCompletions.add(levelCompletion);

            for (int outer = 0; outer < levelCompletions.size(); outer++)
                for (int inner = outer + 1; inner < levelCompletions.size(); inner++)
                    if (levelCompletions.get(inner).getPlayerName().equalsIgnoreCase(
                        levelCompletions.get(outer).getPlayerName()))
                        levelCompletions.remove(levelCompletion);
        }
        level.setLeaderboardCache(levelCompletions);
    }

    public static long getPersonalGlobalCompletions(int playerID) {
        List<Map<String, String>> globalResults = DatabaseQueries.getResults("players",
                "completions", " WHERE player_id=" + playerID);

        return Integer.parseInt(globalResults.get(0).get("completions"));
    }
}
