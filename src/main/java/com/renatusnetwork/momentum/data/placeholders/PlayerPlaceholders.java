package com.renatusnetwork.momentum.data.placeholders;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.infinite.gamemode.InfiniteType;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.entity.Player;

public class PlayerPlaceholders
{
    public static final String PLAYER_PREFIX = "player";

    public static String processPlaceholder(Player player, String placeholder)
    {
        PlayerStats playerStats = Momentum.getStatsManager().get(player);

        // per player placeholders
        if (playerStats != null)
        {
            // all placeholders
            switch (placeholder.toLowerCase())
            {
                case "records":
                    return Utils.formatNumber(playerStats.getNumRecords());
                case "coins":
                    return Utils.formatNumber(playerStats.getCoins());
                case "rank":
                    return playerStats.getRank() == null ? "None" : Utils.translate(playerStats.getRank().getTitle());
                case "clan":
                    return playerStats.getClan() == null ? "None" : playerStats.getClan().getTag();
                case "total_completions":
                    return Utils.formatNumber(playerStats.getTotalLevelCompletions());
                case "mastery_completions":
                    return Utils.formatNumber(playerStats.getNumMasteryCompletions());
                case "race_wins":
                    return Utils.formatNumber(playerStats.getRaceWins());
                case "race_losses":
                    return Utils.formatNumber(playerStats.getRaceLosses());
                case "race_winrate":
                    return String.valueOf(playerStats.getRaceWinRate());
                case "best_classic_infinite":
                    return Utils.formatNumber(playerStats.getBestInfiniteScore(InfiniteType.CLASSIC));
                case "best_sprint_infinite":
                    return Utils.formatNumber(playerStats.getBestInfiniteScore(InfiniteType.SPRINT));
                case "best_timed_infinite":
                    return Utils.formatNumber(playerStats.getBestInfiniteScore(InfiniteType.TIMED));
                case "best_speedrun_infinite":
                    return Utils.formatNumber(playerStats.getBestInfiniteScore(InfiniteType.SPEEDRUN));
                case "event_wins":
                    return Utils.formatNumber(playerStats.getEventWins());
                case "elo":
                    return Utils.formatNumber(playerStats.getELO());
                case "elo_tier":
                    return Utils.translate(playerStats.getELOTierTitleWithLB());
            }
        }
        return "";
    }
}
