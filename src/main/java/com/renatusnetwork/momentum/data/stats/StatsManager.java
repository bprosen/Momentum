package com.renatusnetwork.momentum.data.stats;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.api.GGRewardEvent;
import com.renatusnetwork.momentum.data.bank.items.BankItemType;
import com.renatusnetwork.momentum.data.checkpoints.CheckpointDB;
import com.renatusnetwork.momentum.data.clans.Clan;
import com.renatusnetwork.momentum.data.cmdsigns.CmdSignsDB;
import com.renatusnetwork.momentum.data.elo.ELOOutcomeTypes;
import com.renatusnetwork.momentum.data.elo.ELOTier;
import com.renatusnetwork.momentum.data.infinite.gamemode.InfiniteType;
import com.renatusnetwork.momentum.data.leaderboards.ELOLBPosition;
import com.renatusnetwork.momentum.data.levels.CompletionsDB;
import com.renatusnetwork.momentum.data.levels.Level;
import com.renatusnetwork.momentum.data.menus.LevelSortingType;
import com.renatusnetwork.momentum.data.modifiers.Modifier;
import com.renatusnetwork.momentum.data.modifiers.ModifierType;
import com.renatusnetwork.momentum.data.modifiers.boosters.Booster;
import com.renatusnetwork.momentum.data.perks.Perk;
import com.renatusnetwork.momentum.data.races.gamemode.RaceEndReason;
import com.renatusnetwork.momentum.data.ranks.Rank;
import com.renatusnetwork.momentum.data.leaderboards.CoinsLBPosition;
import com.renatusnetwork.momentum.data.leaderboards.GlobalPersonalLBPosition;
import com.renatusnetwork.momentum.data.saves.SavesDB;
import com.renatusnetwork.momentum.storage.mysql.DatabaseManager;
import com.renatusnetwork.momentum.storage.mysql.DatabaseQueries;
import com.renatusnetwork.momentum.utils.Utils;
import com.renatusnetwork.momentum.utils.dependencies.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class StatsManager {

    private HashMap<String, PlayerStats> playerStatsUUID; // index by uuid
    private HashMap<String, PlayerStats> playerStatsName; // index by name
    private HashSet<PlayerStats> ascendancePlayerList;

    private Set<Player> hiddenPlayers;

    private ArrayList<GlobalPersonalLBPosition> globalPersonalCompletionsLB;
    private ArrayList<CoinsLBPosition> coinsLB;
    private ArrayList<ELOLBPosition> eloLB;
    private HashMap<String, ELOLBPosition> eloLBNames;

    private HashSet<String> saidGG;
    private BukkitTask ggTask;

    private int totalPlayers;
    private long totalCoins;

    private LinkedHashMap<String, PlayerStats> offlineCache;

    public StatsManager(Plugin plugin) {
        this.playerStatsUUID = new HashMap<>();
        this.playerStatsName = new HashMap<>();
        this.offlineCache = new LinkedHashMap<>();
        this.ascendancePlayerList = new HashSet<>();
        this.globalPersonalCompletionsLB = new ArrayList<>(Momentum.getSettingsManager().max_global_personal_completions_leaderboard_size);
        this.coinsLB = new ArrayList<>(Momentum.getSettingsManager().max_coins_leaderboard_size);
        this.eloLB = new ArrayList<>(Momentum.getSettingsManager().elo_lb_size);
        this.eloLBNames = new HashMap<>(Momentum.getSettingsManager().elo_lb_size);
        this.hiddenPlayers = new HashSet<>();
        this.saidGG = new HashSet<>();

        startScheduler(plugin);
        totalPlayers = StatsDB.getTotalPlayers();
    }

    private void startScheduler(Plugin plugin) {

        // run personal lb load and online players perks gained count every 3 mins in async
        new BukkitRunnable() {
            @Override
            public void run() {
                loadGlobalPersonalCompletionsLB();
                loadCoinsLB();
                loadTotalCoins();
                loadELOLB();
            }
        }.runTaskTimerAsynchronously(plugin, 20 * 30, 20 * 180);

        // update ascendance players every second
        new BukkitRunnable() {
            @Override
            public void run() {
                updateAscendancePlayers();
            }
        }.runTaskTimerAsynchronously(Momentum.getPlugin(), 20, 20);
    }

    public void loadStats(PlayerStats playerStats) {
        // load all db and player stats needed
        StatsDB.loadPlayerInformation(playerStats);
        StatsDB.loadBoughtLevels(playerStats);
        CheckpointDB.loadCheckpoints(playerStats);
        SavesDB.loadSaves(playerStats);
        StatsDB.loadFavoriteLevels(playerStats);
        CompletionsDB.loadCompletions(playerStats);
        loadIndividualLevelsBeaten(playerStats);
        StatsDB.loadModifiers(playerStats);
        StatsDB.loadBoughtPerks(playerStats);
        loadRestOfPerks(playerStats);
        CmdSignsDB.loadUsedCommandSigns(playerStats);

        // if loading lb, cant calculate records normally, get from db
        if (Momentum.getLevelManager().isLoadingLeaderboards()) {
            CompletionsDB.loadRecords(playerStats);
        } else {
            playerStats.setRecords(Momentum.getLevelManager().getRecords(playerStats.getName()));
        }
    }

    private void loadRestOfPerks(PlayerStats playerStats) {
        for (Perk perk : Momentum.getPerkManager().getPerks()) {
            if (perk.hasAccess(playerStats)) {
                playerStats.addPerk(perk);
            }
        }
    }

    private void loadIndividualLevelsBeaten(PlayerStats playerStats) {
        // get individual levels beaten by looping through list
        int individualLevelsBeaten = 0;
        for (Level level : Momentum.getLevelManager().getLevels().values()) {
            if (playerStats.hasCompleted(level)) {
                individualLevelsBeaten++;
            }
        }

        playerStats.setIndividualLevelsBeaten(individualLevelsBeaten);
    }

    public void addOffline(PlayerStats playerStats) {
        offlineCache.put(playerStats.getUUID(), playerStats);

        // trim the last element if its over the max
        if (offlineCache.size() > Momentum.getSettingsManager().max_offline_cache_size) {
            Iterator<Map.Entry<String, PlayerStats>> iterator = offlineCache.entrySet().iterator();
            // tail insertion means first one is the last
            offlineCache.remove(iterator.next().getKey());
        }
    }

    public void leaveLevelAndReset(PlayerStats playerStats, boolean autoSaveLevel) {
        if (autoSaveLevel) {
            Momentum.getSavesManager().autoSave(playerStats);
        }

        if (playerStats.isInBlackMarket()) {
            Momentum.getBlackMarketManager().playerLeft(playerStats, false); // remove from event
        }

        // toggle off elytra armor
        toggleOffElytra(playerStats);

        playerStats.resetPreviewLevel();
        playerStats.resetCurrentCheckpoint();
        resetPracticeDataOnly(playerStats);
        playerStats.resetLevel();
        playerStats.clearPotionEffects();

        if (playerStats.isAttemptingRankup()) {
            leftRankup(playerStats);
        }

        if (playerStats.isAttemptingMastery()) {
            leftMastery(playerStats);
        }
    }

    public PlayerStats getOffline(String uuid) {
        return offlineCache.get(uuid);
    }

    public PlayerStats getByName(String playerName) {
        return playerName != null ? playerStatsName.get(playerName.toLowerCase()) : null;
    }

    public PlayerStats get(Player player) {
        return playerStatsUUID.get(player.getUniqueId().toString());
    }

    public PlayerStats get(String uuid) {
        return playerStatsUUID.get(uuid);
    }

    public int getTotalPlayers() {
        return totalPlayers;
    }

    public void addTotalPlayer() {
        totalPlayers++;
    }

    public HashMap<String, PlayerStats> getPlayerStats() {
        return playerStatsUUID;
    }

    public Collection<PlayerStats> getOnlinePlayers() {
        return playerStatsUUID.values();
    }

    public void updateSpectatable(PlayerStats playerStats, boolean spectatable) {
        playerStats.setSpectatable(spectatable);
        StatsDB.updateSpectatable(playerStats.getUUID(), spectatable);
    }

    public void enteredAscendance(PlayerStats playerStats) {
        synchronized (ascendancePlayerList) {
            ascendancePlayerList.add(playerStats);
        }

        // if is ascendance, toggle NV on
        if (!playerStats.hasNightVision()) {
            playerStats.setNightVision(true);
            StatsDB.updateNightVision(playerStats.getUUID(), true);
        }
    }

    public void leftAscendance(PlayerStats playerStats) {
        synchronized (ascendancePlayerList) {
            ascendancePlayerList.remove(playerStats);
        }

        // if is ascendance, toggle NV on
        if (playerStats.hasNightVision()) {
            playerStats.setNightVision(false);
            playerStats.getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);
            StatsDB.updateNightVision(playerStats.getUUID(), false);
        }
    }

    public boolean isInAscendance(PlayerStats playerStats) {
        return ascendancePlayerList.contains(playerStats);
    }

    public void addFromOffline(PlayerStats playerStats, Player player) {
        offlineCache.remove(playerStats.getUUID()); // clean cache

        playerStats.setPlayer(player); // update player object

        String playerName = player.getName();

        // update player name logic
        if (!playerStats.getName().equals(playerName)) {
            playerStats.setName(playerName);
            StatsDB.updateName(playerStats.getUUID(), playerName);
        }

        synchronized (playerStatsUUID) {
            playerStatsUUID.put(playerStats.getUUID(), playerStats);
            playerStatsName.put(playerStats.getName().toLowerCase(), playerStats);
        }
    }

    public PlayerStats add(Player player) {
        // ensure thread safety
        synchronized (playerStatsUUID) {
            PlayerStats playerStats = new PlayerStats(player);
            playerStatsUUID.put(player.getUniqueId().toString(), playerStats);
            playerStatsName.put(player.getName().toLowerCase(), playerStats);

            return playerStats;
        }
    }

    public void remove(PlayerStats playerStats) {
        // ensure thread safety
        synchronized (playerStatsUUID) {
            playerStatsUUID.remove(playerStats.getUUID());
            playerStatsName.remove(playerStats.getName().toLowerCase());
        }

        synchronized (ascendancePlayerList) {
            ascendancePlayerList.remove(playerStats);
        }
    }

    public void addGG(PlayerStats playerStats) {
        if (ggTask != null && !saidGG.contains(playerStats.getName())) {
            int reward = Momentum.getSettingsManager().default_gg_coin_reward;

            GGRewardEvent event = new GGRewardEvent(playerStats, reward);
            Bukkit.getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                reward = event.getReward();

                if (playerStats.hasModifier(ModifierType.GG_BOOSTER)) {
                    Booster booster = (Booster) playerStats.getModifier(ModifierType.GG_BOOSTER);
                    reward *= booster.getMultiplier();
                }

                String rewardString = "&6" + Utils.formatNumber(reward);
                // means its been boosted!
                if (reward != Momentum.getSettingsManager().default_gg_coin_reward) {
                    rewardString = "&c&m" + Utils.formatNumber(Momentum.getSettingsManager().default_gg_coin_reward) + "&6 " + Utils.formatNumber(reward);
                }

                saidGG.add(playerStats.getName());
                playerStats.getPlayer().sendMessage(Utils.translate(rewardString + " &eCoins &7for saying &3&lGG"));
                Momentum.getStatsManager().addCoins(playerStats, reward);
            }
        }
    }

    public void runGGTimer() {
        if (ggTask != null)
        // cancel and rerun timer
        {
            ggTask.cancel();
        }

        ggTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!saidGG.isEmpty()) {
                    // plural because im crazy
                    String playerString = "player";

                    if (saidGG.size() > 1) {
                        playerString += "s";
                    }

                    Bukkit.broadcastMessage(Utils.translate("&3" + saidGG.size() + " &7" + playerString + " said &3&lGG"));
                }
                saidGG.clear();
                ggTask = null;
            }
        }.runTaskLater(Momentum.getPlugin(), Momentum.getSettingsManager().default_gg_timer * 20);
    }

    public void updateInfiniteBlock(PlayerStats playerStats, Material material) {
        playerStats.setInfiniteBlock(material);
        StatsDB.updateInfiniteBlock(playerStats.getUUID(), material.name());
    }

    public void resetInfiniteBlock(PlayerStats playerStats) {
        playerStats.setInfiniteBlock(Momentum.getSettingsManager().infinite_default_block);
        StatsDB.resetInfiniteBlock(playerStats.getUUID());
    }

    public void updateInfiniteScore(PlayerStats playerStats, InfiniteType type, int score) {
        playerStats.setInfiniteScore(type, score);
        StatsDB.updateInfiniteScore(playerStats.getUUID(), type, score);
    }

    public void addEventWin(PlayerStats playerStats) {
        // update wins
        playerStats.addEventWin();
        StatsDB.updateEventWins(playerStats.getUUID(), playerStats.getEventWins());
    }

    public void toggleGrinding(PlayerStats playerStats) {
        playerStats.toggleGrinding();
        StatsDB.updateGrinding(playerStats.getUUID(), playerStats.isGrinding());
    }

    public void toggleFails(PlayerStats playerStats) {
        playerStats.toggleFailMode();
        playerStats.resetFails();
        StatsDB.updateFailMode(playerStats.getUUID(), playerStats.inFailMode());
    }

    public void toggleNightVision(PlayerStats playerStats) {
        Player player = playerStats.getPlayer();

        playerStats.toggleNightVision();

        if (playerStats.hasNightVision()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0));
        } else {
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        }

        StatsDB.updateNightVision(playerStats.getUUID(), playerStats.hasNightVision());
    }

    public void toggleAutoSave(PlayerStats playerStats) {
        playerStats.toggleAutoSave();
        StatsDB.updateAutoSave(playerStats.getUUID(), playerStats.hasAutoSave());
    }

    public void processELOChange(PlayerStats playerStats, int newELO) {
        ELOTier currentTier = playerStats.getELOTier();
        ELOTier nextTier = currentTier.getNextELOTier();
        ELOTier previousTier = currentTier.getPreviousELOTier();

        // means they have now achieved the new tier
        if (nextTier != null && nextTier.getRequiredELO() <= newELO) {
            updateELOTier(playerStats, nextTier);
            Bukkit.broadcastMessage(Utils.translate("&c" + playerStats.getDisplayName() + "&7 advanced to &c" + nextTier.getTitle()));
        } else if (previousTier != null && currentTier.getRequiredELO() > newELO) {
            updateELOTier(playerStats, previousTier);
            Bukkit.broadcastMessage(Utils.translate("&c" + playerStats.getDisplayName() + "&7 is now &c" + previousTier.getTitle()));
        }
    }

    public void updateELOData(PlayerStats playerStats, int elo) {
        playerStats.setELO(elo);
        StatsDB.updateELO(playerStats.getUUID(), elo);
    }

    public void updateELO(PlayerStats playerStats, int elo) {
        updateELOData(playerStats, elo);
        processELOChange(playerStats, elo); // run tier process
        playerStats.loadELOToXPBar();
    }

    public void updateELOTier(PlayerStats playerStats, ELOTier eloTier) {
        playerStats.setELOTier(eloTier);
        StatsDB.updateELOTier(playerStats.getUUID(), eloTier.getName());
    }

    public void calculateNewELO(PlayerStats competitor, PlayerStats against, ELOOutcomeTypes outcome) {
        int newELO = competitor.calculateNewELO(against, outcome);
        updateELO(competitor, newELO);
    }

    public void updateBankBid(PlayerStats playerStats, BankItemType type, int bid) {
        long bidDateMillis = System.currentTimeMillis();
        int week = Momentum.getBankManager().getCurrentWeek();

        if (playerStats.hasBankBid(type)) {
            playerStats.updateBankBid(type, bid, bidDateMillis);
            StatsDB.updateBankBid(playerStats.getUUID(), week, type, bid, bidDateMillis);
        } else {
            playerStats.addBankBid(type, bid, bidDateMillis);
            StatsDB.insertBankBid(playerStats.getUUID(), week, type, bid, bidDateMillis);
        }
    }

    public void updateCoins(PlayerStats playerStats, int coins) {
        updateCoins(playerStats, coins, true);
    }

    public void updateCoins(PlayerStats playerStats, int coins, boolean async) {
        StatsDB.updateCoins(playerStats.getUUID(), coins, async);
        playerStats.setCoins(coins);
    }

    public void removeCoins(PlayerStats playerStats, int coins) {
        removeCoins(playerStats, coins, true);
    }

    public void removeCoins(PlayerStats playerStats, int coins, boolean async) {
        StatsDB.updateCoins(playerStats.getUUID(), playerStats.getCoins() - coins, async);
        playerStats.removeCoins(coins);
    }

    public void addCoins(PlayerStats playerStats, int coins) {
        addCoins(playerStats, coins, true);
    }

    public void addCoins(PlayerStats playerStats, int coins, boolean async) {
        StatsDB.updateCoins(playerStats.getUUID(), playerStats.getCoins() + coins, async);
        playerStats.addCoins(coins);
    }

    public void addBoughtLevel(PlayerStats playerStats, Level level) {
        playerStats.buyLevel(level);
        StatsDB.addBoughtLevel(playerStats.getUUID(), level.getName());
    }

    public void removeBoughtLevel(PlayerStats playerStats, Level level) {
        playerStats.removeBoughtLevel(level);
        StatsDB.removeBoughtLevel(playerStats.getUUID(), level.getName());
    }

    public void addFavoriteLevel(PlayerStats playerStats, Level level) {
        playerStats.addFavoriteLevel(level);
        StatsDB.addFavoriteLevel(playerStats.getUUID(), level.getName());
    }

    public void removeFavoriteLevel(PlayerStats playerStats, Level level) {
        playerStats.removeFavoriteLevel(level);
        StatsDB.removeFavoriteLevel(playerStats.getUUID(), level.getName());
    }

    public void enteredRankup(PlayerStats playerStats) {
        playerStats.setAttemptingRankup(true);
        StatsDB.updateAttemptingRankup(playerStats.getUUID(), true);
    }

    public void leftRankup(PlayerStats playerStats) {
        playerStats.setAttemptingRankup(false);
        StatsDB.updateAttemptingRankup(playerStats.getUUID(), false);
    }

    public void enteredMastery(PlayerStats playerStats) {
        playerStats.setAttemptingMastery(true);
        StatsDB.updateAttemptingMastery(playerStats.getUUID(), true);
    }

    public void leftMastery(PlayerStats playerStats) {
        playerStats.setAttemptingMastery(false);
        StatsDB.updateAttemptingMastery(playerStats.getUUID(), false);
    }

    public void addModifier(PlayerStats playerStats, Modifier modifier) {
        // add to cache and db
        playerStats.addModifier(modifier);
        StatsDB.addModifier(playerStats.getUUID(), modifier.getName());
    }

    public void removeModifier(PlayerStats playerStats, Modifier modifier) {
        // remove from cache and db
        playerStats.removeModifier(modifier);
        StatsDB.removeModifier(playerStats.getUUID(), modifier.getName());
    }

    public void removeModifierName(String playerName, Modifier modifier) {
        PlayerStats playerStats = Momentum.getStatsManager().getByName(playerName);

        // remove from cache if not null
        if (playerStats != null) {
            removeModifier(playerStats, modifier);
        } else {
            StatsDB.removeModifierName(playerName, modifier.getName());
        }
    }

    public void updateMenuSortLevelsType(PlayerStats playerStats, LevelSortingType newType) {
        playerStats.setLevelSortingType(newType);
        StatsDB.updateMenuSortLevelsType(playerStats.getUUID(), newType);
    }

    public void updateRaceWins(PlayerStats playerStats, int wins) {
        playerStats.setRaceWins(wins);
        StatsDB.updateRaceWins(playerStats.getUUID(), wins);
        playerStats.calcRaceWinRate();
    }

    public void updateRaceLosses(PlayerStats playerStats, int losses) {
        playerStats.setRaceLosses(losses);
        StatsDB.updateRaceLosses(playerStats.getUUID(), losses);
        playerStats.calcRaceWinRate();
    }

    public long getTotalCoins() {
        return totalCoins;
    }

    public void loadTotalCoins() {
        Map<String, String> result = DatabaseQueries.getResult(DatabaseManager.PLAYERS_TABLE, "SUM(coins) AS total_coins", "");

        if (!result.isEmpty() && result.get("total_coins") != null) {
            totalCoins = (long) Double.parseDouble(result.get("total_coins"));
        }
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
                    "DESC LIMIT " + Momentum.getSettingsManager().max_global_personal_completions_leaderboard_size
                                                                                    );

            for (Map<String, String> playerCompletionStat : playerCompletions) {
                int completions = Integer.parseInt(playerCompletionStat.get("total_completions"));
                String playerName = playerCompletionStat.get("name");

                // if they have more than 0 completions, add (reset stats case)
                if (completions > 0)
                // add playername to completion in map
                {
                    globalPersonalCompletionsLB.add(new GlobalPersonalLBPosition(playerName, completions));
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
                                                                                "WHERE coins > 0 ORDER BY coins DESC LIMIT " + Momentum.getSettingsManager().max_coins_leaderboard_size);

            for (Map<String, String> coinsResult : coinsResults) {
                String playerName = coinsResult.get("name");
                int coins = Integer.parseInt(coinsResult.get("coins"));
                coinsLB.add(new CoinsLBPosition(playerName, coins));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadELOLB() {
        eloLB.clear();
        eloLBNames.clear();

        try {
            // find the highest top 10 completion stat
            List<Map<String, String>> eloResults = DatabaseQueries.getResults(DatabaseManager.PLAYERS_TABLE, "name, elo",
                                                                              "WHERE elo IS NOT NULL ORDER BY elo DESC LIMIT " + Momentum.getSettingsManager().elo_lb_size);

            int position = 1;
            for (Map<String, String> eloResult : eloResults) {
                String playerName = eloResult.get("name");
                int elo = Integer.parseInt(eloResult.get("elo"));
                ELOLBPosition lbPosition = new ELOLBPosition(playerName, elo, position);

                eloLB.add(lbPosition);
                eloLBNames.put(playerName, lbPosition);
                position++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<CoinsLBPosition> getCoinsLB() {
        return coinsLB;
    }

    public ArrayList<ELOLBPosition> getELOLB() {
        return eloLB;
    }

    public ELOLBPosition getELOLBPositionIfExists(String playerName) {
        return eloLBNames.get(playerName);
    }

    public ArrayList<GlobalPersonalLBPosition> getGlobalPersonalCompletionsLB() {
        return globalPersonalCompletionsLB;
    }

    public void toggleOffElytra(PlayerStats playerStats) {
        playerStats.getPlayer().setGliding(false); // disable gliding

        // load if saved
        if (playerStats.getChestplateSavedFromElytra() != null) {
            playerStats.getPlayer().getInventory().setChestplate(playerStats.getChestplateSavedFromElytra());
            playerStats.setChestplateSavedFromElytra(null);
            // remove elytra if was in level
        } else if (playerStats.inLevel() && playerStats.getLevel().isElytra()) {
            playerStats.getPlayer().getInventory().setChestplate(null);
        }
    }

    public void toggleOnElytra(PlayerStats playerStats) {

        // save item
        if (playerStats.getPlayer().getInventory().getChestplate() != null) {
            playerStats.setChestplateSavedFromElytra(playerStats.getPlayer().getInventory().getChestplate());
        }

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
    public void updateAscendancePlayers() {

        synchronized (ascendancePlayerList) {
            for (PlayerStats playerStats : ascendancePlayerList) {
                ProtectedRegion region = WorldGuard.getRegion(playerStats.getPlayer().getLocation());
                if (region != null) {
                    Level level = Momentum.getLevelManager().get(region.getId());

                    // if their level is not the same as what they moved to, then update it
                    if (level != null && level.isAscendance() && playerStats.inLevel() && !playerStats.getLevel().equals(level)) {
                        playerStats.resetCurrentCheckpoint();

                        // load checkpoint into cache
                        Location checkpoint = playerStats.getCheckpoint(level);

                        if (checkpoint != null) {
                            playerStats.setCurrentCheckpoint(checkpoint);
                        }

                        playerStats.setLevel(level);
                        playerStats.disableLevelStartTime();
                    }
                }
            }
        }
    }

    public void hidePlayer(Player player) {
        hiddenPlayers.add(player);

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.isOp()) {
                player.hidePlayer(Momentum.getPlugin(), online);
            }
        }
    }

    public void showPlayer(Player player) {
        hiddenPlayers.remove(player);

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.isOp()) {
                player.showPlayer(Momentum.getPlugin(), online);
            }
        }
    }

    public boolean containsHiddenPlayer(Player player) {
        return hiddenPlayers.contains(player);
    }

    public void hideHiddenPlayersFromJoined(Player playerJoined) {
        for (Player player : hiddenPlayers) {
            player.hidePlayer(Momentum.getPlugin(), playerJoined);
        }
    }

    public void togglePlayerHiderOff(Player player, boolean notify) {
        togglePlayerHiderOff(player, Utils.getSlotFromInventory(player.getInventory(), Utils.translate("&7Players » &cDisabled")), notify);
    }

    public void togglePlayerHiderOn(Player player, boolean notify) {
        togglePlayerHiderOn(player, Utils.getSlotFromInventory(player.getInventory(), Utils.translate("&7Players » &aEnabled")), notify);
    }

    public void togglePlayerHiderOff(Player player, int slot, boolean notify) {
        if (slot > -1) {
            showPlayer(player);

            if (notify) {
                player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 0.7f, 0);
                player.sendMessage(Utils.translate("&aYou have turned on players"));
            }

            ItemStack newItem = new ItemStack(Material.REDSTONE_TORCH_ON);
            ItemMeta meta = newItem.getItemMeta();
            meta.setDisplayName(Utils.translate("&7Players » &aEnabled"));
            newItem.setItemMeta(meta);
            player.getInventory().setItem(slot, newItem);

        }
    }

    public void togglePlayerHiderOn(Player player, int slot, boolean notify) {
        if (slot > -1) {
            hidePlayer(player);

            if (notify) {
                player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 0.7f, 0);
                player.sendMessage(Utils.translate("&cYou have turned off players"));
            }

            Utils.setDisabledPlayersItem(player.getInventory(), slot);
        }
    }

    /*
        Practice management section
     */
    public void resetPracticeMode(PlayerStats playerStats, boolean message) {
        Player player = playerStats.getPlayer();

        playerStats.teleport(playerStats.getPracticeStart(), true);
        resetPracticeDataOnly(playerStats);

        if (message) {
            player.sendMessage(Utils.translate("&7You have turned practice mode &cOff"));
        }
    }

    public void resetPracticeDataOnly(PlayerStats playerStats) {
        if (playerStats.inPracticeMode()) {
            Player player = playerStats.getPlayer();
            PlayerInventory inv = player.getInventory();
            ItemStack item = Utils.getPracPlateIfExists(inv);

            if (item != null) {
                if (inv.getItemInOffHand().isSimilar(item)) {
                    inv.setItemInOffHand(new ItemStack(Material.AIR));
                } else {
                    inv.remove(item);
                }
            }

            playerStats.resetPracticeMode();
        }
    }

    /*
        Spectator management
     */
    public void spectateToPlayer(PlayerStats spectatorStats, PlayerStats playerStats, boolean initialSpectate) {
        Player spectator = spectatorStats.getPlayer();
        Player player = playerStats.getPlayer();

        if (player.isOnline() && spectator.isOnline()) {
            spectatorStats.teleport(player.getLocation(), false);

            // this is done AFTER teleport to override some world changes that can happen
            if (initialSpectate) {
                spectator.setAllowFlight(true);
                spectator.setFlying(true);
            }

            spectatorStats.sendTitle("&7Teleported to " + player.getDisplayName(), "&2/spectate &7 to exit", 10, 40, 10);
        }
    }

    public void setSpectatorMode(PlayerStats spectatorStats, PlayerStats playerStats, boolean initialSpectate) {
        Player spectator = spectatorStats.getPlayer();

        spectatorStats.setPlayerToSpectate(playerStats);

        // in case they /spectate while spectating
        if (initialSpectate) {
            spectatorStats.setSpectateSpawn(spectator.getLocation());
            toggleOffElytra(spectatorStats);
        }

        spectateToPlayer(spectatorStats, playerStats, initialSpectate);
    }

    public void resetSpectatorMode(PlayerStats spectatorStats) {
        Player player = spectatorStats.getPlayer();

        spectatorStats.setPlayerToSpectate(null);

        if (!player.isOp()) {
            player.setFlying(false);
            player.setAllowFlight(false);
        }

        Location loc = spectatorStats.getSpectateSpawn();

        if (loc != null) {
            spectatorStats.teleport(loc, true);
            spectatorStats.sendTitle("", "&7You are no longer spectating anyone", 10, 40, 10);
            Utils.applySlowness(spectatorStats.getPlayer());
            spectatorStats.resetSpectateSpawn();

            Momentum.getLevelManager().regionLevelCheck(spectatorStats, loc);
        }
    }

    public String createChatHover(PlayerStats playerStats) {
        if (playerStats != null && playerStats.isLoaded()) {
            String playerName = playerStats.getName();
            int coins = playerStats.getCoins();
            int hours = playerStats.getPlayer().getStatistic(Statistic.PLAY_ONE_TICK) / 72000;

            Clan clan = playerStats.getClan();
            String clanString = clan != null ? clan.getTag() : "&cNone";

            Rank rank = playerStats.getRank();
            String rankString = rank != null ? rank.getTitle() : "&cNone";

            String eloTier = playerStats.getELOTierTitleWithLB();
            if (eloTier == null) {
                eloTier = "&cNone";
            }

            int prestiges = playerStats.getPrestiges();
            int records = playerStats.getNumRecords();
            int totalCompletions = playerStats.getTotalLevelCompletions();
            int levelsRated = playerStats.getRatedLevelsCount();
            int raceWins = playerStats.getRaceWins();
            int raceLosses = playerStats.getRaceLosses();
            int eventWins = playerStats.getEventWins();
            int jumps = playerStats.getPlayer().getStatistic(Statistic.JUMP);

            return Utils.translate(
                    "&7Name » &f" + playerName + "\n" +
                    "&7Coins » &6" + Utils.formatNumber(coins) + "\n" +
                    "&7Hours » &b" + Utils.formatNumber(hours) + "\n" +
                    "&7Jumps » &a" + Utils.formatNumber(jumps) + "\n\n" +
                    "&7Rank » &a" + rankString + "\n" +
                    "&7ELO » &2" + eloTier + "\n" +
                    "&7Clan » &e" + clanString + "\n" +
                    "&7Prestige » &5" + prestiges + "\n" +
                    "&7Records » &e✦ " + Utils.formatNumber(records) + "\n" +
                    "&7Total Completions » &a" + Utils.formatNumber(totalCompletions) + "\n" +
                    "&7Rated Levels » &3" + Utils.formatNumber(levelsRated) + "\n" +
                    "&7Race Wins/Losses » &c" + raceWins + "/" + raceLosses + "\n" +
                    "&7Event Wins » &b" + eventWins
                                  );
        }
        return Utils.translate("&cLoading stats...");
    }

    public void shutdown() {
        synchronized (playerStatsUUID) {
            for (PlayerStats playerStats : playerStatsUUID.values()) {
                if (playerStats != null && playerStats.isLoaded()) {
                    if (playerStats.isPreviewingLevel()) {
                        playerStats.resetPreviewLevel();
                    }

                    playerStats.endRace(RaceEndReason.SHUTDOWN);

                    if (playerStats.inPracticeMode()) {
                        resetPracticeMode(playerStats, false);
                    }

                    if (playerStats.isSpectating()) {
                        resetSpectatorMode(playerStats);
                    }

                    toggleOffElytra(playerStats);
                }
            }
        }
    }
}