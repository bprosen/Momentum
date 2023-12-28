package com.renatusnetwork.parkour.data.placeholders;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.clans.Clan;
import com.renatusnetwork.parkour.data.leaderboards.EventLBPosition;
import com.renatusnetwork.parkour.data.infinite.gamemode.InfiniteType;
import com.renatusnetwork.parkour.data.leaderboards.InfiniteLBPosition;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.leaderboards.RaceLBPosition;
import com.renatusnetwork.parkour.data.levels.LevelCompletion;
import com.renatusnetwork.parkour.data.leaderboards.CoinsLBPosition;
import com.renatusnetwork.parkour.data.leaderboards.GlobalPersonalLBPosition;
import com.renatusnetwork.parkour.data.leaderboards.RecordsLBPosition;
import com.renatusnetwork.parkour.utils.Utils;

public class LBPlaceholders
{
    public static final String LB_PREFIX = "lb";

    public static String processPlaceholder(String placeholder)
    {
        String[] split = placeholder.split("_");

        if (split.length == 3)
        {
            String type = split[0];
            String position = split[1];
            String value = split[2].toLowerCase();

            if (Utils.isInteger(position))
            {
                int posInt = Integer.parseInt(position);

                if (posInt >= 1 && posInt <= 10)
                {
                    switch (type)
                    {
                        case "races":
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
                            break;
                        }
                        case "toprated":
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
                            break;
                        }
                        case "clans":
                        {
                            Clan clan = Parkour.getClansManager().getLeaderboard().get(posInt);

                            if (clan != null)
                            {
                                // return name or value
                                if (value.equals("xp"))
                                    return Utils.shortStyleNumber(clan.getTotalXP());
                                else if (value.equals("name"))
                                    return clan.getTag();
                                else if (value.equals("owner"))
                                    return clan.getOwner().getName();
                            }
                            break;
                        }
                        case "players":
                        {
                            GlobalPersonalLBPosition lbPos = Parkour.getStatsManager().getGlobalPersonalCompletionsLB().get(posInt);

                            if (lbPos != null)
                            {
                                if (value.equals("name"))
                                    return lbPos.getName();
                                else if (value.equals("completions"))
                                    return Utils.formatNumber(lbPos.getCompletions());
                            }
                            break;
                        }
                        case "levels":
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
                            break;
                        }
                        case "coins":
                        {
                            CoinsLBPosition lbPos = Parkour.getStatsManager().getCoinsLB().get(posInt);

                            if (lbPos != null)
                            {
                                if (value.equals("name"))
                                    return lbPos.getName();
                                else if (value.equals("coins"))
                                    return Utils.formatNumber(lbPos.getCoins());
                            }
                            break;
                        }
                        case "records":
                        {
                            RecordsLBPosition lbPos = Parkour.getStatsManager().getRecordsLB().get(posInt);

                            if (lbPos != null)
                            {
                                if (value.equals("name"))
                                    return lbPos.getName();
                                else if (value.equals("records"))
                                    return Utils.formatNumber(lbPos.getRecords());
                            }
                            break;
                        }
                        case "events":
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
                            break;
                        }
                        default:
                        {
                            Level level = Parkour.getLevelManager().get(type);

                            // stats type!
                            if (level != null)
                            {
                                if (!Parkour.getStatsManager().isLoadingLeaderboards())
                                {
                                    if (level.getLeaderboard().isEmpty())
                                        return "N/A";

                                    LevelCompletion completion = level.getLeaderboard().get(posInt - 1); // adjust for index

                                    // return name or value
                                    if (value.equals("name"))
                                        return completion.getName();
                                    else if (value.equals("time"))
                                        return String.valueOf(completion.getCompletionTimeElapsedSeconds());
                                }
                            }
                            else
                            {
                                return type + " is not a level!";
                            }
                        }
                    }
                }
                else
                {
                    return "Out of bounds of leaderboards";
                }
            }
        }
        else if (split.length == 4)
        {
            String lbType = split[0].toLowerCase();
            String specification = split[1];
            String position = split[2];
            String value = split[3].toLowerCase();

            if (lbType.equals("infinite"))
            {
                if (Utils.isInteger(position))
                {
                    int posInt = Integer.parseInt(position);
                    try
                    {
                        InfiniteType type = InfiniteType.valueOf(specification.toUpperCase());
                        InfiniteLBPosition infiniteLBPosition = Parkour.getInfiniteManager().getLeaderboard(type).getLeaderboardPosition(posInt);

                        if (infiniteLBPosition != null)
                        {
                            // return name or value
                            if (value.equals("name"))
                                return infiniteLBPosition.getName();
                            else if (value.equals("score"))
                                return Utils.formatNumber(infiniteLBPosition.getScore());
                        }
                    }
                    catch (IllegalArgumentException exception)
                    {
                        return "Invalid infinite type";
                    }
                }
            }
        }
        return "";
    }
}
