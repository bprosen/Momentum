package com.renatusnetwork.parkour.data.ranks;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.commands.SpawnCMD;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.data.stats.StatsDB;
import com.renatusnetwork.parkour.storage.mysql.DatabaseManager;
import com.renatusnetwork.parkour.storage.mysql.DatabaseQueries;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.*;

public class RanksManager {

    private static HashMap<String, Rank> ranks = new HashMap<>();

    public RanksManager() {
        load();
    }

    public void load() {
        ranks = RanksDB.loadRanks();

        Parkour.getPluginLogger().info("Ranks loaded: " + ranks.size());
    }

    public void add(String rankName)
    {
        Rank rank = new Rank(rankName);
        ranks.put(rankName, rank);
    }

    public Rank get(String rankName) {
        return ranks.get(rankName);
    }

    public boolean exists(String rankName) {
        return (get(rankName) != null);
    }

    public Set<String> getNames() {
        return ranks.keySet();
    }

    public void remove(String rankName)
    {
        ranks.remove(rankName);
    }

    public void updatePrestiges(PlayerStats playerStats, int prestiges)
    {
        RanksDB.updatePrestiges(playerStats.getUUID(), prestiges);
        playerStats.setPrestiges(prestiges);
    }

    public void resetPlayersInRank(Rank rank)
    {
        Rank defaultRank = Parkour.getRanksManager().get(Parkour.getSettingsManager().default_rank);
        HashMap<String, PlayerStats> players = Parkour.getStatsManager().getPlayerStats();

        // thread safety
        synchronized (players)
        {
            for (PlayerStats playerStats : players.values())
                // if in rank, reset them to default rank
                if (playerStats != null && playerStats.getRank().equals(rank))
                    playerStats.setRank(defaultRank);
        }
    }

    public void doRankUp(Player player)
    {
        PlayerStats playerStats = Parkour.getStatsManager().get(player);

        String nextRank = playerStats.getRank().getNextRank();
        Rank rank = get(nextRank);
        playerStats.setRank(rank);
        StatsDB.updateRank(player.getUniqueId().toString(), nextRank);
        // play sound
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2f, 0f);

        Bukkit.broadcastMessage(Utils.translate("&c" + player.getDisplayName() + " &7has ranked up to &c" + rank.getTitle()));
        Parkour.getStatsManager().runGGTimer();
    }

    public void doPrestige(PlayerStats playerStats, double cost)
    {
        Player player = playerStats.getPlayer();
        Rank defaultRank = get(Parkour.getSettingsManager().default_rank);
        // update cache and database
        playerStats.setRank(defaultRank);
        playerStats.addPrestige();

        Parkour.getStatsManager().removeCoins(playerStats, cost);

        // update prestige multiplier
        float prestigeMultiplier = Parkour.getSettingsManager().prestige_multiplier_per_prestige * playerStats.getPrestiges();

        if (prestigeMultiplier >= Parkour.getSettingsManager().max_prestige_multiplier)
            prestigeMultiplier = Parkour.getSettingsManager().max_prestige_multiplier;

        prestigeMultiplier = (float) (1.00 + (prestigeMultiplier / 100));
        playerStats.setPrestigeMultiplier(prestigeMultiplier);

        // dont need to update stage as they will never hit stage 2 in max rank
        StatsDB.updateRank(player.getUniqueId().toString(), defaultRank.getName());

        // now add prestige db
        RanksDB.updatePrestiges(player.getUniqueId().toString(), playerStats.getPrestiges());

        // add an s if its not one because im OCD with this
        String endingString = "time";
        if (playerStats.getPrestiges() > 1)
            endingString += "s";

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 8F, 2F);
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(Utils.translate("&c" + player.getDisplayName() + " &7has just &6&lPRESTIGED&7!" +
                                                     " &7They have prestiged &6" +
                                                     playerStats.getPrestiges() + " " + endingString + "!"));
        Bukkit.broadcastMessage("");
        Parkour.getStatsManager().runGGTimer();
    }

    public HashMap<String, Rank> getRanks() {
        return ranks;
    }

    public Rank getNextRank(Rank current)
    {
        Rank rank = current;

        if (!rank.isMaxRank())
            rank = get(rank.getNextRank());

        return rank;
    }

    public void enteredRankup(PlayerStats playerStats)
    {
        playerStats.setAttemptingRankup(true);
        DatabaseQueries.runAsyncQuery("UPDATE players SET attempting_rankup=1 WHERE uuid='" + playerStats.getUUID() + "'");
    }

    public void leftRankup(PlayerStats playerStats)
    {
        playerStats.setAttemptingRankup(false);
        DatabaseQueries.runAsyncQuery("UPDATE players SET attempting_rankup=0 WHERE uuid='" + playerStats.getUUID() + "'");
    }

    public boolean isPastOrAtRank(PlayerStats playerStats, Rank current)
    {
        if (playerStats != null && playerStats.getRank() != null)
        {
            // if they have prestiged already
            if (playerStats.hasPrestiges())
                return true;

            // keep recursively going through the ranks until we reach the end of the rank
            while (current != null)
            {
                if (playerStats.getRank().equals(current))
                    return true;

                current = get(current.getNextRank());
            }
        }
        return false;
    }

    public boolean isPastOrAtRank(PlayerStats playerStats, String currentString)
    {
        return isPastOrAtRank(playerStats, get(currentString));
    }

    public Rank getMaxRank()
    {
        for (Rank rank : ranks.values())
            if (rank.isMaxRank())
                return rank;

        return null;
    }
}