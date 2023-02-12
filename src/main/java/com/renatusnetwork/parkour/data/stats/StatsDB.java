package com.renatusnetwork.parkour.data.stats;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.clans.Clan;
import com.renatusnetwork.parkour.data.clans.ClansManager;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.perks.PerksDB;
import com.renatusnetwork.parkour.data.ranks.Rank;
import com.renatusnetwork.parkour.storage.mysql.DatabaseQueries;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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
        Parkour.getStatsManager().loadPerksGainedCount(playerStats);
    }

    private static void loadPlayerID(PlayerStats playerStats) {

        List<Map<String, String>> playerResults = DatabaseQueries.getResults(
                "players",
                "player_id, player_name, coins, spectatable, clan_id, rank_id, rankup_stage, rank_prestiges, infinitepk_score, level_completions, race_wins, race_losses, night_vision, grinding, records",
                " WHERE uuid='" + playerStats.getUUID() + "'"
        );

        if (playerResults.size() > 0) {
            for (Map<String, String> playerResult : playerResults) {
                ClansManager clansManager = Parkour.getClansManager();

                playerStats.setPlayerID(Integer.parseInt(playerResult.get("player_id")));
                int clanID = Integer.parseInt(playerResult.get("clan_id"));
                String nameInDB = playerResult.get("player_name");

                // update player names
                if (!nameInDB.equals(playerStats.getPlayerName())) {
                    updatePlayerName(playerStats, nameInDB);

                    if (clanID > 0) {
                        Clan clan = clansManager.get(clanID);
                        // update name in cache
                        Parkour.getClansManager().updatePlayerNameInClan(clan, nameInDB, playerStats.getPlayerName());
                    }
                    // update in db
                    Parkour.getPlotsManager().updatePlayerNameInPlot(nameInDB, playerStats.getPlayerName());
                }

                double coins = Double.parseDouble(playerResult.get("coins"));
                playerStats.setCoins(coins);

                int spectatable = Integer.parseInt(playerResult.get("spectatable"));
                if (spectatable == 1)
                    playerStats.setSpectatable(true);
                else
                    playerStats.setSpectatable(false);

                if (clanID > 0) {
                    Clan clan = clansManager.get(clanID);

                    if (clan != null) {
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

                // set total race wins
                int raceWins = Integer.parseInt(playerResult.get("race_wins"));
                playerStats.setRaceWins(raceWins);

                // set total race losses
                int raceLosses = Integer.parseInt(playerResult.get("race_losses"));
                playerStats.setRaceLosses(raceLosses);

                // set night vision, 0 == false, 1 == true
                int nightVision = Integer.parseInt(playerResult.get("night_vision"));
                if (nightVision == 0)
                    playerStats.setNVStatus(false);
                else {
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
                        DatabaseQueries.getResults("ratings", "COUNT(*)",
                                " WHERE player_name='" + playerStats.getPlayerName() + "'")
                                .get(0).get("COUNT(*)"));
                playerStats.setRatedLevelsCount(ratedLevelsCount);

                // set race win rate
                if (raceLosses > 0)
                    playerStats.setRaceWinRate(Float.parseFloat(Utils.formatDecimal((double) raceWins / raceLosses)));
                else
                    playerStats.setRaceWinRate(raceWins);

                // set multiplier percentage
                if (playerStats.getPrestiges() > 0) {
                    float prestigeMultiplier = Parkour.getSettingsManager().prestige_multiplier_per_prestige * playerStats.getPrestiges();

                    if (prestigeMultiplier >= Parkour.getSettingsManager().max_prestige_multiplier)
                        prestigeMultiplier = Parkour.getSettingsManager().max_prestige_multiplier;

                    prestigeMultiplier = (float) (1.00 + (prestigeMultiplier / 100));

                    playerStats.setPrestigeMultiplier(prestigeMultiplier);
                }

                // Set to true if 1 (true)
                if (Integer.parseInt(playerResult.get("grinding")) == 1)
                    playerStats.toggleGrinding();

                // set records
                int records = Integer.parseInt(playerResult.get("records"));
                playerStats.setRecords(records);
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

    // this will update the player name all across the database
    private static void updatePlayerName(PlayerStats playerStats, String oldName) {
        // update in stats
        Parkour.getDatabaseManager().asyncRun("UPDATE players SET " +
                "player_name='" + playerStats.getPlayerName() + "' " +
                "WHERE player_id=" + playerStats.getPlayerID());

        // update in checkpoints
        Parkour.getDatabaseManager().asyncRun("UPDATE checkpoints SET " +
                "player_name='" + playerStats.getPlayerName() + "' " +
                "WHERE player_name='" + oldName + "'");

        // update in ratings
        Parkour.getDatabaseManager().asyncRun("UPDATE ratings SET " +
                "player_name='" + playerStats.getPlayerName() + "' " +
                "WHERE player_name='" + oldName + "'");

        // update in plots
        Parkour.getDatabaseManager().asyncRun("UPDATE plots SET " +
                "player_name='" + playerStats.getPlayerName() + "' " +
                "WHERE player_name='" + oldName + "'");
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

    public static void updatePlayerNightVision(PlayerStats playerStats) {
        int vision = 0;

        if (playerStats.hasNVStatus())
            vision = 1;

        String query = "UPDATE players SET " +
                "night_vision=" + vision + " " +
                "WHERE player_id=" + playerStats.getPlayerID()
                ;

        Parkour.getDatabaseManager().add(query);
    }

    public static void updatePlayerGrinding(PlayerStats playerStats)
    {
        int grinding = 0;

        if (playerStats.isGrinding())
            grinding = 1;

        String query = "UPDATE players SET " +
                "grinding=" + grinding + " WHERE player_id=" + playerStats.getPlayerID();

        Parkour.getDatabaseManager().add(query);
    }

    public static void updateCoins(PlayerStats playerStats, double coins)
    {
        if (coins < 0)
            coins = 0;

        String query = "UPDATE players SET coins=" + coins + " WHERE player_id=" + playerStats.getPlayerID();
        Parkour.getDatabaseManager().add(query);
    }

    public static void updateCoinsUUID(String UUID, double coins)
    {
        if (coins < 0)
            coins = 0;

        String query = "UPDATE players SET coins=" + coins + " WHERE uuid='" + UUID + "'";
        Parkour.getDatabaseManager().add(query);
    }

    public static void updateCoinsName(String playerName, double coins)
    {
        if (coins < 0)
            coins = 0;

        String query = "UPDATE players SET coins=" + coins + " WHERE player_name='" + playerName + "'";
        Parkour.getDatabaseManager().add(query);
    }

    public static void updateRecordsName(String playerName, int records)
    {
        if (records < 0)
            records = 0;

        String query = "UPDATE players SET records=" + records + " WHERE player_name='" + playerName + "'";
        Parkour.getDatabaseManager().add(query);
    }

    public static void addRecordsName(String playerName)
    {
        int records = StatsDB.getRecordsFromName(playerName);
        updateRecordsName(playerName, records + 1);
    }

    public static void removeRecordsName(String playerName)
    {
        int records = StatsDB.getRecordsFromName(playerName);
        updateRecordsName(playerName, records - 1);
    }
    public static int getRecordsFromName(String playerName)
    {
        int records = 0;

        List<Map<String, String>> playerResults = DatabaseQueries.getResults(
                "players",
                "records",
                " WHERE player_name='" + playerName + "'"
        );

        for (Map<String, String> playerResult : playerResults)
            records = Integer.parseInt(playerResult.get("records"));

        return records;
    }

    public static double getCoinsFromName(String playerName)
    {
        double coins = 0;

        List<Map<String, String>> playerResults = DatabaseQueries.getResults(
                "players",
                "coins",
                " WHERE player_name='" + playerName + "'"
        );

        for (Map<String, String> playerResult : playerResults)
            coins = Double.parseDouble(playerResult.get("coins"));

        return coins;
    }

    public static double getCoinsFromUUID(String UUID)
    {
        double coins = 0;

        List<Map<String, String>> playerResults = DatabaseQueries.getResults(
                "players",
                "coins",
                " WHERE uuid='" + UUID + "'"
        );

        for (Map<String, String> playerResult : playerResults)
            coins = Double.parseDouble(playerResult.get("coins"));

        return coins;
    }

    public static boolean isPlayerInDatabase(String playerName) {

        List<Map<String, String>> playerResults = DatabaseQueries.getResults(
                "players",
                "uuid",
                " WHERE player_name='" + playerName + "'"
        );

        return !playerResults.isEmpty();
    }

    public static int getPlayerID(String playerName) {

        int playerID = -1;

        List<Map<String, String>> playerResults = DatabaseQueries.getResults(
                "players",
                "player_id",
                " WHERE player_name='" + playerName + "'"
        );

        for (Map<String, String> playerResult : playerResults)
            playerID = Integer.parseInt(playerResult.get("player_id"));

        return playerID;
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

        // get individual levels beaten by looping through list
        int individualLevelsBeaten = 0;
        for (Level level : Parkour.getLevelManager().getLevels().values())
            if (playerStats.getLevelCompletionsCount(level.getName()) > 0)
                individualLevelsBeaten++;

        playerStats.setIndividualLevelsBeaten(individualLevelsBeaten);
    }

    public static void insertCompletion(PlayerStats playerStats, Level level, LevelCompletion levelCompletion) {

        Parkour.getDatabaseManager().asyncRun(
                "INSERT INTO completions " +
                    "(player_id, level_id, time_taken, completion_date)" +
                    " VALUES (" +
                    playerStats.getPlayerID() + ", " +
                    level.getID() + ", " +
                    levelCompletion.getCompletionTimeElapsed() + ", " +
                    "FROM_UNIXTIME(" + (levelCompletion.getTimeOfCompletion() / 1000) + ")" +
                    ")"
        );

        Parkour.getDatabaseManager().asyncRun("UPDATE players SET level_completions=" + playerStats.getTotalLevelCompletions() +
                                                  " WHERE player_name='" + playerStats.getPlayerName() + "'");
    }

    public static int getTotalCompletions(String playerName) {

        List<Map<String, String>> playerResults = DatabaseQueries.getResults("players", "level_completions", " WHERE player_name='" + playerName + "'");

        for (Map<String, String> playerResult : playerResults)
            return Integer.parseInt(playerResult.get("level_completions"));

        return -1;
    }

    public static void removeCompletions(int playerID, int levelID) {

        String query = "DELETE FROM completions WHERE player_id=" + playerID + " AND level_id=" + levelID;

        Parkour.getDatabaseManager().add(query);
    }

    public static boolean hasCompleted(int playerID, int levelID) {

        List<Map<String, String>> playerResults = DatabaseQueries.getResults("completions", "*",
                                        " WHERE player_id=" + playerID + " AND level_id=" + levelID);

        return !playerResults.isEmpty();
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

    public static void loadLeaderboards()
    {
        Parkour.getStatsManager().toggleLoadingLeaderboards(true);

        for (Level level : Parkour.getLevelManager().getLevels().values())
            if (level != null)
                loadLeaderboard(level);

        syncRecords();
        Parkour.getStatsManager().toggleLoadingLeaderboards(false);
    }

    public static void syncRecords()
    {
        HashMap<String, Integer> recordsMap = new HashMap<>();

        for (Level level : Parkour.getLevelManager().getLevels().values())
        {
            if (!level.getLeaderboard().isEmpty())
            {
                String recordHolder = level.getLeaderboard().get(0).getPlayerName();

                // add to map
                if (recordsMap.containsKey(recordHolder))
                    recordsMap.replace(recordHolder, recordsMap.get(recordHolder) + 1);
                else
                    recordsMap.put(recordHolder, 1);

            }
        }

        if (!recordsMap.isEmpty())
        {
            // reset
            Parkour.getDatabaseManager().run("UPDATE players SET records=0");

            for (Map.Entry<String, Integer> entry : recordsMap.entrySet())
            {
                PlayerStats playerStats = Parkour.getStatsManager().getByName(entry.getKey());

                // if not null, use stats manager
                if (playerStats != null)
                    playerStats.setRecords(entry.getValue());

                StatsDB.updateRecordsName(entry.getKey(), entry.getValue());
            }
            Parkour.getPluginLogger().info("Synced " + recordsMap.size() + " level records");
        }
    }

    public static void loadLeaderboard(Level level) {

        List<Map<String, String>> levelsResults = DatabaseQueries.getResults(
                "completions",
                "player.player_name, " +
                        "MIN(time_taken) AS fastest_time, " +
                        "(UNIX_TIMESTAMP(completion_date) * 1000) AS date",
                "JOIN players player" +
                        " on completions.player_id=player.player_id" +
                        " WHERE completions.level_id=" + level.getID() +
                        " AND fastest_time > 0" +
                        " GROUP BY completions.player_id" +
                        " ORDER BY fastest_time" +
                        " ASC LIMIT 10"
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
                        Long.parseLong(levelResult.get("date")),
                        Long.parseLong(levelResult.get("fastest_time"))
                );

                levelCompletion.setPlayerName(playerName);
                levelCompletions.add(levelCompletion);
                addedPlayers.add(playerName);
            }
        }
        level.setLeaderboardCache(levelCompletions);
    }
}
