package com.renatusnetwork.parkour.data;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.ranks.Rank;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.data.stats.StatsManager;
import com.renatusnetwork.parkour.utils.Utils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class Placeholders extends PlaceholderExpansion
{
    private final StatsManager statsManager;

    public Placeholders()
    {
        statsManager = Parkour.getStatsManager();
    }

    @Override
    public String getAuthor()
    {
        return "xxBen";
    }

    @Override
    public String getIdentifier()
    {
        return "rn-parkour";
    }

    @Override
    public String getVersion()
    {
        return "1.0.0";
    }

    @Override
    public boolean persist()
    {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String placeholder)
    {
        PlayerStats playerStats = statsManager.get(player);

        // load if not null
        if (playerStats != null)
        {
            // all placeholders
            switch (placeholder)
            {
                case "records":
                    return Utils.formatNumber(playerStats.getRecords());
                case "coins":
                    return Utils.formatNumber((int) playerStats.getCoins());
                case "rank":
                    return playerStats.getRank() == null ? "None" : Utils.translate(playerStats.getRank().getRankTitle());
                case "clan":
                    return playerStats.getClan() == null ? "None" : playerStats.getClan().getTag();
                case "total_completions":
                    return Utils.formatNumber(playerStats.getTotalLevelCompletions());
                case "race_wins":
                    return Utils.formatNumber(playerStats.getRaceWins());
                case "race_losses":
                    return Utils.formatNumber(playerStats.getRaceLosses());
                case "best_infinite":
                    return Utils.formatNumber(playerStats.getInfinitePKScore());
            }
        }

        return null;
    }
}
