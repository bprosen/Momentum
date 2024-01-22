package com.renatusnetwork.parkour.data.stats;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.api.GGRewardEvent;
import com.renatusnetwork.parkour.data.checkpoints.CheckpointDB;
import com.renatusnetwork.parkour.data.clans.Clan;
import com.renatusnetwork.parkour.data.clans.ClanMember;
import com.renatusnetwork.parkour.data.infinite.gamemode.InfiniteType;
import com.renatusnetwork.parkour.data.levels.CompletionsDB;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.levels.LevelCompletion;
import com.renatusnetwork.parkour.data.menus.LevelSortingType;
import com.renatusnetwork.parkour.data.modifiers.Modifier;
import com.renatusnetwork.parkour.data.modifiers.ModifierType;
import com.renatusnetwork.parkour.data.modifiers.ModifiersDB;
import com.renatusnetwork.parkour.data.modifiers.boosters.Booster;
import com.renatusnetwork.parkour.data.perks.Perk;
import com.renatusnetwork.parkour.data.perks.PerksDB;
import com.renatusnetwork.parkour.data.ranks.Rank;
import com.renatusnetwork.parkour.data.leaderboards.CoinsLBPosition;
import com.renatusnetwork.parkour.data.leaderboards.GlobalPersonalLBPosition;
import com.renatusnetwork.parkour.data.leaderboards.RecordsLBPosition;
import com.renatusnetwork.parkour.data.saves.SavesDB;
import com.renatusnetwork.parkour.storage.mysql.DatabaseManager;
import com.renatusnetwork.parkour.storage.mysql.DatabaseQueries;
import com.renatusnetwork.parkour.utils.Utils;
import com.renatusnetwork.parkour.utils.dependencies.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class StatsManager {

    private HashMap<String, PlayerStats> playerStatsUUID; // index by uuid
    private HashMap<String, PlayerStats> playerStatsName; // index by name
    private HashSet<PlayerStats> ascendancePlayerList;
    private HashMap<Integer, GlobalPersonalLBPosition> globalPersonalCompletionsLB;
    private HashMap<Integer, CoinsLBPosition> coinsLB;
    private HashMap<Integer, RecordsLBPosition> recordsLB;
    private HashSet<String> saidGG;

    private BukkitTask ggTask;
    private boolean loadingLeaderboards;

    private int totalPlayers;
    private long totalCoins;

    private LinkedHashMap<String, PlayerStats> offlineCache;

    public StatsManager(Plugin plugin)
    {
        this.playerStatsUUID = new HashMap<>();
        this.playerStatsName = new HashMap<>();
        this.offlineCache = new LinkedHashMap<>();
        this.ascendancePlayerList = new HashSet<>();
        this.globalPersonalCompletionsLB = new HashMap<>(Parkour.getSettingsManager().max_global_personal_completions_leaderboard_size);
        this.coinsLB = new HashMap<>(Parkour.getSettingsManager().max_coins_leaderboard_size);
        this.recordsLB = new HashMap<>(Parkour.getSettingsManager().max_records_leaderboard_size);
        this.saidGG = new HashSet<>();

        startScheduler(plugin);
        totalPlayers = StatsDB.getTotalPlayers();
    }

    private void startScheduler(Plugin plugin)
    {

        // Leader Boards
        new BukkitRunnable() {
            public void run() {
                CompletionsDB.loadTotalCompletions();
                CompletionsDB.loadLeaderboards();
                loadGlobalPersonalCompletionsLB();
                loadCoinsLB();
                loadRecordsLB();
                loadTotalCoins();
            }
        }.runTaskAsynchronously(plugin);

        // run personal lb load and online players perks gained count every 3 mins in async
        new BukkitRunnable() {
            @Override
            public void run() {
                loadGlobalPersonalCompletionsLB();
                loadCoinsLB();
                loadRecordsLB();
                loadTotalCoins();
            }
        }.runTaskTimerAsynchronously(plugin, 20 * 180, 20 * 180);

        // update ascendance players every second
        new BukkitRunnable() {
            @Override
            public void run() {
                updateAscendancePlayers();
            }
        }.runTaskTimerAsynchronously(Parkour.getPlugin(), 20, 20);
    }

    public void loadStats(PlayerStats playerStats)
    {
        // load all db and player stats needed
        StatsDB.loadPlayerInformation(playerStats);
        StatsDB.loadBoughtLevels(playerStats);
        CheckpointDB.loadCheckpoints(playerStats);
        SavesDB.loadSaves(playerStats);
        StatsDB.loadFavoriteLevels(playerStats);
        CompletionsDB.loadCompletions(playerStats);
        CompletionsDB.loadRecords(playerStats);
        loadIndividualLevelsBeaten(playerStats);
        StatsDB.loadModifiers(playerStats);
        StatsDB.loadBoughtPerks(playerStats);
        loadRestOfPerks(playerStats);
    }

    private void loadRestOfPerks(PlayerStats playerStats)
    {
        for (Perk perk : Parkour.getPerkManager().getPerks())
            if (perk.hasAccess(playerStats))
                playerStats.addPerk(perk);
    }

    private void loadIndividualLevelsBeaten(PlayerStats playerStats)
    {
        // get individual levels beaten by looping through list
        int individualLevelsBeaten = 0;
        for (Level level : Parkour.getLevelManager().getLevels().values())
            if (playerStats.hasCompleted(level))
                individualLevelsBeaten++;

        playerStats.setIndividualLevelsBeaten(individualLevelsBeaten);
    }

    public void addOffline(PlayerStats playerStats)
    {
        offlineCache.put(playerStats.getUUID(), playerStats);

        // trim the last element if its over the max
        if (offlineCache.size() > Parkour.getSettingsManager().max_offline_cache_size)
        {
            Iterator<Map.Entry<String, PlayerStats>> iterator = offlineCache.entrySet().iterator();
            // tail insertion means first one is the last
            offlineCache.remove(iterator.next().getKey());
        }
    }

    public PlayerStats getOffline(String uuid) { return offlineCache.get(uuid); }

    public PlayerStats getByName(String playerName)
    {
        return playerStatsName.get(playerName);
    }

    public PlayerStats get(Player player) {
        return playerStatsUUID.get(player.getUniqueId().toString());
    }

    public PlayerStats get(String uuid)
    {
        return playerStatsUUID.get(uuid);
    }

    public boolean isLoadingLeaderboards() { return loadingLeaderboards; }

    public void toggleLoadingLeaderboards(boolean loadingLeaderboards) { this.loadingLeaderboards = loadingLeaderboards; }

    public int getTotalPlayers() { return totalPlayers; }

    public void addTotalPlayer() { totalPlayers++; }

    public HashMap<String, PlayerStats> getPlayerStats()
    {
        return playerStatsUUID;
    }

    public Collection<PlayerStats> getOnlinePlayers() { return playerStatsUUID.values(); }

    public void enteredAscendance(PlayerStats playerStats)
    {
        synchronized (ascendancePlayerList)
        {
            ascendancePlayerList.add(playerStats);
        }

        // if is ascendance, toggle NV on
        if (!playerStats.hasNightVision())
        {
            playerStats.setNightVision(true);
            StatsDB.updatePlayerNightVision(playerStats);
        }
    }

    public void leftAscendance(PlayerStats playerStats)
    {
        synchronized (ascendancePlayerList)
        {
            ascendancePlayerList.remove(playerStats);
        }

        // if is ascendance, toggle NV on
        if (playerStats.hasNightVision())
        {
            playerStats.setNightVision(false);
            playerStats.getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);
            StatsDB.updatePlayerNightVision(playerStats);
        }
    }

    public boolean isInAscendance(PlayerStats playerStats) { return ascendancePlayerList.contains(playerStats); }

    public void addFromOffline(PlayerStats playerStats, Player player)
    {
        offlineCache.remove(playerStats.getUUID()); // clean cache

        playerStats.setPlayer(player); // update player object

        String playerName = player.getName();

        // update player name logic
        if (!playerStats.getName().equals(playerName))
        {
            playerStats.setName(playerName);
            StatsDB.updatePlayerName(playerStats);
        }

        synchronized (playerStatsUUID)
        {
            playerStatsUUID.put(playerStats.getUUID(), playerStats);
            playerStatsName.put(playerStats.getName(), playerStats);
        }
    }

    public PlayerStats add(Player player)
    {
        // ensure thread safety
        synchronized (playerStatsUUID)
        {
            PlayerStats playerStats = new PlayerStats(player);
            playerStatsUUID.put(player.getUniqueId().toString(), playerStats);
            playerStatsName.put(player.getName(), playerStats);

            return playerStats;
        }
    }

    public void remove(PlayerStats playerStats)
    {
        // ensure thread safety
        synchronized (playerStatsUUID)
        {
            playerStatsUUID.remove(playerStats.getUUID());
            playerStatsName.remove(playerStats.getName());
        }

        synchronized (ascendancePlayerList)
        {
            ascendancePlayerList.remove(playerStats);
        }
    }

    public void addGG(PlayerStats playerStats)
    {
        if (ggTask != null && !saidGG.contains(playerStats.getName()))
        {
            int reward = Parkour.getSettingsManager().default_gg_coin_reward;

            GGRewardEvent event = new GGRewardEvent(playerStats, reward);
            Bukkit.getPluginManager().callEvent(event);

            if (!event.isCancelled())
            {
                reward = event.getReward();

                if (playerStats.hasModifier(ModifierType.GG_BOOSTER))
                {
                    Booster booster = (Booster) playerStats.getModifier(ModifierType.GG_BOOSTER);
                    reward *= booster.getMultiplier();
                }

                String rewardString;
                // means its been boosted!
                if (reward != Parkour.getSettingsManager().default_gg_coin_reward)
                    rewardString = "&c&m" + Utils.formatNumber(Parkour.getSettingsManager().default_gg_coin_reward) + "&6 " + Utils.formatNumber(reward);
                else
                    rewardString = "&6" + Utils.formatNumber(reward);

                saidGG.add(playerStats.getName());
                playerStats.getPlayer().sendMessage(Utils.translate(rewardString + " &eCoin &7reward for saying &3&lGG&b!"));
                Parkour.getStatsManager().addCoins(playerStats, reward);
            }
        }
    }

    public void runGGTimer()
    {
        if (ggTask != null)
            // cancel and rerun timer
            ggTask.cancel();

            ggTask = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (!saidGG.isEmpty())
                {
                    // plural because im crazy
                    String playerString = "player";

                    if (saidGG.size() > 1)
                        playerString += "s";

                    Bukkit.broadcastMessage(Utils.translate("&3" + saidGG.size() + " &7" + playerString + " said &3&lGG&b!"));
                }
                saidGG.clear();
                ggTask = null;
            }
        }.runTaskLater(Parkour.getPlugin(), Parkour.getSettingsManager().default_gg_timer * 20);
    }

    public void updateInfiniteBlock(PlayerStats playerStats, Material material)
    {
        playerStats.setInfiniteBlock(material);
        StatsDB.updateInfiniteBlock(playerStats.getUUID(), material.name());
    }

    public void updateCoins(PlayerStats playerStats, double coins)
    {
        StatsDB.updateCoins(playerStats.getUUID(), coins);
        playerStats.setCoins(coins);
    }

    public void removeCoins(PlayerStats playerStats, double coins)
    {
        StatsDB.updateCoins(playerStats.getUUID(), playerStats.getCoins() - coins);
        playerStats.removeCoins(coins);
    }

    public void addCoins(PlayerStats playerStats, double coins)
    {
        StatsDB.updateCoins(playerStats.getUUID(), playerStats.getCoins() + coins);
        playerStats.addCoins(coins);
    }

    public void addBoughtLevel(PlayerStats playerStats, Level level)
    {
        playerStats.buyLevel(level);
        StatsDB.addBoughtLevel(playerStats.getUUID(), level.getName());
    }

    public void removeBoughtLevel(PlayerStats playerStats, Level level)
    {
        playerStats.removeBoughtLevel(level);
        StatsDB.removeBoughtLevel(playerStats.getUUID(), level.getName());
    }

    public void addFavoriteLevel(PlayerStats playerStats, Level level)
    {
        playerStats.addFavoriteLevel(level);
        StatsDB.addFavoriteLevel(playerStats.getUUID(), level.getName());
    }

    public void removeFavoriteLevel(PlayerStats playerStats, Level level)
    {
        playerStats.removeFavoriteLevel(level);
        StatsDB.removeFavoriteLevel(playerStats.getUUID(), level.getName());
    }

    public void enteredRankup(PlayerStats playerStats)
    {
        playerStats.setAttemptingRankup(true);
        StatsDB.updateAttemptingRankup(playerStats);
    }

    public void leftRankup(PlayerStats playerStats)
    {
        playerStats.setAttemptingRankup(false);
        StatsDB.updateAttemptingRankup(playerStats);
    }

    public void enteredMastery(PlayerStats playerStats)
    {
        playerStats.setAttemptingMastery(true);
        StatsDB.updateAttemptingMastery(playerStats);
    }

    public void leftMastery(PlayerStats playerStats)
    {
        playerStats.setAttemptingMastery(false);
        StatsDB.updateAttemptingMastery(playerStats);
    }

    public void addModifier(PlayerStats playerStats, Modifier modifier)
    {
        // add to cache and db
        playerStats.addModifier(modifier);
        StatsDB.addModifier(playerStats.getUUID(), modifier.getName());
    }

    public void removeModifier(PlayerStats playerStats, Modifier modifier)
    {
        // remove from cache and db
        playerStats.removeModifier(modifier);
        StatsDB.removeModifier(playerStats.getUUID(), modifier.getName());
    }

    public void removeModifierName(String playerName, Modifier modifier)
    {
        PlayerStats playerStats = Parkour.getStatsManager().getByName(playerName);

        // remove from cache if not null
        if (playerStats != null)
            removeModifier(playerStats, modifier);
        else
            StatsDB.removeModifierName(playerName, modifier.getName());
    }

    public void updateMenuSortLevelsType(PlayerStats playerStats, LevelSortingType newType)
    {
        playerStats.setLevelSortingType(newType);
        StatsDB.updateMenuSortLevelsType(playerStats, newType);
    }

    public long getTotalCoins() { return totalCoins; }

    public void loadTotalCoins()
    {
        Map<String, String> result = DatabaseQueries.getResult(DatabaseManager.PLAYERS_TABLE, "SUM(coins) AS total_coins", "");

        totalCoins = (long) Double.parseDouble(result.get("total_coins"));
    }

    public void loadGlobalPersonalCompletionsLB() {
        try {
            globalPersonalCompletionsLB.clear();

            // find the highest top 10 completion stat
            List<Map<String, String>> playerCompletions = DatabaseQueries.getResults(
                    DatabaseManager.LEVEL_COMPLETIONS_TABLE + " lc",
                    "p.name, COUNT(lc.level_name) AS total_completions",
                   "JOIN " + DatabaseManager.PLAYERS_TABLE + " p ON p.uuid=lc.uuid " +
                           "GROUP BY p.name " +
                           "ORDER BY total_completions " +
                           "DESC LIMIT " + Parkour.getSettingsManager().max_global_personal_completions_leaderboard_size
            );

            int lbPos = 1;

            for (Map<String, String> playerCompletionStat : playerCompletions) {
                int completions = Integer.parseInt(playerCompletionStat.get("total_completions"));
                String playerName = playerCompletionStat.get("name");

                // if they have more than 0 completions, add (reset stats case)
                if (completions > 0)
                {
                    // add playername to completion in map
                    globalPersonalCompletionsLB.put(lbPos, new GlobalPersonalLBPosition(playerName, completions));
                    lbPos++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadCoinsLB() {
        coinsLB.clear();

        try {

            // find the highest top 10 completion stat
            List<Map<String, String>> coinsResults = DatabaseQueries.getResults(DatabaseManager.PLAYERS_TABLE, "name, coins",
                    "ORDER BY coins DESC LIMIT " + Parkour.getSettingsManager().max_coins_leaderboard_size);

            int lbPos = 1;
            for (Map<String, String> coinsResult : coinsResults)
            {
                String playerName = coinsResult.get("name");
                double coins = Double.parseDouble(coinsResult.get("coins"));

                // if they have more than 0 completions, add (reset stats case)
                if (coins > 0)
                {
                    coinsLB.put(lbPos, new CoinsLBPosition(playerName, coins));
                    lbPos++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadRecordsLB() {
        recordsLB.clear();

        try {

            // find the highest top 10 completion stat
            List<Map<String, String>> recordsResult = DatabaseQueries.getResults(
                    DatabaseManager.PLAYERS_TABLE + " p",
                    "name, COUNT(level_name) AS numRecords",
                    "JOIN " + DatabaseManager.LEVEL_COMPLETIONS_TABLE + " lc " +
                            "ON lc.uuid=p.uuid " +
                            "WHERE lc.record=(1) " +
                            "GROUP BY p.name " +
                            "ORDER BY numRecords " +
                            "DESC LIMIT " + Parkour.getSettingsManager().max_records_leaderboard_size);

            int lbPos = 1;
            for (Map<String, String> recordResult : recordsResult)
            {
                String playerName = recordResult.get("name");
                int records = Integer.parseInt(recordResult.get("numRecords"));

                // if they have more than 0 completions, add (reset stats case)
                if (records > 0)
                {
                    recordsLB.put(lbPos, new RecordsLBPosition(playerName, records));
                    lbPos++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HashMap<Integer, CoinsLBPosition> getCoinsLB()
    {
        return coinsLB;
    }

    public HashMap<Integer, RecordsLBPosition> getRecordsLB() { return recordsLB; }

    public HashMap<Integer, GlobalPersonalLBPosition> getGlobalPersonalCompletionsLB() {
        return globalPersonalCompletionsLB;
    }

    public void toggleOffElytra(PlayerStats playerStats) {
        playerStats.getPlayer().setGliding(false); // disable gliding

        // load if saved
        if (playerStats.getChestplateSavedFromElytra() != null) {
            playerStats.getPlayer().getInventory().setChestplate(playerStats.getChestplateSavedFromElytra());
            playerStats.setChestplateSavedFromElytra(null);
        // remove elytra if was in level
        } else if (playerStats.inLevel() && playerStats.getLevel().isElytra())
            playerStats.getPlayer().getInventory().setChestplate(null);
    }

    public void toggleOnElytra(PlayerStats playerStats) {

        // save item
        if (playerStats.getPlayer().getInventory().getChestplate() != null)
            playerStats.setChestplateSavedFromElytra(playerStats.getPlayer().getInventory().getChestplate());

        // create item
        ItemStack elytraItem = new ItemStack(Material.ELYTRA);
        ItemMeta itemMeta = elytraItem.getItemMeta();

        itemMeta.setDisplayName(Utils.translate("&cElytra"));
        itemMeta.setUnbreakable(true);
        itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        elytraItem.setItemMeta(itemMeta);

        playerStats.getPlayer().getInventory().setChestplate(elytraItem);
    }

    /*
        Only use active level updating for ascendance, this should barely cause issues as it will be ran every few seconds
        and very few people to iterate through.
     */
    public void updateAscendancePlayers()
    {

        synchronized (ascendancePlayerList)
        {
            for (PlayerStats playerStats : ascendancePlayerList)
            {
                ProtectedRegion region = WorldGuard.getRegion(playerStats.getPlayer().getLocation());
                if (region != null) {
                    Level level = Parkour.getLevelManager().get(region.getId());

                    // if their level is not the same as what they moved to, then update it
                    if (level != null && level.isAscendance() &&
                            playerStats.inLevel() && !playerStats.getLevel().getName().equalsIgnoreCase(level.getName()))
                    {
                        playerStats.resetCurrentCheckpoint();

                        // load checkpoint into cache
                        Location checkpoint = playerStats.getCheckpoint(level);

                        if (checkpoint != null)
                            playerStats.setCurrentCheckpoint(checkpoint);

                        playerStats.setLevel(level);
                        playerStats.disableLevelStartTime();
                    }
                }
            }
        }
    }

    public String createChatHover(PlayerStats playerStats)
    {
        String playerName = playerStats.getName();
        double coins = playerStats.getCoins();
        int hours = playerStats.getPlayer().getStatistic(Statistic.PLAY_ONE_TICK) / 72000;

        Clan clan = playerStats.getClan();
        String clanString = "&cNone";
        if (clan != null)
            clanString = clan.getTag();

        Rank rank = playerStats.getRank();
        String rankString = "&cNone";
        if (rank != null)
            rankString = rank.getTitle();

        int prestiges = playerStats.getPrestiges();
        int records = playerStats.getNumRecords();
        int totalCompletions = playerStats.getTotalLevelCompletions();
        int levelsRated = playerStats.getRatedLevelsCount();
        int raceWins = playerStats.getRaceWins();
        int raceLosses = playerStats.getRaceLosses();
        int eventWins = playerStats.getEventWins();
        int jumps = playerStats.getPlayer().getStatistic(Statistic.JUMP);

        String hover = Utils.translate(
                     "&7Name » &f" + playerName + "\n" +
                     "&7Coins » &6" + Utils.formatNumber(coins) + "\n" +
                     "&7Hours » &b" + Utils.formatNumber(hours) + "\n" +
                     "&7Jumps » &a" + Utils.formatNumber(jumps) + "\n\n" +
                     "&7Clan » &e" + clanString + "\n" +
                     "&7Rank » &a" + rankString + "\n" +
                     "&7Prestige » &5" + prestiges + "\n" +
                     "&7Records » &e✦ " + Utils.formatNumber(records) + "\n" +
                     "&7Total Completions » &a" + Utils.formatNumber(totalCompletions) + "\n" +
                     "&7Rated Levels » &3" + Utils.formatNumber(levelsRated) + "\n" +
                     "&7Race Wins/Losses » &c" + raceWins + "/" + raceLosses + "\n" +
                     "&7Event Wins » &b" + eventWins
        );
        return hover;
    }
}