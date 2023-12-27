package com.renatusnetwork.parkour.data.stats;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.clans.Clan;
import com.renatusnetwork.parkour.data.clans.ClansManager;
import com.renatusnetwork.parkour.data.infinite.gamemode.InfiniteType;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.modifiers.ModifiersDB;
import com.renatusnetwork.parkour.data.perks.PerksDB;
import com.renatusnetwork.parkour.data.ranks.Rank;
import com.renatusnetwork.parkour.storage.mysql.DatabaseManager;
import com.renatusnetwork.parkour.storage.mysql.DatabaseQueries;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class StatsDB {

    /*
     * Player Stats Section
     */
    public static void loadPlayerStats(PlayerStats playerStats) {
        loadPlayerInformation(playerStats);
        loadCompletions(playerStats);
        loadIndividualLevelsBeaten(playerStats);
        PerksDB.loadPerks(playerStats);
        Parkour.getStatsManager().loadPerksGainedCount(playerStats);
        ModifiersDB.loadModifiers(playerStats);

    }

    private static void loadPlayerInformation(PlayerStats playerStats)
    {

        List<Map<String, String>> playerResults = DatabaseQueries.getResults(
                DatabaseManager.PLAYERS_TABLE,
                "*",
                " WHERE uuid='" + playerStats.getUUID() + "'"
        );

        if (playerResults.isEmpty())
        {
            String query = "INSERT INTO " + DatabaseManager.PLAYERS_TABLE +
                    " (uuid, name)" +
                    " VALUES " +
                    "('" +
                    playerStats.getUUID() + "', '" +
                    playerStats.getPlayerName() +
                    "')"
                    ;

            DatabaseQueries.runQuery(query);

            // reload results
            playerResults = DatabaseQueries.getResults(
                    DatabaseManager.PLAYERS_TABLE,
                    "*",
                    " WHERE uuid='" + playerStats.getUUID() + "'"
            );

        }

        for (Map<String, String> playerResult : playerResults)
        {
            ClansManager clansManager = Parkour.getClansManager();

            String nameInDB = playerResult.get("name");
            Clan clan = clansManager.get(playerResult.get("clan"));

            // update player names
            if (!nameInDB.equals(playerStats.getPlayerName()))
            {
                updatePlayerName(playerStats);

                if (clan != null)
                    // update name in cache
                    Parkour.getClansManager().updatePlayerNameInClan(clan, nameInDB, playerStats.getPlayerName());

                // update in db
                Parkour.getPlotsManager().updatePlayerNameInPlot(nameInDB, playerStats.getPlayerName());
            }

            playerStats.setCoins(Double.parseDouble(playerResult.get("coins")));

            int spectatable = Integer.parseInt(playerResult.get("spectatable"));
            if (spectatable == 1)
                playerStats.setSpectatable(true);
            else
                playerStats.setSpectatable(false);

            if (clan != null)
            {
                playerStats.setClan(clan);

                // notify members of joining
                String memberRole = "Member";
                if (playerStats.getClan().getOwner().getPlayerName().equalsIgnoreCase(playerStats.getPlayerName()))
                    memberRole = "Owner";

                // Bukkit#getPlayer() is async safe :)
                clansManager.sendMessageToMembers(playerStats.getClan(),
                        "&eClan " + memberRole + " &6" + playerStats.getPlayerName() + " &7joined",
                        playerStats.getPlayerName());
            }

            String rankName = playerResult.get("rank");
            Rank rank = Parkour.getRanksManager().get(rankName);
            if (rank != null)
                playerStats.setRank(rank);

            playerStats.setPrestiges(Integer.parseInt(playerResult.get("prestiges")));

            for (InfiniteType type : InfiniteType.values())
            {
                String typeString = "infinite_" + type.toString().toLowerCase() + "_score";
                String scoreString = playerResult.get(typeString);

                // set score
                if (scoreString != null)
                    playerStats.setInfiniteScore(type, Integer.parseInt(scoreString));
            }

            // set total race wins
            int raceWins = Integer.parseInt(playerResult.get("race_wins"));
            playerStats.setRaceWins(raceWins);

            // set total race losses
            int raceLosses = Integer.parseInt(playerResult.get("race_losses"));
            playerStats.setRaceLosses(raceLosses);

            playerStats.setRecords(getNumRecords(playerStats));

            // set night vision, 0 == false, 1 == true
            int nightVision = Integer.parseInt(playerResult.get("night_vision"));
            if (nightVision == 0)
                playerStats.setNVStatus(false);
            else
            {
                playerStats.setNVStatus(true);

                // run sync potion add
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        playerStats.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0));
                    }
                }.runTask(Parkour.getPlugin());
            }

            // get total count of how many levels they've rated
            int ratedLevelsCount = Integer.parseInt(
                    DatabaseQueries.getResults(DatabaseManager.LEVEL_RATINGS_TABLE, "COUNT(*)",
                            " WHERE player_name='" + playerStats.getPlayerName() + "'")
                            .get(0).get("COUNT(*)"));

            playerStats.setRatedLevelsCount(ratedLevelsCount);

            // set race win rate
            if (raceLosses > 0)
                playerStats.setRaceWinRate(Float.parseFloat(Utils.formatDecimal((double) raceWins / raceLosses)));
            else
                playerStats.setRaceWinRate(raceWins);

            // set multiplier percentage
            if (playerStats.getPrestiges() > 0)
            {
                float prestigeMultiplier = Parkour.getSettingsManager().prestige_multiplier_per_prestige * playerStats.getPrestiges();

                if (prestigeMultiplier >= Parkour.getSettingsManager().max_prestige_multiplier)
                    prestigeMultiplier = Parkour.getSettingsManager().max_prestige_multiplier;

                prestigeMultiplier = (float) (1.00 + (prestigeMultiplier / 100));

                playerStats.setPrestigeMultiplier(prestigeMultiplier);
            }

            // Set to true if 1 (true)
            if (Integer.parseInt(playerResult.get("grinding")) == 1)
                playerStats.toggleGrinding();

            int eventWins = Integer.parseInt(playerResult.get("event_wins"));
            playerStats.setEventWins(eventWins);

            String infiniteBlock = playerResult.get("infinite_block");

            // only set it if its non null
            if (infiniteBlock != null)
                playerStats.setInfiniteBlock(Material.matchMaterial(infiniteBlock));
            else
                // default is quartz block otherwise
                playerStats.setInfiniteBlock(Parkour.getSettingsManager().infinite_default_block);

            String infiniteType = playerResult.get("infinite_type");
            if (infiniteType != null)
                playerStats.setInfiniteType(InfiniteType.valueOf(infiniteType.toUpperCase()));
            else
                playerStats.setInfiniteType(Parkour.getSettingsManager().infinite_default_type);

            // set fail mode, 0 == false, 1 == true
            int failsToggled = Integer.parseInt(playerResult.get("fail_mode"));
            if (failsToggled == 0)
                playerStats.setFailMode(false);
            else
                playerStats.setFailMode(true);

            int attemptingRankup = Integer.parseInt(playerResult.get("attempting_rankup"));
            if (attemptingRankup == 0)
                playerStats.setAttemptingRankup(false);
            else
                playerStats.setAttemptingRankup(true);

            loadBoughtLevels(playerStats);
        }
    }

    private static void loadIndividualLevelsBeaten(PlayerStats playerStats)
    {
        // get individual levels beaten by looping through list
        int individualLevelsBeaten = 0;
        for (Level level : Parkour.getLevelManager().getLevels().values())
            if (playerStats.hasCompleted(level.getName()))
                individualLevelsBeaten++;

        playerStats.setIndividualLevelsBeaten(individualLevelsBeaten);
    }

    public static int getTotalPlayers()
    {
        List<Map<String, String>> results = DatabaseQueries.getResults(DatabaseManager.PLAYERS_TABLE, "COUNT(*) AS total", "");

        return Integer.parseInt(results.get(0).get("total"));
    }

    // this will update the player name all across the database
    private static void updatePlayerName(PlayerStats playerStats) {
        // update in stats
        DatabaseQueries.runAsyncQuery("UPDATE "  + DatabaseManager.PLAYERS_TABLE + " SET " +
                "name='" + playerStats.getPlayerName() + "' WHERE uuid=" + playerStats.getUUID());
    }

    public static void updatePlayerSpectatable(PlayerStats playerStats)
    {
        int spectatable = playerStats.isSpectatable() ? 1 : 0;

        String query = "UPDATE " + DatabaseManager.PLAYERS_TABLE + " SET " +
                "spectatable=" + spectatable + " " +
                "WHERE uuid='" + playerStats.getUUID() + "'";

        DatabaseQueries.runAsyncQuery(query);
    }

    public static void updatePlayerNightVision(PlayerStats playerStats)
    {
        int vision = playerStats.hasNVStatus() ? 1 : 0;

        String query = "UPDATE " + DatabaseManager.PLAYERS_TABLE + " SET " +
                "night_vision=" + vision + " " +
                "WHERE uuid='" + playerStats.getUUID() + "'";

        DatabaseQueries.runAsyncQuery(query);
    }

    public static void updatePlayerGrinding(PlayerStats playerStats)
    {
        int grinding = playerStats.isGrinding() ? 1 : 0;

        String query = "UPDATE " + DatabaseManager.PLAYERS_TABLE + " SET " +
                "grinding=" + grinding + " WHERE uuid='" + playerStats.getUUID() + "'";

        DatabaseQueries.runAsyncQuery(query);
    }

    public static void updateCoins(String uuid, double coins)
    {
        if (coins < 0)
            coins = 0;

        String query = "UPDATE " + DatabaseManager.PLAYERS_TABLE + " SET coins=" + coins + " WHERE uuid='" + uuid + "'";
        DatabaseQueries.runAsyncQuery(query);
    }

    public static void updateCoinsName(String playerName, double coins)
    {
        if (coins < 0)
            coins = 0;

        String query = "UPDATE " + DatabaseManager.PLAYERS_TABLE + " SET coins=" + coins + " WHERE name='" + playerName + "'";
        DatabaseQueries.runAsyncQuery(query);
    }

    public static void updateInfiniteType(PlayerStats playerStats, InfiniteType newType)
    {
        String query = "UPDATE " + DatabaseManager.PLAYERS_TABLE + " SET infinite_type='" + newType.toString().toLowerCase() + "' WHERE uuid='" + playerStats.getUUID() + "'";
        DatabaseQueries.runAsyncQuery(query);
    }

    public static void updateRank(String uuid, String rank)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.PLAYERS_TABLE + " SET rank=? WHERE uuid=?", rank, uuid);
    }

    public static double getCoinsFromName(String playerName)
    {
        double coins = 0;

        List<Map<String, String>> playerResults = DatabaseQueries.getResults(
                DatabaseManager.PLAYERS_TABLE,
                "coins",
                " WHERE name=?", playerName
        );

        for (Map<String, String> playerResult : playerResults)
            coins = Double.parseDouble(playerResult.get("coins"));

        return coins;
    }

    public static double getCoinsFromUUID(String UUID)
    {
        double coins = 0;

        List<Map<String, String>> playerResults = DatabaseQueries.getResults(
                DatabaseManager.PLAYERS_TABLE,
                "coins",
                " WHERE uuid='" + UUID + "'"
        );

        for (Map<String, String> playerResult : playerResults)
            coins = Double.parseDouble(playerResult.get("coins"));

        return coins;
    }

    public static boolean isPlayerInDatabase(String playerName) {

        List<Map<String, String>> playerResults = DatabaseQueries.getResults(
                DatabaseManager.PLAYERS_TABLE,
                "uuid",
                " WHERE name=?", playerName
        );

        return !playerResults.isEmpty();
    }

    public static void loadBoughtLevels(PlayerStats playerStats)
    {
        if (playerStats != null)
        {
            HashSet<String> boughtLevels = new HashSet<>();

            List<Map<String, String>> purchasesResults = DatabaseQueries.getResults(
                    DatabaseManager.LEVEL_PURCHASES_TABLE,
                    "level_name",
                    "WHERE uuid='" + playerStats.getUUID() + "'"
            );

            for (Map<String, String> boughtResult : purchasesResults)
                boughtLevels.add(boughtResult.get("level_name"));

            playerStats.setBoughtLevels(boughtLevels);
        }
    }

    public static void addBoughtLevel(PlayerStats playerStats, String boughtLevel)
    {
        String query = "INSERT INTO " + DatabaseManager.LEVEL_PURCHASES_TABLE + " (uuid, level_name)" +
                " VALUES " +
                "('" + playerStats.getUUID() + "', '" + boughtLevel + "')";

        DatabaseQueries.runAsyncQuery(query);
    }

    public static void removeBoughtLevel(String uuid, String boughtLevel)
    {
        String query = "DELETE FROM " + DatabaseManager.LEVEL_PURCHASES_TABLE + " WHERE uuid=? AND level_name=?";

        DatabaseQueries.runAsyncQuery(query, uuid, boughtLevel);
    }

    public static boolean hasBoughtLevel(String uuid, String boughtLevel)
    {
        List<Map<String, String>> playerResults = DatabaseQueries.getResults(
                DatabaseManager.LEVEL_PURCHASES_TABLE,
                "uuid",
                " WHERE uuid=? AND level_name=?", uuid, boughtLevel
        );

        return !playerResults.isEmpty();
    }

    /*
     * Completions Section
     */
    private static void loadCompletions(PlayerStats playerStats)
    {
        List<Map<String, String>> completionsResults = DatabaseQueries.getResults(
                DatabaseManager.LEVEL_COMPLETIONS_TABLE,
                "*",
                "WHERE uuid='" + playerStats.getUUID() + "'");

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

    public static void insertCompletion(PlayerStats playerStats, Level level, LevelCompletion levelCompletion) {

        DatabaseQueries.runAsyncQuery(
                "INSERT INTO " + DatabaseManager.LEVEL_COMPLETIONS_TABLE +
                    " (uuid, level_name, completion_date, time_taken)" +
                    " VALUES ('" +
                    playerStats.getUUID() + "', '" +
                    level.getName() + "', " +
                    "FROM_UNIXTIME(" + levelCompletion.getTimeOfCompletionSeconds() + "), " +
                    levelCompletion.getCompletionTimeElapsed() +
                    ")"
        );
    }

    public static void removeCompletions(String uuid, String levelName) {

        String query = "DELETE FROM " + DatabaseManager.LEVEL_COMPLETIONS_TABLE + " WHERE uuid=? AND level_name=?";

        DatabaseQueries.runAsyncQuery(query, uuid, levelName);
    }

    public static boolean hasCompleted(String uuid, String levelName) {

        List<Map<String, String>> playerResults = DatabaseQueries.getResults(DatabaseManager.LEVEL_COMPLETIONS_TABLE, "*",
                                        " WHERE uuid=? AND level_name=?", uuid, levelName);

        return !playerResults.isEmpty();
    }

    public static int getNumRecords(PlayerStats playerStats)
    {
        List<Map<String, String>> recordResults = DatabaseQueries.getResults(DatabaseManager.LEVEL_RECORDS_TABLE,
                "COUNT(level_name) AS num_records",
                        "WHERE uuid='" + playerStats.getUUID() + "' GROUP BY uuid");

        for (Map<String, String> recordResult : recordResults)
            return Integer.parseInt(recordResult.get("num_records"));

        return 0;
    }

    public static int getNumRecordsFromName(String name)
    {
        List<Map<String, String>> recordResults = DatabaseQueries.getResults(DatabaseManager.LEVEL_RECORDS_TABLE + " lr",
                "COUNT(level_name) AS num_records",
                "JOIN " + DatabaseManager.PLAYERS_TABLE + " p ON p.uuid=lr.uuid " +
                        "WHERE p.name=? GROUP BY p.name", name);

        for (Map<String, String> recordResult : recordResults)
            return Integer.parseInt(recordResult.get("num_records"));

        return 0;
    }

    /*
     * Leader Board Section
     */
    public static void loadTotalCompletions()
    {
        List<Map<String, String>> levelsResults = DatabaseQueries.getResults(
                DatabaseManager.LEVEL_COMPLETIONS_TABLE,
                "level_name, " +
                        "COUNT(*) AS total_completions",
                        "GROUP BY level_name"
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
                    DatabaseManager.LEVEL_COMPLETIONS_TABLE,
                    "COUNT(*) AS total_completions",
                    "WHERE level_name='" + level.getName() + "'"
            );

            for (Map<String, String> levelResult : levelsResults)
                level.setTotalCompletionsCount(Integer.parseInt(levelResult.get("total_completions")));
        }
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
                            currentLevel.setLeaderboardCache(currentLB);

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
                    currentLevel.setLeaderboardCache(currentLB);
            }
            catch (SQLException exception)
            {
                exception.printStackTrace();
            }
        }
        Parkour.getStatsManager().toggleLoadingLeaderboards(false);
    }

    public static void loadLeaderboard(Level level) {
        // Artificial limit of 500
        List<Map<String, String>> levelsResults = DatabaseQueries.getResults(
                DatabaseManager.LEVEL_COMPLETIONS_TABLE + " c",
                "p.uuid AS player_uuid, p.name AS player_name, " +
                        "time_taken, " +
                        "(UNIX_TIMESTAMP(completion_date) * 1000) AS date",
                "JOIN " + DatabaseManager.PLAYERS_TABLE + " p " +
                        "ON c.player_id=p.player_id " +
                        "WHERE c.level_name=" + level.getName() + " " +
                        "AND time_taken > 0 " +
                        "ORDER BY time_taken " +
                        "ASC LIMIT 500"
        );

        List<LevelCompletion> levelCompletions = new ArrayList<>();
        Set<String> addedPlayers = new HashSet<>(); // used to avoid duplicate positions

        for (Map<String, String> levelResult : levelsResults) {

            if (levelCompletions.size() >= 10)
                break;

            String playerName = levelResult.get("player_name");

            // if not added already, add to leaderboard
            if (!addedPlayers.contains(playerName)) {
                LevelCompletion levelCompletion = new LevelCompletion(
                        level.getName(),
                        levelResult.get("player_uuid"),
                        playerName,
                        Long.parseLong(levelResult.get("date")),
                        Long.parseLong(levelResult.get("time_taken"))
                );
                levelCompletions.add(levelCompletion);
                addedPlayers.add(playerName);
            }
        }
        level.setLeaderboardCache(levelCompletions);
    }
}
