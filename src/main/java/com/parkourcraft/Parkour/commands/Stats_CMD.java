package com.parkourcraft.Parkour.commands;


import com.parkourcraft.Parkour.stats.StatsManager;
import com.parkourcraft.Parkour.stats.objects.LevelCompletion;
import com.parkourcraft.Parkour.stats.objects.LevelStats;
import com.parkourcraft.Parkour.stats.objects.PlayerStats;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class Stats_CMD implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (sender instanceof Player) {
            Player player = ((Player) sender).getPlayer();
            PlayerStats playerStats = StatsManager.getPlayerStats(player);

            if (playerStats != null) {
                Map<String, LevelStats> levelStatsMap = playerStats.getLevelStatsMap();

                for (Map.Entry<String, LevelStats> levelStatsEntry : levelStatsMap.entrySet()) {
                    Map<Long, LevelCompletion> levelCompletionMap = levelStatsEntry.getValue().getLevelCompletionsMap();

                    String levelCompletions = levelStatsEntry.getKey() + ":";

                    for (Map.Entry<Long, LevelCompletion> levelCompletionsEntry : levelCompletionMap.entrySet()) {
                        LevelCompletion completion = levelCompletionsEntry.getValue();

                        levelCompletions = levelCompletions + " "
                                + (completion.getCompletionTimeElapsed() / 1000) + "s";
                    }

                    player.sendMessage(levelCompletions);
                }
            }
        }

        return true;
    }

}
