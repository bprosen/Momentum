package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.stats.LevelCompletion;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.List;

public class RecordsCMD implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a)
    {
        if (sender instanceof Player)
        {
            if (a.length == 0)
            {
                Player player = (Player) sender;
                PlayerStats playerStats = Parkour.getStatsManager().get(player);
                int currentFound = 0;

                player.sendMessage(Utils.translate("&9&lYour Records"));

                // iterate through all levels
                for (Level level : Parkour.getLevelManager().getLevels().values())
                {
                    // stop when we have all the records we wanted
                    if (playerStats.getRecords() > currentFound)
                    {
                        List<LevelCompletion> leaderboard = level.getLeaderboard();

                        // if not empty, keep going
                        if (!leaderboard.isEmpty())
                        {
                            // print to player and increment
                            long time = leaderboard.get(0).getCompletionTimeElapsed();

                            player.sendMessage(Utils.translate("&a" + level.getFormattedTitle() + " &7" + time + "s"));
                            currentFound++;
                        }
                    }
                    else
                        break;
                }
            }
        }
        else
        {
            sender.sendMessage("Console cannot do this");
        }
        return false;
    }
}
