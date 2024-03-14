package com.renatusnetwork.momentum.data.stats;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.clans.Clan;
import com.renatusnetwork.momentum.data.clans.ClansManager;
import com.renatusnetwork.momentum.data.levels.Level;
import com.renatusnetwork.momentum.data.perks.PerksDB;
import com.renatusnetwork.momentum.data.ranks.Rank;
import com.renatusnetwork.momentum.storage.mysql.DatabaseQueries;
import com.renatusnetwork.momentum.utils.Utils;
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
        loadPlayerID(playerStats);
        loadCompletions(playerStats);
        PerksDB.loadPerks(playerStats);
        Momentum.getStatsManager().loadPerksGainedCount(playerStats);
    }

    private static void loadPlayerID(PlayerStats playerStats) {

        List<Map<String, String>> playerResults = DatabaseQueries.getResults(
                "players",
                "player_id, player_name, coins, spectatable, clan_id, rank_id, rankup_stage, rank_prestiges, infinitepk_score, level_completions, race_wins, race_losses, night_vision, grinding, records, event_wins, infinite_block, fail_mode",
                " WHERE uuid='" + playerStats.getUUID() + "'"
        );

        if (playerResults.size() > 0) {
            for (Map<String, String> playerResult : playerResults) {
                ClansManager clansManager = Momentum.getClansManager();

                playerStats.setPlayerID(Integer.parseInt(playerResult.get("player_id")));
                int clanID = Integer.parseInt(playerResult.get("clan_id"));
                String nameInDB = playerResult.get("player_name");

                // update player names
                if (!nameInDB.equals(playerStats.getPlayerName())) {
                    updatePlayerName(playerStats, nameInDB);

                    if (clanID > 0) {
                        Clan clan = clansManager.get(clanID);
                        // update name in cache
                        Momentum.getClansManager().updatePlayerNameInClan(clan, nameInDB, playerStats.getPlayerName());
                    }
                    // update in db
                    Momentum.getPlotsManager().updatePlayerNameInPlot(nameInDB, playerStats.getPlayerName());
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
                Rank rank = Momentum.getRanksManager().get(rankID);
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
                    }.runTask(Momentum.getPlugin());
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
                    float prestigeMultiplier = Momentum.getSettingsManager().prestige_multiplier_per_prestige * playerStats.getPrestiges();

                    if (prestigeMultiplier >= Momentum.getSettingsManager().max_prestige_multiplier)
                        prestigeMultiplier = Momentum.getSettingsManager().max_prestige_multiplier;

                    prestigeMultiplier = (float) (1.00 + (prestigeMultiplier / 100));

                    playerStats.setPrestigeMultiplier(prestigeMultiplier);
                }

                // Set to true if 1 (true)
                if (Integer.parseInt(playerResult.get("grinding")) == 1)
                    playerStats.toggleGrinding();

                // set records
                int records = Integer.parseInt(playerResult.get("records"));
                playerStats.setRecords(records);

                int eventWins = Integer.parseInt(playerResult.get("event_wins"));
                playerStats.setEventWins(eventWins);

                String infiniteBlock = playerResult.get("infinite_block");

                // only set it if its non null or ""
                if (infiniteBlock != null && !infiniteBlock.equals(""))
                    playerStats.setInfiniteBlock(Material.matchMaterial(infiniteBlock));
                else
                    // default is quartz block otherwise
                    playerStats.setInfiniteBlock(Material.QUARTZ_BLOCK);

                // set fail mode, 0 == false, 1 == true
                int failsToggled = Integer.parseInt(playerResult.get("fail_mode"));
                if (failsToggled == 0)
                    playerStats.setFailMode(false);
                else
                    playerStats.setFailMode(true);

                updateBoughtLevels(playerStats);
            }
        } else {
            insertPlayerID(playerStats);
            loadPlayerStats(playerStats);
        }
    }

    public static int getTotalPlayers()
    {
        int total;

        List<Map<String, String>> results = DatabaseQueries.getResults("players", "COUNT(*) AS total", "");
        total = Integer.parseInt(results.get(0).get("total"));

        return total;
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

        Momentum.getDatabaseManager().runQuery(query);
    }

    // this will update the player name all across the database
    private static void updatePlayerName(PlayerStats playerStats, String oldName) {
        // update in stats
        Momentum.getDatabaseManager().runAsyncQuery("UPDATE players SET " +
                "player_name='" + playerStats.getPlayerName() + "' " +
                "WHERE player_id=" + playerStats.getPlayerID());

        // update in checkpoints
        Momentum.getDatabaseManager().runAsyncQuery("UPDATE checkpoints SET " +
                "player_name='" + playerStats.getPlayerName() + "' " +
                "WHERE player_name='" + oldName + "'");

        // update in ratings
        Momentum.getDatabaseManager().runAsyncQuery("UPDATE ratings SET " +
                "player_name='" + playerStats.getPlayerName() + "' " +
                "WHERE player_name='" + oldName + "'");

        // update in plots
        Momentum.getDatabaseManager().runAsyncQuery("UPDATE plots SET " +
                "player_name='" + playerStats.getPlayerName() + "' " +
                "WHERE player_name='" + oldName + "'");

        // update in bought levels
        Momentum.getDatabaseManager().runAsyncQuery("UPDATE bought_levels SET " +
                "player_name='" + playerStats.getPlayerName() + "' " +
                "WHERE player_name='" + oldName + "'");

        // update in saves
        Momentum.getDatabaseManager().runAsyncQuery("UPDATE saves SET " +
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

        Momentum.getDatabaseManager().runAsyncQuery(query);
    }

    public static void updatePlayerNightVision(PlayerStats playerStats) {
        int vision = 0;

        if (playerStats.hasNVStatus())
            vision = 1;

        String query = "UPDATE players SET " +
                "night_vision=" + vision + " " +
                "WHERE player_id=" + playerStats.getPlayerID()
                ;

        Momentum.getDatabaseManager().runAsyncQuery(query);
    }

    public static void updatePlayerGrinding(PlayerStats playerStats)
    {
        int grinding = 0;

        if (playerStats.isGrinding())
            grinding = 1;

        String query = "UPDATE players SET " +
                "grinding=" + grinding + " WHERE player_id=" + playerStats.getPlayerID();

        Momentum.getDatabaseManager().runAsyncQuery(query);
    }

    public static void updateCoins(PlayerStats playerStats, double coins)
    {
        if (coins < 0)
            coins = 0;

        String query = "UPDATE players SET coins=" + coins + " WHERE player_id=" + playerStats.getPlayerID();
        Momentum.getDatabaseManager().runAsyncQuery(query);
    }

    public static void updateCoinsUUID(String UUID, double coins)
    {
        if (coins < 0)
            coins = 0;

        String query = "UPDATE players SET coins=" + coins + " WHERE uuid='" + UUID + "'";
        Momentum.getDatabaseManager().runAsyncQuery(query);
    }

    public static void updateCoinsName(String playerName, double coins)
    {
        if (coins < 0)
            coins = 0;

        String query = "UPDATE players SET coins=" + coins + " WHERE player_name='" + playerName + "'";
        Momentum.getDatabaseManager().runAsyncQuery(query);
    }

    public static void updateRecordsName(String playerName, int records)
    {
        if (records < 0)
            records = 0;

        String query = "UPDATE players SET records=" + records + " WHERE player_name='" + playerName + "'";
        Momentum.getDatabaseManager().runAsyncQuery(query);
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
                " WHERE player_name=?", playerName
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
                " WHERE player_name=?", playerName
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
                " WHERE player_name=?", playerName
        );

        return !playerResults.isEmpty();
    }

    public static int getPlayerID(String playerName) {

        int playerID = -1;

        List<Map<String, String>> playerResults = DatabaseQueries.getResults(
                "players",
                "player_id",
                " WHERE player_name=?", playerName
        );

        for (Map<String, String> playerResult : playerResults)
            playerID = Integer.parseInt(playerResult.get("player_id"));

        return playerID;
    }

    public static void updateBoughtLevels(PlayerStats playerStats)
    {
        if (playerStats != null)
        {
            HashSet<String> boughtLevels = new HashSet<>();

            List<Map<String, String>> boughtResults = DatabaseQueries.getResults(
                    "bought_levels",
                    "level_name",
                    "WHERE uuid='" + playerStats.getUUID() + "'"
            );

            for (Map<String, String> boughtResult : boughtResults)
                boughtLevels.add(boughtResult.get("level_name"));

            playerStats.setBoughtLevels(boughtLevels);
        }
    }

    public static void addBoughtLevel(String UUID, String playerName, String boughtLevel)
    {
        String query = "INSERT INTO bought_levels " +
                "(uuid, player_name, level_name)" +
                " VALUES " +
                "('" +
                UUID + "', '" +
                playerName + "', '" +
                boughtLevel +
                "')"
                ;

        Momentum.getDatabaseManager().runAsyncQuery(query);
    }

    public static void addBoughtLevel(PlayerStats playerStats, String boughtLevel)
    {
        String query = "INSERT INTO bought_levels " +
                "(uuid, player_name, level_name)" +
                " VALUES " +
                "('" +
                playerStats.getUUID() + "', '" +
                playerStats.getPlayerName() + "', '" +
                boughtLevel +
                "')"
        ;

        Momentum.getDatabaseManager().runAsyncQuery(query);
    }

    public static void removeBoughtLevel(String playerName, String boughtLevel)
    {
        String query = "DELETE FROM bought_levels WHERE player_name=? AND level_name=?";

        Momentum.getDatabaseManager().runAsyncQuery(query, playerName, boughtLevel);
    }

    public static boolean hasBoughtLevel(String playerName, String boughtLevel)
    {
        List<Map<String, String>> playerResults = DatabaseQueries.getResults(
                "bought_levels",
                "uuid",
                " WHERE player_name=? AND level_name=?", playerName, boughtLevel
        );

        return !playerResults.isEmpty();
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
        for (Level level : Momentum.getLevelManager().getLevels().values())
            if (playerStats.getLevelCompletionsCount(level.getName()) > 0)
                individualLevelsBeaten++;

        playerStats.setIndividualLevelsBeaten(individualLevelsBeaten);
    }

    public static void insertCompletion(PlayerStats playerStats, Level level, LevelCompletion levelCompletion) {

        Momentum.getDatabaseManager().runAsyncQuery(
                "INSERT INTO completions " +
                    "(player_id, level_id, time_taken, completion_date)" +
                    " VALUES (" +
                    playerStats.getPlayerID() + ", " +
                    level.getID() + ", " +
                    levelCompletion.getCompletionTimeElapsed() + ", " +
                    "FROM_UNIXTIME(" + (levelCompletion.getTimeOfCompletion() / 1000) + ")" +
                    ")"
        );

        Momentum.getDatabaseManager().runAsyncQuery("UPDATE players SET level_completions=" + playerStats.getTotalLevelCompletions() +
                                                  " WHERE player_name='" + playerStats.getPlayerName() + "'");
    }

    public static int getTotalCompletions(String playerName) {

        List<Map<String, String>> playerResults = DatabaseQueries.getResults("players", "level_completions", " WHERE player_name=?", playerName);

        for (Map<String, String> playerResult : playerResults)
            return Integer.parseInt(playerResult.get("level_completions"));

        return -1;
    }

    public static int getCompletionsFromLevel(String playerName, int levelID)
    {
        int playerID = getPlayerID(playerName);

        List<Map<String, String>> playerResults = DatabaseQueries.getResults("completions", "COUNT(*) AS total_level_completions",
                " WHERE player_id=" + playerID + " AND level_id=" + levelID);

        for (Map<String, String> playerResult : playerResults)
            return Integer.parseInt(playerResult.get("total_level_completions"));

        return -1;
    }

    public static void removeCompletions(int playerID, int levelID) {

        String query = "DELETE FROM completions WHERE player_id=" + playerID + " AND level_id=" + levelID;

        Momentum.getDatabaseManager().runAsyncQuery(query);
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
            Level level = Momentum.getLevelManager().get(levelResult.get("level_name"));

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
        Momentum.getStatsManager().toggleLoadingLeaderboards(true);

        ResultSet results = DatabaseQueries.getRawResults(
                "SELECT c.level_id, l.level_name, p.player_name, c.time_taken, c.completion_date " +
                "FROM (" +
                "  SELECT *, ROW_NUMBER() OVER (PARTITION BY level_id ORDER BY time_taken) AS row_num" +
                "  FROM (" +
                "    SELECT level_id, player_id, MIN(time_taken) AS time_taken, MIN(completion_date) AS completion_date" +
                "    FROM completions" +
                "    WHERE time_taken > 0" +
                "    GROUP BY level_id, player_id" +
                "  ) AS grouped_completions" +
                ") AS c " +
                "JOIN players p ON c.player_id = p.player_id " +
                "JOIN levels l ON c.level_id = l.level_id " +
                "WHERE c.row_num <= 10 " +
                "ORDER BY c.level_id, c.time_taken;"
        );

        if (results != null)
        {
            // default values
            int currentID = -1;
            Level currentLevel = null;
            List<LevelCompletion> currentLB = new ArrayList<>();

            try
            {
                while (results.next())
                {
                    int levelID = results.getInt("level_id");

                    if (currentID != levelID)
                    {
                        // if not at the start (level is null), set LB
                        if (currentLevel != null)
                            currentLevel.setLeaderboardCache(currentLB);

                        // initialize
                        currentLB = new ArrayList<>();
                        currentLevel = Momentum.getLevelManager().get(results.getString("level_name"));

                        // adjust
                        currentID = levelID;
                    }

                    // create completion
                    LevelCompletion levelCompletion = new LevelCompletion(results.getLong("completion_date"), results.getLong("time_taken"));
                    levelCompletion.setPlayerName(results.getString("player_name"));

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

            // sync records afterwards
            syncRecords();
        }
        Momentum.getStatsManager().toggleLoadingLeaderboards(false);
    }

    public static void syncRecords()
    {
        HashMap<String, Integer> recordsMap = new HashMap<>();

        for (Level level : Momentum.getLevelManager().getLevels().values())
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
            Momentum.getDatabaseManager().runQuery("UPDATE players SET records=0");

            for (Map.Entry<String, Integer> entry : recordsMap.entrySet())
            {
                PlayerStats playerStats = Momentum.getStatsManager().getByName(entry.getKey());

                // if not null, use stats manager
                if (playerStats != null)
                    playerStats.setRecords(entry.getValue());

                StatsDB.updateRecordsName(entry.getKey(), entry.getValue());
            }
            Momentum.getPluginLogger().info("Synced " + recordsMap.size() + " level records");
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
                        " ON completions.player_id=player.player_id" +
                        " WHERE level_id=" + level.getID() +
                        " AND time_taken > 0" +
                        " ORDER BY time_taken" +
                        " ASC LIMIT 500"
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
                        Long.parseLong(levelResult.get("time_taken"))
                );

                levelCompletion.setPlayerName(playerName);
                levelCompletions.add(levelCompletion);
                addedPlayers.add(playerName);
            }
        }
        level.setLeaderboardCache(levelCompletions);
    }
}
