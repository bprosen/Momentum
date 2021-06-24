package com.parkourcraft.parkour.data.rank;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.storage.mysql.DatabaseQueries;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.*;

public class RanksManager {

    private static HashMap<String, Rank> rankList = new HashMap<>();

    public RanksManager() {
        load();
    }

    public void load() {
        rankList = new HashMap<>();

        for (String rankName : RanksYAML.getNames())
            load(rankName);

        updatePlayers();
        Parkour.getPluginLogger().info("Ranks loaded: " + rankList.size());
    }

    public void load(String rankName) {

        boolean exists = exists(rankName);

        if (!RanksYAML.exists(rankName) && exists)
            remove(rankName);
        else {
            if (exists)
                remove(rankName);

            add(rankName);
        }
    }

    public void add(String rankName) {
        // get from YAML
        String rankTitle = RanksYAML.getRankTitle(rankName);
        int rankId = RanksYAML.getRankId(rankName);
        double rankUpPrice = RanksYAML.getRankUpPrice(rankName);

        Rank rank = new Rank(rankName, rankTitle, rankId, rankUpPrice);
        rankList.put(rankName, rank);
    }

    public Rank get(int rankId) {
        for (Rank rank : rankList.values())
            if (rank.getRankId() == rankId)
                return rank;

        return null;
    }

    public Rank get(String rankName) {
        return rankList.get(rankName);
    }

    public boolean exists(String rankName) {
        return (get(rankName) != null);
    }

    public boolean exists(int rankId) {
        return (get(rankId) != null);
    }

    public Set<String> getNames() {
        return rankList.keySet();
    }

    public Set<Integer> getIDs() {
        Set<Integer> tempList = new HashSet<>();

        for (Rank rank : rankList.values())
            tempList.add(rank.getRankId());

        return tempList;
    }

    public void remove(String rankName) {
        for (Iterator<Rank> iterator = rankList.values().iterator(); iterator.hasNext();) {
            if (iterator.next().getRankName().equalsIgnoreCase(rankName)) {
                RanksYAML.remove(iterator.getClass().getName());
                iterator.remove();
            }
        }
    }

    public void resetPlayersInRank(Rank rank) {

        for (PlayerStats playerStats : Parkour.getStatsManager().getPlayerStats().values()) {
            // if in rank, then delete from database and lower rank by 1
            if (playerStats != null && playerStats.isLoaded() && playerStats.getPlayer().isOnline() &&
                playerStats.getRank().getRankId() == rank.getRankId()) {

                for (int i = playerStats.getRank().getRankId() - 1; i >= 2; i--) {
                    if (exists(i)) {
                        playerStats.setRank(get(i));
                        RanksDB.updateRank(playerStats.getPlayer().getUniqueId(), i);
                        break;
                    }
                }
            }
        }
    }

    public void updatePlayers() {

        // update online players ranks
        for (PlayerStats playerStats : Parkour.getStatsManager().getPlayerStats().values()) {

            if (playerStats != null && playerStats.isLoaded() && playerStats.getPlayer().isOnline()) {

                List<Map<String, String>> playerResults = DatabaseQueries.getResults(
                        "players",
                        "rank_id",
                        " WHERE uuid='" + playerStats.getUUID() + "'"
                );

                if (playerResults.size() > 0) {
                    for (Map<String, String> playerResult : playerResults) {

                        int rankID = Integer.parseInt(playerResult.get("rank_id"));
                        Rank rank = Parkour.getRanksManager().get(rankID);

                        if (rank != null)
                            playerStats.setRank(rank);
                    }
                }
            }
        }
    }

    public void doRankUp(Player player) {
        PlayerStats playerStats = Parkour.getStatsManager().get(player);
        int newId = playerStats.getRank().getRankId() + 1;
        Rank rank = get(newId);
        playerStats.setRank(rank);
        RanksDB.updateRank(player.getUniqueId(), newId);
        RanksDB.updateStage(player.getUniqueId(), 1);
        // play sound
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 8F, 2F);

        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(Utils.translate("&c&l" + player.getDisplayName() + " &7has ranked up to &c" + rank.getRankTitle()));
        Bukkit.broadcastMessage("");
    }

    public void doPrestige(Player player) {
        PlayerStats playerStats = Parkour.getStatsManager().get(player);
        Rank defaultRank = get(1);
        // update cache and database
        playerStats.setRank(defaultRank);
        // update prestige multiplier
        playerStats.setPrestigeMultiplier(playerStats.getPrestigeMultiplier() + (Parkour.getSettingsManager().prestige_multiplier_per_prestige / 100));
        // dont need to update stage as they will never hit stage 2 in max rank
        RanksDB.updateRank(player.getUniqueId(), 1);

        // now add prestige to stats and database
        playerStats.addPrestige();
        RanksDB.updatePrestiges(player.getUniqueId(), playerStats.getPrestiges());

        // add an s if its not one because im OCD with this
        String endingString = "time";
        if (playerStats.getPrestiges() > 1)
            endingString += "s";

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 8F, 2F);
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(Utils.translate("&c&l" + player.getDisplayName() + " &7has just &6&lPRESTIGED&7!" +
                                                     " &7They have prestiged &6" +
                                                     playerStats.getPrestiges() + " " + endingString + "!"));
        Bukkit.broadcastMessage("");
    }

    public HashMap<String, Rank> getRankList() {
        return rankList;
    }

    public boolean isMaxRank(Rank rank) {
        if (rank.getRankId() == getMaxRank().getRankId())
            return true;
        return false;
    }

    public Rank getMaxRank() {

        Rank currentMax = null;

        for (Rank rank : rankList.values()) {
            if (currentMax == null || currentMax.getRankId() < rank.getRankId())
                currentMax = rank;
        }
        return currentMax;
    }
}