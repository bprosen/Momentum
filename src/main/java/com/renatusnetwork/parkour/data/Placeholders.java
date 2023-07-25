package com.renatusnetwork.parkour.data;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.clans.Clan;
import com.renatusnetwork.parkour.data.events.EventLBPosition;
import com.renatusnetwork.parkour.data.infinite.InfinitePKLBPosition;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.races.RaceLBPosition;
import com.renatusnetwork.parkour.data.ranks.Rank;
import com.renatusnetwork.parkour.data.stats.*;
import com.renatusnetwork.parkour.utils.Utils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import java.util.Map;

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
        if (placeholder.equals("global_completions"))
            return Utils.formatNumber(Parkour.getLevelManager().getTotalLevelCompletions());
        else if (placeholder.equals("total_coins"))
            return String.format("%,d", Parkour.getStatsManager().getTotalCoins());

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
                            InfinitePKLBPosition infinitePKLBPosition = Parkour.getInfinitePKManager().getLeaderboard().get(posInt);

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
                            RaceLBPosition raceLBPosition = Parkour.getRaceManager().getLeaderboard().get(posInt);

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
                            Level level = Parkour.getLevelManager().getTopRatedLevelsLB().get(posInt);

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
                            Clan clan = Parkour.getClansManager().getLeaderboard().get(posInt);

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
                            GlobalPersonalLBPosition lbPos = Parkour.getStatsManager().getGlobalPersonalCompletionsLB().get(posInt);

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
                            Level level = Parkour.getLevelManager().getGlobalLevelCompletionsLB().get(posInt);

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
                            CoinsLBPosition lbPos = Parkour.getStatsManager().getCoinsLB().get(posInt);

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
                            RecordsLBPosition lbPos = Parkour.getStatsManager().getRecordsLB().get(posInt);

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
                            EventLBPosition eventLBPosition = Parkour.getEventManager().getEventLeaderboard().get(posInt);

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
                            Level level = Parkour.getLevelManager().get(type);

                            // stats type!
                            if (level != null)
                            {
                                if (!Parkour.getStatsManager().isLoadingLeaderboards())
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
