package com.renatusnetwork.momentum.data;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.clans.Clan;
import com.renatusnetwork.momentum.data.events.EventLBPosition;
import com.renatusnetwork.momentum.data.infinite.InfinitePKLBPosition;
import com.renatusnetwork.momentum.data.levels.Level;
import com.renatusnetwork.momentum.data.races.RaceLBPosition;
import com.renatusnetwork.momentum.data.stats.*;
import com.renatusnetwork.momentum.utils.Utils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class Placeholders extends PlaceholderExpansion
{
    private final StatsManager statsManager;

    public Placeholders()
    {
        statsManager = Momentum.getStatsManager();
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
        if (placeholder.equals("global_completions"))
            return Utils.formatNumber(Momentum.getLevelManager().getTotalLevelCompletions());
        else if (placeholder.equals("total_coins"))
            return String.format("%,d", Momentum.getStatsManager().getTotalCoins());

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
                case "race_winrate":
                    return String.valueOf(playerStats.getRaceWinRate());
                case "best_infinite":
                    return Utils.formatNumber(playerStats.getInfinitePKScore());
                case "shortened_rank":
                    return playerStats.getRank() == null ? "" : Utils.translate(playerStats.getRank().getShortRankTitle());
                case "event_wins":
                    return Utils.formatNumber(playerStats.getEventWins());
            }
        }

        if (placeholder.startsWith("lb"))
        {
            String[] split = placeholder.split("_");

            if (split.length == 4)
            {
                String type = split[1];
                String position = split[2];
                String value = split[3];

                if (Utils.isInteger(position))
                {
                    int posInt = Integer.parseInt(position);

                    if (posInt >= 1 && posInt <= 10)
                    {
                        if (type.equals("infinite"))
                        {
                            InfinitePKLBPosition infinitePKLBPosition = Momentum.getInfinitePKManager().getLeaderboard().get(posInt);

                            if (infinitePKLBPosition != null)
                            {
                                // return name or value
                                if (value.equals("name"))
                                    return infinitePKLBPosition.getName();
                                else if (value.equals("score"))
                                    return Utils.formatNumber(infinitePKLBPosition.getScore());
                            }
                        }
                        else if (type.equals("races"))
                        {
                            RaceLBPosition raceLBPosition = Momentum.getRaceManager().getLeaderboard().get(posInt);

                            if (raceLBPosition != null)
                            {
                                // return name or value
                                if (value.equals("name"))
                                    return raceLBPosition.getName();
                                else if (value.equals("wins"))
                                    return Utils.formatNumber(raceLBPosition.getWins());
                                else if (value.equals("winrate"))
                                    return String.valueOf(raceLBPosition.getWinRate());
                            }
                        }
                        else if (type.equals("toprated"))
                        {
                            Level level = Momentum.getLevelManager().getTopRatedLevelsLB().get(posInt);

                            if (level != null)
                            {
                                // return name or value
                                if (value.equals("title"))
                                    return level.getFormattedTitle();
                                else if (value.equals("rating"))
                                    return String.valueOf(level.getRating());
                            }
                        }
                        else if (type.equals("clans"))
                        {
                            Clan clan = Momentum.getClansManager().getLeaderboard().get(posInt);

                            if (clan != null)
                            {
                                // return name or value
                                if (value.equals("xp"))
                                    return Utils.shortStyleNumber(clan.getTotalGainedXP());
                                else if (value.equals("name"))
                                    return clan.getTag();
                                else if (value.equals("owner"))
                                    return clan.getOwner().getPlayerName();
                            }
                        }
                        else if (type.equals("players"))
                        {
                            GlobalPersonalLBPosition lbPos = Momentum.getStatsManager().getGlobalPersonalCompletionsLB().get(posInt);

                            if (lbPos != null)
                            {
                                if (value.equals("name"))
                                    return lbPos.getName();
                                else if (value.equals("completions"))
                                    return Utils.formatNumber(lbPos.getCompletions());
                            }
                        }
                        else if (type.equals("levels"))
                        {
                            Level level = Momentum.getLevelManager().getGlobalLevelCompletionsLB().get(posInt);

                            if (level != null)
                            {
                                // return name or value
                                if (value.equals("title"))
                                    return level.getFormattedTitle();
                                else if (value.equals("completions"))
                                    return Utils.formatNumber(level.getTotalCompletionsCount());
                            }
                        }
                        else if (type.equals("coins"))
                        {
                            CoinsLBPosition lbPos = Momentum.getStatsManager().getCoinsLB().get(posInt);

                            if (lbPos != null)
                            {
                                if (value.equals("name"))
                                    return lbPos.getName();
                                else if (value.equals("coins"))
                                    return Utils.formatNumber(lbPos.getCoins());
                            }
                        }
                        else if (type.equals("records"))
                        {
                            RecordsLBPosition lbPos = Momentum.getStatsManager().getRecordsLB().get(posInt);

                            if (lbPos != null)
                            {
                                if (value.equals("name"))
                                    return lbPos.getName();
                                else if (value.equals("records"))
                                    return Utils.formatNumber(lbPos.getRecords());
                            }
                        }
                        else if (type.equals("events"))
                        {
                            EventLBPosition eventLBPosition = Momentum.getEventManager().getEventLeaderboard().get(posInt);

                            if (eventLBPosition != null)
                            {
                                // return name or value
                                if (value.equals("name"))
                                    return eventLBPosition.getName();
                                else if (value.equals("wins"))
                                    return Utils.formatNumber(eventLBPosition.getWins());
                            }
                        }
                        else
                        {
                            Level level = Momentum.getLevelManager().get(type);

                            // stats type!
                            if (level != null)
                            {
                                if (!Momentum.getStatsManager().isLoadingLeaderboards())
                                {
                                    LevelCompletion completion = level.getLeaderboard().get(posInt - 1); // adjust for index

                                    // return name or value
                                    if (value.equals("name"))
                                        return completion.getPlayerName();
                                    else if (value.equals("time"))
                                        return String.valueOf(((double) completion.getCompletionTimeElapsed()) / 1000);
                                }
                            }
                            else
                            {
                                return type + " is not a level!";
                            }
                        }
                    }
                    else
                    {
                        return "Out of bounds of leaderboards";
                    }
                }
            }
        }
        return "";
    }
}
