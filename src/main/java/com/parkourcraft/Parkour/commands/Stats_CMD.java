package com.parkourcraft.Parkour.commands;

import com.parkourcraft.Parkour.Parkour;
import com.parkourcraft.Parkour.data.levels.LevelObject;
import com.parkourcraft.Parkour.data.stats.LevelCompletion;
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

            LevelObject levelObject = Parkour.levels.get(levelName);

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
                                + ChatColor.GREEN + levelCompletion.getPlayerName()
                        );
                    }
                else
                    sender.sendMessage(ChatColor.RED + "No timed completions to display");

                int totalCompletionsCount = levelObject.getTotalCompletionsCount();
                String outOfMessage = ChatColor.GRAY + "Out of "
                        + ChatColor.GREEN + totalCompletionsCount;

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
                PlayerStats playerStats = Parkour.stats.get(player);

                if (playerStats != null) {
                    Map<String, List<LevelCompletion>> levelCompletionsMap = playerStats.getLevelCompletionsMap();

                    for (Map.Entry<String, List<LevelCompletion>> levelCompletionsEntry : levelCompletionsMap.entrySet()) {
                        List<LevelCompletion> levelCompletionsList = levelCompletionsEntry.getValue();
                        LevelObject level = Parkour.levels.get(levelCompletionsEntry.getKey());

                        if (level != null) {
                            String levelCompletions = level.getFormattedTitle() + ChatColor.GRAY + " :" + ChatColor.GREEN;

                            int untimed = 0;

                            for (LevelCompletion levelCompletion : levelCompletionsList)
                                if (levelCompletion.getCompletionTimeElapsed() == 0L)
                                    untimed++;
                                else
                                    levelCompletions = levelCompletions + " "
                                        + (((double) levelCompletion.getCompletionTimeElapsed()) / 1000) + "s";

                            player.sendMessage(levelCompletions);
                        }
                    }
                }
            }

            sender.sendMessage(ChatColor.GREEN + "/stats <level>" + ChatColor.GRAY + " View a level's stats");
        }

        return true;
    }

}
