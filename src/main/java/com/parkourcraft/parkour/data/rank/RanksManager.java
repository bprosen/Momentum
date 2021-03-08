package com.parkourcraft.parkour.data.rank;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.data.stats.Stats_DB;
import com.parkourcraft.parkour.storage.mysql.DatabaseQueries;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RanksManager {

    private static List<Rank> rankList = new ArrayList<>();

    public RanksManager() {
        load();
    }

    public void load() {
        rankList = new ArrayList<>();

        for (String rankName : Ranks_YAML.getNames())
            load(rankName);

        updatePlayers();
        Parkour.getPluginLogger().info("Ranks loaded: " + rankList.size());
    }

    public void load(String rankName) {

        boolean exists = exists(rankName);

        if (!Ranks_YAML.exists(rankName) && exists)
            remove(rankName);
        else {
            if (exists)
                remove(rankName);

            add(rankName);
        }
    }

    public void add(String rankName) {
        // get from YAML
        String rankTitle = Ranks_YAML.getRankTitle(rankName);
        int rankId = Ranks_YAML.getRankId(rankName);
        double rankUpPrice = Ranks_YAML.getRankUpPrice(rankName);

        Rank rank = new Rank(rankName, rankTitle, rankId, rankUpPrice);
        rankList.add(rank);
    }

    public Rank get(int rankId) {
        for (Rank rank : rankList)
            if (rank.getRankId() == rankId)
                return rank;

        return null;
    }

    public Rank get(String rankName) {
        for (Rank rank : rankList)
            if (rank.getRankName().equalsIgnoreCase(rankName))
                return rank;

        return null;
    }

    public boolean exists(String rankName) {
        return (get(rankName) != null);
    }

    public boolean exists(int rankId) {
        return (get(rankId) != null);
    }

    public List<String> getNames() {
        List<String> tempList = new ArrayList<>();

        for (Rank rank : rankList)
            tempList.add(rank.getRankName());

        return tempList;
    }

    public List<Integer> getIDs() {
        List<Integer> tempList = new ArrayList<>();

        for (Rank rank : rankList)
            tempList.add(rank.getRankId());

        return tempList;
    }

    public void remove(String rankName) {
        for (Iterator<Rank> iterator = rankList.iterator(); iterator.hasNext();) {
            if (iterator.next().getRankName().equalsIgnoreCase(rankName)) {
                Ranks_YAML.remove(iterator.getClass().getName());
                iterator.remove();
            }
        }
    }

    public void resetPlayersInRank(Rank rank) {

        for (PlayerStats playerStats : Parkour.getStatsManager().getPlayerStats()) {
            if (playerStats != null && playerStats.isLoaded() && playerStats.getPlayer().isOnline()) {

                // if in rank, then delete from database and lower rank by 1
                if (playerStats.getRank().getRankId() == rank.getRankId()) {
                    for (int i = playerStats.getRank().getRankId() - 1; i >= 2; i--) {
                        if (exists(i)) {
                            playerStats.setRank(get(i));
                            Ranks_DB.updateRank(playerStats.getPlayer().getUniqueId(), i);
                            break;
                        }
                    }
                }
            }
        }
    }

    public void updatePlayers() {

        // update online players ranks
        for (PlayerStats playerStats : Parkour.getStatsManager().getPlayerStats()) {

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
        Ranks_DB.updateRank(player.getUniqueId(), newId);

        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(Utils.translate("&c&l" + player.getDisplayName() + " &7has ranked up to &c" + rank.getRankTitle()));
        Bukkit.broadcastMessage("");
    }

    public List<Rank> getRankList() {
        return rankList;
    }

}