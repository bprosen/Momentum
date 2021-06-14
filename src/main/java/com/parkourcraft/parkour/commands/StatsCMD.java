package com.parkourcraft.parkour.commands;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.infinite.InfinitePKLBPosition;
import com.parkourcraft.parkour.data.levels.Level;
import com.parkourcraft.parkour.data.stats.LevelCompletion;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class StatsCMD implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (a.length > 0) {
            // infinite pk lb
            if (a.length == 1 && a[0].equalsIgnoreCase("infinite")) {

                if (!Parkour.getInfinitePKManager().getLeaderboard().isEmpty()) {
                    sender.sendMessage(Utils.translate("&5Infinite Parkour &7Leaderboard"));

                    for (int i = 1; i <= Parkour.getSettingsManager().max_infinitepk_leaderboard_size; i++) {

                        InfinitePKLBPosition lbPosition = Parkour.getInfinitePKManager().getLeaderboardPosition(i);

                        if (lbPosition != null) {
                            sender.sendMessage(Utils.translate(" &7" +
                                    lbPosition.getPosition() + " &5" +
                                    Utils.formatNumber(lbPosition.getScore()) + " &d" +
                                    lbPosition.getName()));
                        }
                    }

                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        PlayerStats playerStats = Parkour.getStatsManager().get(player.getUniqueId().toString());
                        sender.sendMessage(Utils.translate("&7Your best &d" + Utils.formatNumber(playerStats.getInfinitePKScore())));
                    }
                } else {
                    sender.sendMessage(Utils.translate("&cNo infinite leaderboard positions found"));
                }
            } else if (a.length == 1 && a[0].equalsIgnoreCase("levels")) {

                LinkedHashSet<Level> globalLevelCompletionsLB = Parkour.getLevelManager().getGlobalLevelCompletionsLB();

                if (!globalLevelCompletionsLB.isEmpty()) {

                    sender.sendMessage(Utils.translate("&4Global Level Completions &7Leaderboard"));

                    int lbPositionNum = 1;
                    for (Level lbPosition : globalLevelCompletionsLB) {

                        if (lbPosition != null) {
                            sender.sendMessage(Utils.translate(" &7" +
                                    lbPositionNum + " &4" +
                                    Utils.shortStyleNumber(lbPosition.getTotalCompletionsCount()) + " &c" +
                                    lbPosition.getFormattedTitle()));
                            lbPositionNum++;
                        }
                    }
                    sender.sendMessage(Utils.translate("&7Global Completions &c" + Utils.formatNumber(Parkour.getLevelManager().getTotalLevelCompletions())));
                } else {
                    sender.sendMessage(Utils.translate("&cLevels leaderboard not loaded fully"));
                }
            // level lb
            } else {

                String levelName = a[0];

                Level level = Parkour.getLevelManager().get(levelName);

                if (level != null) {
                    sender.sendMessage(Utils.translate(level.getFormattedTitle() + " &7Leaderboard"));
                    List<LevelCompletion> completions = level.getLeaderboard();

                    if (completions.size() > 0)
                        for (int i = 0; i <= completions.size() - 1; i++) {
                            LevelCompletion levelCompletion = completions.get(i);
                            int rank = i + 1;
                            sender.sendMessage(Utils.translate(" &7" + rank + " &2" +
                                    (((double) levelCompletion.getCompletionTimeElapsed()) / 1000) + "s &a" +
                                    levelCompletion.getPlayerName()));
                        }
                    else
                        sender.sendMessage(Utils.translate("&cNo timed completions to display"));

                    int totalCompletionsCount = level.getTotalCompletionsCount();
                    String outOfMessage = Utils.translate("&7Out of &2" + totalCompletionsCount);

                    sender.sendMessage(outOfMessage);
                } else {
                    sender.sendMessage(Utils.translate("&7No level named '&c" + levelName + "&7' exists"));
                }
            }
        } else if (sender instanceof Player) {
            Player player = (Player) sender;
            PlayerStats playerStats = Parkour.getStatsManager().get(player);

            if (playerStats != null) {
                Map<String, List<LevelCompletion>> levelCompletionsMap = playerStats.getLevelCompletionsMap();

                for (Map.Entry<String, List<LevelCompletion>> levelCompletionsEntry : levelCompletionsMap.entrySet()) {
                    List<LevelCompletion> levelCompletionsList = levelCompletionsEntry.getValue();
                    Level level = Parkour.getLevelManager().get(levelCompletionsEntry.getKey());

                    if (level != null) {

                        LevelCompletion levelCompletion = levelCompletionsList.get(0);

                        if (levelCompletion.getCompletionTimeElapsed() > 0) {
                            String levelCompletions = Utils.translate(level.getFormattedTitle() + "  &2"
                                        + (((double) levelCompletion.getCompletionTimeElapsed()) / 1000) + "s");
                            player.sendMessage(levelCompletions);
                        }
                    }
                }
            }
        } else {
            sender.sendMessage(Utils.translate("&2/stats <level> &7 View a level's stats"));
        }
        return true;
    }
}