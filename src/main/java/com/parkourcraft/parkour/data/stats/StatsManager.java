package com.parkourcraft.parkour.data.stats;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.checkpoints.CheckpointDB;
import com.parkourcraft.parkour.data.clans.Clan;
import com.parkourcraft.parkour.data.clans.ClanMember;
import com.parkourcraft.parkour.data.levels.Level;
import com.parkourcraft.parkour.storage.mysql.DatabaseQueries;
import com.parkourcraft.parkour.utils.Utils;
import com.parkourcraft.parkour.utils.dependencies.WorldGuard;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;
import java.util.stream.Collectors;

public class StatsManager {

    private HashMap<String, PlayerStats> playerStatsList = new HashMap<>();
    private HashSet<PlayerStats> ascendancePlayerList = new HashSet<>();

    private LinkedHashMap<String, Integer> globalPersonalCompletionsLB = new LinkedHashMap<>
            (Parkour.getSettingsManager().max_global_personal_completions_leaderboard_size);

    public StatsManager(Plugin plugin) {
        startScheduler(plugin);
    }

    private void startScheduler(Plugin plugin) {
        /*
          // Do not know why Sean did this, as you can just remove from quit, but just gonna disable for now...

        // Garbage collection for offline players
        new BukkitRunnable() {
            public void run() {
                clean();
            }
        }.runTaskTimer(plugin, 0L, 10L);*/

        // Leader Boards
        new BukkitRunnable() {
            public void run() {
                StatsDB.loadTotalCompletions();
                StatsDB.loadLeaderboards();
                Parkour.getLevelManager().loadGlobalLevelCompletionsLB(); // we MUST load this after leaderboards
                loadGlobalPersonalCompletionsLB();
            }
        }.runTaskAsynchronously(plugin);

        // run personal lb load every 3 mins in async
        new BukkitRunnable() {
            @Override
            public void run() {
                loadGlobalPersonalCompletionsLB();
            }
        }.runTaskTimerAsynchronously(plugin, 20 * 180, 20 * 180);

        // update ascendance players every 2 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                updateAscendancePlayers();
            }
        }.runTaskTimerAsynchronously(Parkour.getPlugin(), 20 * 2, 20 * 2);
    }

    public PlayerStats get(String UUID) {
        for (PlayerStats playerStats : getPlayerStats().values())
            if (playerStats.getUUID().equals(UUID))
                return playerStats;

        return null;
    }

    public PlayerStats get(int playerID) {
        for (PlayerStats playerStats : getPlayerStats().values())
            if (playerStats.getPlayerID() == playerID)
                return playerStats;

        return null;
    }

    public HashMap<String, PlayerStats> getPlayerStats() {
        return (HashMap<String, PlayerStats>) playerStatsList.clone();
    }

    public HashSet<PlayerStats> getPlayersInAscendance() { return ascendancePlayerList; }

    public void enteredAscendance(PlayerStats playerStats) {
        ascendancePlayerList.add(playerStats);

        // run in async
        new BukkitRunnable() {
            @Override
            public void run() {
                loadAscendanceCheckpoints(playerStats);
            }
        }.runTaskAsynchronously(Parkour.getPlugin());
    }

    public void leftAscendance(PlayerStats playerStats) {
        ascendancePlayerList.remove(playerStats);
        playerStats.resetAscendanceCheckpoints();
    }

    public boolean isInAscendance(PlayerStats playerStats) { return ascendancePlayerList.contains(playerStats); }

    public PlayerStats getByName(String playerName) {
        for (Map.Entry<String, PlayerStats> entry : getPlayerStats().entrySet())
            if (entry.getKey().equalsIgnoreCase(playerName))
                return entry.getValue();

        return null;
    }

    public PlayerStats get(Player player) {
        return playerStatsList.get(player.getName());
    }

    public boolean exists(String playerName) {
        return getByName(playerName) != null;
    }

    public void add(Player player) {
        if (!exists(player.getUniqueId().toString())) {
            PlayerStats playerStats = new PlayerStats(player);
            playerStatsList.put(player.getName(), playerStats);
        }
    }

    public void addUnloadedPlayers() {
        for (Player player : Bukkit.getOnlinePlayers())
            if (!exists(player.getUniqueId().toString()))
                add(player);
    }

    public void remove(PlayerStats playerStats) {
        playerStatsList.remove(playerStats.getPlayerName());
        ascendancePlayerList.remove(playerStats);
    }

    public void loadGlobalPersonalCompletionsLB() {
        try {
            globalPersonalCompletionsLB.clear();

            // find the highest top 10 completion stat
            List<Map<String, String>> playerCompletions = DatabaseQueries.getResults("players", "player_name, level_completions",
                    " ORDER BY level_completions DESC LIMIT " + Parkour.getSettingsManager().max_global_personal_completions_leaderboard_size);

                for (Map<String, String> playerCompletionStat : playerCompletions)
                    // add playername to completion in map
                    globalPersonalCompletionsLB.put(playerCompletionStat.get("player_name"),
                                                    Integer.parseInt(playerCompletionStat.get("level_completions")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public LinkedHashMap<String, Integer> getGlobalPersonalCompletionsLB() {
        return globalPersonalCompletionsLB;
    }

    public void toggleOffElytra(PlayerStats playerStats) {
        playerStats.getPlayer().setGliding(false); // disable gliding

        // load if saved
        if (playerStats.getChestplateSavedFromElytra() != null) {
            playerStats.getPlayer().getInventory().setChestplate(playerStats.getChestplateSavedFromElytra());
            playerStats.setChestplateSavedFromElytra(null);
        // remove elytra if was in level
        } else if (playerStats.inLevel() && playerStats.getLevel().isElytraLevel())
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
    public void updateAscendancePlayers() {

        for (PlayerStats playerStats : ascendancePlayerList) {

            List<String> regions = WorldGuard.getRegions(playerStats.getPlayer().getLocation());
            if (!regions.isEmpty()) {
                Level level = Parkour.getLevelManager().get(regions.get(0));

                // if their level is not the same as what they moved to, then update it
                if (level != null && level.isAscendanceLevel() &&
                    playerStats.inLevel() && !playerStats.getLevel().getName().equalsIgnoreCase(level.getName())) {
                    // save if has checkpoint
                    if (playerStats.getCheckpoint() != null) {
                        CheckpointDB.savePlayerAsync(playerStats);
                        playerStats.resetCheckpoint();
                    }
                    // load checkpoint into cache
                    Location ascendanceCheckpoint = playerStats.getAscendanceCheckpoint(level.getName());
                    if (ascendanceCheckpoint != null) {
                        playerStats.setCheckpoint(ascendanceCheckpoint);
                        Parkour.getDatabaseManager().add("DELETE FROM checkpoints WHERE uuid='" + playerStats.getUUID() +
                                "' AND level_name='" + level.getName() + "'");
                    }

                    playerStats.setLevel(level);
                    playerStats.disableLevelStartTime();
                }
            }
        }
    }

    public void loadAscendanceCheckpoints(PlayerStats playerStats) {
        playerStats.resetAscendanceCheckpoints();
        playerStats.setAscendanceCheckpoints(CheckpointDB.getAscendanceCheckpoints(playerStats));
    }

    /*
        Profile Section
     */
    public void loadProfile(PlayerStats playerStats, Player opener) {

        Inventory openInventory = opener.getOpenInventory().getTopInventory();
        Inventory newInventory = Bukkit.createInventory(null, openInventory.getSize(), openInventory.getTitle());

        if (openInventory != null && playerStats != null) {
            // so there is not many lines of "You do not have a clan" spam
            boolean alreadyCheckedClan = false;

            for (int i = 0; i < openInventory.getSize(); i++) {
                ItemStack item = openInventory.getItem(i);

                if (item != null) {
                    if (item.getType() != Material.STAINED_GLASS_PANE &&
                        item.hasItemMeta() &&
                        item.getItemMeta().hasLore()) {

                        ItemMeta itemMeta = item.getItemMeta();
                        List<String> itemLore = item.getItemMeta().getLore();
                        List<String> newLore = new ArrayList<>();

                        // special condition if they are viewing someone else's stats, replace the skull with correct values
                        if (playerStats.getPlayerName() != opener.getName() && item.getType() == Material.GOLD_NUGGET) {
                            ItemStack skullItem = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
                            SkullMeta skullMeta = (SkullMeta) skullItem.getItemMeta();
                            skullMeta.setOwningPlayer(playerStats.getPlayer());
                            skullMeta.setDisplayName(Utils.translate("&7You are viewing &c" + playerStats.getPlayer().getDisplayName() + "&7's Profile"));
                            skullItem.setItemMeta(skullMeta);
                            // replace item for later when it sets
                            item = skullItem;
                        // otherwise it is a stat item
                        } else {

                            Clan clan = playerStats.getClan();
                            for (String loreString : itemLore) {

                                // rank and game stats item
                                loreString = loreString.replace("%balance%", ((int) Parkour.getEconomy().getBalance(playerStats.getPlayer())) + "")
                                        .replace("%perks_gained%", playerStats.getPerks().size() + "")
                                        .replace("%perks_total%", Parkour.getPerkManager().getPerks().size() + "")
                                        .replace("%rank_name%", Utils.translate(playerStats.getRank().getRankTitle()))
                                        .replace("%prestiges%", playerStats.getPrestiges() + "")
                                        .replace("%infinite_score%", playerStats.getInfinitePKScore() + "")
                                        .replace("%race_wins%", playerStats.getRaceWins() + "")
                                        .replace("%race_losses%", playerStats.getRaceLosses() + "")
                                        .replace("%race_winrate%", playerStats.getRaceWinRate() + "");

                                // level stats, only add if the most completed level is not null
                                Level mostCompletedLevel = Parkour.getLevelManager().get(playerStats.getMostCompletedLevel());
                                if (mostCompletedLevel != null) {

                                    loreString = loreString.replace("%favorite_level%", mostCompletedLevel.getFormattedTitle())
                                            .replace("%favorite_level_completions%", playerStats.getLevelCompletionsCount(mostCompletedLevel.getName()) + "");

                                    LevelCompletion fastestCompletion = playerStats.getQuickestCompletion(
                                            playerStats.getMostCompletedLevel());

                                    if (fastestCompletion != null)
                                            loreString = loreString.replace("%fastest_completion%",
                                  (((double) fastestCompletion.getCompletionTimeElapsed()) / 1000) + "s");
                                }

                                if (loreString.contains("%favorite_level%") || loreString.contains("%fastest_completion%"))
                                    continue;

                                // now add the last part of the level stats
                                loreString = loreString.replace("%total_completions%", playerStats.getTotalLevelCompletions() + "")
                                        .replace("%levels_completed%", playerStats.getIndividualLevelsBeaten() + "")
                                        .replace("%total_levels%", Parkour.getLevelManager().getLevels().size() + "")
                                        .replace("%rated_levels_count%", playerStats.getRatedLevelsCount() + "");

                                // if they have a clan, check for clan item
                                if (clan != null) {

                                    // replace clan items
                                    loreString = loreString.replace("%clan_name%", clan.getTag())
                                            .replace("%clan_level%", clan.getLevel() + "")
                                            .replace("%clan_total_xp%", Utils.shortStyleNumber(clan.getTotalGainedXP()))
                                            .replace("%clan_level_xp%", Utils.shortStyleNumber(clan.getXP()))
                                            .replace("%clan_owner%", clan.getOwner().getPlayerName())
                                            .replace("%clan_member_count%", clan.getMembers().size() + "");

                                    // null it for clan
                                    if (loreString.contains("%clan_members%"))
                                        loreString = null;

                                // item loaded for clan is emerald
                                } else if (item.getType() == Material.EMERALD) {
                                    if (!alreadyCheckedClan) {
                                        newLore.add(Utils.translate("&7Not in a clan"));
                                        alreadyCheckedClan = true;
                                    }
                                    continue;
                                }

                                if (loreString != null)
                                    newLore.add(loreString);
                                    // this means clan members!
                                else for (ClanMember clanMember : clan.getMembers()) {

                                    // make string for online/offline
                                    String onlineStatus = "&cOffline";
                                    if (Bukkit.getPlayer(clanMember.getPlayerName()) != null)
                                        onlineStatus = "&aOnline";

                                    newLore.add(Utils.translate("  &7" + clanMember.getPlayerName() + " " + onlineStatus));
                                }
                            }
                            itemMeta.setLore(newLore);
                            item.setItemMeta(itemMeta);
                        }
                    }
                }
                newInventory.setItem(i, item);
            }
            opener.closeInventory();
            opener.openInventory(newInventory);
        }
    }
}