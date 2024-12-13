package com.renatusnetwork.momentum.data.placeholders;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.clans.Clan;
import com.renatusnetwork.momentum.data.leaderboards.*;
import com.renatusnetwork.momentum.data.infinite.gamemode.InfiniteType;
import com.renatusnetwork.momentum.data.levels.Level;
import com.renatusnetwork.momentum.utils.Utils;

import java.util.ArrayList;

public class LBPlaceholders {

    public static final String LB_PREFIX = "lb";

    public static String processPlaceholder(String placeholder) {
        String[] split = placeholder.split("_");

        if (split.length == 3) {
            String type = split[0];
            String position = split[1];
            String value = split[2].toLowerCase();

            if (Utils.isInteger(position)) {
                int posInt = Integer.parseInt(position) - 1;

                if (posInt >= 0 && posInt <= 9) {
                    switch (type) {
                        case "races": {
                            ArrayList<RaceLBPosition> leaderboard = Momentum.getRaceManager().getLeaderboard();
                            if (leaderboard.size() > posInt) {
                                RaceLBPosition raceLBPosition = leaderboard.get(posInt);

                                if (raceLBPosition != null) {
                                    // return name or value
                                    if (value.equals("name")) {
                                        return raceLBPosition.getName();
                                    } else if (value.equals("wins")) {
                                        return Utils.formatNumber(raceLBPosition.getWins());
                                    } else if (value.equals("winrate")) {
                                        return String.valueOf(raceLBPosition.getWinRate());
                                    }
                                }
                            }
                            break;
                        }
                        case "toprated": {
                            ArrayList<Level> leaderboard = Momentum.getLevelManager().getTopRatedLevelsLB();
                            if (leaderboard.size() > posInt) {
                                Level level = leaderboard.get(posInt);

                                if (level != null) {
                                    // return name or value
                                    if (value.equals("title")) {
                                        return level.getFormattedTitle();
                                    } else if (value.equals("rating")) {
                                        return String.valueOf(level.getRating());
                                    }
                                }
                            }
                            break;
                        }
                        case "clans": {
                            ArrayList<Clan> leaderboard = Momentum.getClansManager().getLeaderboard();
                            if (leaderboard.size() > posInt) {
                                Clan clan = leaderboard.get(posInt);

                                if (clan != null) {
                                    // return name or value
                                    if (value.equals("xp")) {
                                        return Utils.shortStyleNumber(clan.getTotalXP());
                                    } else if (value.equals("name")) {
                                        return clan.getTag();
                                    } else if (value.equals("owner")) {
                                        return clan.getOwner().getName();
                                    }
                                }
                            }
                            break;
                        }
                        case "players": {
                            ArrayList<GlobalPersonalLBPosition> leaderboard = Momentum.getStatsManager().getGlobalPersonalCompletionsLB();
                            if (leaderboard.size() > posInt) {
                                GlobalPersonalLBPosition lbPos = leaderboard.get(posInt);

                                if (lbPos != null) {
                                    if (value.equals("name")) {
                                        return lbPos.getName();
                                    } else if (value.equals("completions")) {
                                        return Utils.formatNumber(lbPos.getCompletions());
                                    }
                                }
                            }
                            break;
                        }
                        case "levels": {
                            ArrayList<Level> leaderboard = Momentum.getLevelManager().getGlobalLevelCompletionsLB();

                            if (leaderboard.size() > posInt) {
                                Level level = Momentum.getLevelManager().getGlobalLevelCompletionsLB().get(posInt);

                                if (level != null) {
                                    // return name or value
                                    if (value.equals("title")) {
                                        return level.getFormattedTitle();
                                    } else if (value.equals("completions")) {
                                        return Utils.formatNumber(level.getTotalCompletionsCount());
                                    }
                                }
                            }
                            break;
                        }
                        case "coins": {
                            ArrayList<CoinsLBPosition> leaderboard = Momentum.getStatsManager().getCoinsLB();

                            if (leaderboard.size() > posInt) {
                                CoinsLBPosition lbPos = leaderboard.get(posInt);

                                if (lbPos != null) {
                                    if (value.equals("name")) {
                                        return lbPos.getName();
                                    } else if (value.equals("coins")) {
                                        return Utils.formatNumber(lbPos.getCoins());
                                    }
                                }
                            }
                            break;
                        }
                        case "records": {
                            ArrayList<RecordsLBPosition> leaderboard = Momentum.getLevelManager().getRecordsLB();

                            if (leaderboard.size() > posInt) {
                                RecordsLBPosition lbPos = leaderboard.get(posInt);

                                if (lbPos != null) {
                                    if (value.equals("name")) {
                                        return lbPos.getName();
                                    } else if (value.equals("records")) {
                                        return Utils.formatNumber(lbPos.getRecords());
                                    }
                                }
                            }
                            break;
                        }
                        case "events": {
                            ArrayList<EventLBPosition> leaderboard = Momentum.getEventManager().getEventLeaderboard();

                            if (leaderboard.size() > posInt) {
                                EventLBPosition eventLBPosition = leaderboard.get(posInt);

                                if (eventLBPosition != null) {
                                    // return name or value
                                    if (value.equals("name")) {
                                        return eventLBPosition.getName();
                                    } else if (value.equals("wins")) {
                                        return Utils.formatNumber(eventLBPosition.getWins());
                                    }
                                }
                            }
                            break;
                        }
                        case "elo": {
                            ArrayList<ELOLBPosition> leaderboard = Momentum.getStatsManager().getELOLB();

                            if (leaderboard.size() > posInt) {
                                ELOLBPosition elolbPosition = leaderboard.get(posInt);

                                if (elolbPosition != null) {
                                    // return name or value
                                    if (value.equals("name")) {
                                        return elolbPosition.getName();
                                    } else if (value.equals("elo")) {
                                        return Utils.formatNumber(elolbPosition.getELO());
                                    }
                                }
                            }
                            break;
                        }
                        default: {
                            Level level = Momentum.getLevelManager().get(type);

                            // stats type!
                            if (level != null) {
                                if (!Momentum.getLevelManager().isLoadingLeaderboards()) {
                                    if (level.getLeaderboard().isEmpty()) {
                                        return "N/A";
                                    }

                                    LevelLBPosition completion = level.getLeaderboard().get(posInt);

                                    // return name or value
                                    if (value.equals("name")) {
                                        return completion.getPlayerName();
                                    } else if (value.equals("time")) {
                                        return String.valueOf(completion.getTimeTakenSeconds());
                                    }
                                }
                            } else {
                                return type + " is not a level!";
                            }
                        }
                    }
                } else {
                    return "Out of bounds of leaderboards";
                }
            }
        } else if (split.length == 4) {
            String lbType = split[0].toLowerCase();
            String specification = split[1];
            String position = split[2];
            String value = split[3].toLowerCase();

            if (lbType.equals("infinite")) {
                if (Utils.isInteger(position)) {
                    int posInt = Integer.parseInt(position);
                    try {
                        InfiniteType type = InfiniteType.valueOf(specification.toUpperCase());
                        InfiniteLBPosition infiniteLBPosition = Momentum.getInfiniteManager().getLeaderboard(type).getLeaderboardPosition(posInt);

                        if (infiniteLBPosition != null) {
                            // return name or value
                            if (value.equals("name")) {
                                return infiniteLBPosition.getName();
                            } else if (value.equals("score")) {
                                return Utils.formatNumber(infiniteLBPosition.getScore());
                            }
                        }
                    } catch (IllegalArgumentException exception) {
                        return "Invalid infinite type";
                    }
                }
            }
        }
        return "";
    }
}
