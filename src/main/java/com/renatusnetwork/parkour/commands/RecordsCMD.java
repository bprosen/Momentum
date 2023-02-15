package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.stats.LevelCompletion;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.data.stats.StatsDB;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

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
                sendStats(player, player.getName(), false);
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

                if (target == null)
                {
                    // if not online, we run the async offline records check
                    new BukkitRunnable()
                    {
                        @Override
                        public void run()
                        {
                            sendStats(player, targetName, true);
                        }
                    }.runTaskAsynchronously(Parkour.getPlugin());
                }
                else
                    sendStats(player, target.getName(), false); // do normal getter for online
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
        player.sendMessage(Utils.translate("&3/records  &7View your own records"));
        player.sendMessage(Utils.translate("&3/records (player)  &7View their records"));
        player.sendMessage(Utils.translate("&3/records top  &7Prints the records leaderboard"));
        player.sendMessage(Utils.translate("&3/records help  &7Prints this screen"));
    }

    private void sendStats(Player sender, String targetName, boolean offline)
    {
        // make sure we are not loading lbs
        if (!Parkour.getStatsManager().isLoadingLeaderboards())
        {
            int records = 0;
            boolean exists = true;

            // if offline, we get from database (async)
            if (offline)
            {
                if (StatsDB.isPlayerInDatabase(targetName))
                    records = StatsDB.getRecordsFromName(targetName);
                else
                    exists = false;
            }
            // otherwise we run the normal records player stats
            else
            {
                PlayerStats targetStats = Parkour.getStatsManager().getByName(targetName);

                if (targetStats != null)
                    records = targetStats.getRecords();
            }

            // make sure they exist first
            if (exists)
            {
                // if they are equal, we print out "Your records"
                if (sender.getName().equalsIgnoreCase(targetName))
                    sender.sendMessage(Utils.translate("&9&lYour Records"));
                else // otherwise, print out their name
                    sender.sendMessage(Utils.translate("&9&l" + targetName + "'s Records"));

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
                            if (!leaderboard.isEmpty() && leaderboard.get(0).getPlayerName().equalsIgnoreCase(targetName))
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
                sender.sendMessage(Utils.translate("&c" + targetName + " &7has not joined the server"));
            }
        }
        else
        {
            sender.sendMessage(Utils.translate("&cStill loading records..."));
        }
    }
}
