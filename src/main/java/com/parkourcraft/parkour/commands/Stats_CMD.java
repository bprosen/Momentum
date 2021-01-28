package com.parkourcraft.parkour.commands;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.levels.LevelObject;
import com.parkourcraft.parkour.data.stats.LevelCompletion;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.utils.Utils;
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

            LevelObject levelObject = Parkour.getLevelManager().get(levelName);

            if (levelObject != null) {
                if (Parkour.getLevelManager().getEnabledLeaderboards().contains(levelName)) {
                    sender.sendMessage(Utils.translate(levelObject.getFormattedTitle() + " &7Leaderboard"));
                    List<LevelCompletion> completions = levelObject.getLeaderboard();

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

                    int totalCompletionsCount = levelObject.getTotalCompletionsCount();
                    String outOfMessage = Utils.translate("&7Out of &2" + totalCompletionsCount);

                    sender.sendMessage(outOfMessage);
                } else {
                    sender.sendMessage(Utils.translate("&7This level does not have an enabled leaderboard"));
                }
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
                    LevelObject level = Parkour.getLevelManager().get(levelCompletionsEntry.getKey());

                    if (level != null) {
                        String levelCompletions = level.getFormattedTitle() + Utils.translate("&& :&2");

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
        } else {
            sender.sendMessage(Utils.translate("&2/stats <level> &7 View a level's stats"));
        }
        return true;
    }
}