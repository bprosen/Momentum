package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.stats.LevelCompletion;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
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
            Player player = (Player) sender;

            if (a.length == 0)
            {
                player.sendMessage(Utils.translate("&9&lYour Records"));
                sendStats(player, player);
            }
            else if (a.length == 1 && a[0].equalsIgnoreCase("top"))
            {
                StatsCMD.printRecordsLB(sender);
            }
            else if (a.length == 1 && a[0].equalsIgnoreCase("help"))
            {
                sendHelp(player);
            }
            else if (a.length == 1)
            {
                String targetName = a[0];
                Player target = Bukkit.getPlayer(targetName);

                if (target != null)
                {
                    player.sendMessage(Utils.translate("&9&l" + targetName + "'s Records"));
                    sendStats(player, target);
                }
                else
                    player.sendMessage(Utils.translate("&4" + targetName + " &cis not online!"));
            }
            else
            {
                sendHelp(player);
            }
        }
        else
        {
            sender.sendMessage("Console cannot do this");
        }
        return false;
    }

    private void sendHelp(Player player)
    {
        player.sendMessage(Utils.translate("&9Records Help"));
        player.sendMessage(Utils.translate("&a/records  &7View your own records"));
        player.sendMessage(Utils.translate("&a/records (player) &7View their records"));
        player.sendMessage(Utils.translate("&a/records top  &7Prints the records leaderboard"));
        player.sendMessage(Utils.translate("&a/records help  &7Prints this screen"));
    }

    private void sendStats(Player sender, Player target)
    {
        if (!Parkour.getStatsManager().isLoadingLeaderboards())
        {
            PlayerStats playerStats = Parkour.getStatsManager().get(target);
            int records = playerStats.getRecords();

            // only continue if they have records!
            if (records > 0)
            {
                int currentFound = 0;

                // iterate through all levels
                for (Level level : Parkour.getLevelManager().getLevels().values())
                {
                    // stop when we have all the records we wanted
                    if (records > currentFound)
                    {
                        List<LevelCompletion> leaderboard = level.getLeaderboard();

                        // if not empty, keep going
                        if (!leaderboard.isEmpty() && leaderboard.get(0).getPlayerName().equalsIgnoreCase(target.getName()))
                        {
                            // print to player and increment
                            long time = leaderboard.get(0).getCompletionTimeElapsed();

                            sender.sendMessage(Utils.translate("&a" + level.getFormattedTitle() + " &7" + (((double) time) / 1000) + "s"));
                            currentFound++;
                        }
                    }
                    else
                        break;
                }
                sender.sendMessage(Utils.translate("&e✦ " + records + " &7Records"));
            }
            else
            {
                sender.sendMessage(Utils.translate("&7None"));
            }
        }
        else
        {
            sender.sendMessage(Utils.translate("&cStill loading leaderboards..."));
        }
    }
}
