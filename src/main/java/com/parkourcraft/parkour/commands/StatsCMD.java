package com.parkourcraft.parkour.commands;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.levels.Level;
import com.parkourcraft.parkour.data.stats.LevelCompletion;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class StatsCMD implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (a.length > 0) {
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
                sender.sendMessage(Utils.translate("&&No level named '&c" + levelName + "&7' exists"));
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