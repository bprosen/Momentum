package com.parkourcraft.Parkour.commands;

import com.parkourcraft.Parkour.data.LevelManager;
import com.parkourcraft.Parkour.data.StatsManager;
import com.parkourcraft.Parkour.data.levels.LevelObject;
import com.parkourcraft.Parkour.data.stats.LevelCompletion;
import com.parkourcraft.Parkour.data.stats.LevelStats;
import com.parkourcraft.Parkour.data.stats.PlayerStats;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class Stats_CMD implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (a.length > 0) {
            String levelName = a[0];

            LevelObject levelObject = LevelManager.get(levelName);

            if (levelObject != null) {
                sender.sendMessage(
                        levelObject.getFormattedTitle() + ChatColor.RESET
                        + ChatColor.GRAY + " Leaderboard"
                );

                List<LevelCompletion> completions = levelObject.getLeaderboard();

                if (completions.size() > 0)
                    for (int i = 0; i <= completions.size() - 1; i++) {
                        LevelCompletion levelCompletion = completions.get(i);
                        int rank = i + 1;

                        sender.sendMessage(
                                "  " + ChatColor.GRAY
                                + rank + " " + ChatColor.DARK_GREEN
                                + (((double) levelCompletion.getCompletionTimeElapsed()) / 1000) + "s "
                                + ChatColor.GREEN + StatsManager.getNameFromCache(levelCompletion.getPlayerID())
                        );
                    }
                else
                    sender.sendMessage(ChatColor.RED + "No timed completions to display");

                int totalCompletionsCount = levelObject.getTotalCompletionsCount();
                String outOfMessage = ChatColor.GRAY + "Out of "
                        + ChatColor.GREEN + totalCompletionsCount
                        + ChatColor.GRAY + " Completion";
                if (totalCompletionsCount > 1)
                    outOfMessage += "s";

                sender.sendMessage(outOfMessage);
            } else
                sender.sendMessage(
                        ChatColor.GRAY + "No level named '" +
                                ChatColor.RED + levelName +
                                ChatColor.GRAY + "' exists"
                );
        } else {
            if (sender instanceof Player) {
                Player player = ((Player) sender).getPlayer();
                PlayerStats playerStats = StatsManager.get(player);

                if (playerStats != null) {
                    Map<String, LevelStats> levelStatsMap = playerStats.getLevelStatsMap();

                    for (Map.Entry<String, LevelStats> levelStatsEntry : levelStatsMap.entrySet()) {
                        Map<Long, LevelCompletion> levelCompletionMap = levelStatsEntry.getValue().getLevelCompletionsMap();

                        String levelCompletions = levelStatsEntry.getKey() + ":";

                        for (Map.Entry<Long, LevelCompletion> levelCompletionsEntry : levelCompletionMap.entrySet()) {
                            LevelCompletion completion = levelCompletionsEntry.getValue();

                            levelCompletions = levelCompletions + " "
                                    + (((double) completion.getCompletionTimeElapsed()) / 1000) + "s";
                        }

                        player.sendMessage(levelCompletions);
                    }
                }
            }

            sender.sendMessage(ChatColor.GREEN + "/stats <level>" + ChatColor.GRAY + " View a level's stats");
        }

        return true;
    }

}
