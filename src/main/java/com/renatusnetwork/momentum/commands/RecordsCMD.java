package com.renatusnetwork.momentum.commands;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.levels.Level;
import com.renatusnetwork.momentum.data.stats.LevelCompletion;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.data.stats.StatsDB;
import com.renatusnetwork.momentum.utils.Utils;
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

            if (a.length == 1 && a[0].equalsIgnoreCase("top"))
            {
                StatsCMD.printRecordsLB(sender);
            }
            else if (a.length == 1 && a[0].equalsIgnoreCase("help"))
            {
                sendHelp(player);
            }
            else if (a.length >= 0 && a.length <= 2)
            {
                if (a.length == 0)
                {
                    sendStats(player, player.getName(), false, 1, false);
                }
                else
                {
                    String arg = a[0];
                    String targetName = player.getName();
                    boolean pageOnSelf = false;

                    // if length 2, by default arg = page number and a[0] = target
                    if (a.length == 2)
                    {
                        arg = a[1];
                        targetName = a[0];
                    }

                    int page = 1;
                    // if arg is an int, cast it to the page!
                    if (Utils.isInteger(arg))
                    {
                        page = Integer.parseInt(arg);

                        // dont allow negative pages
                        if (page < 1)
                            page = 1;

                        // if the args length is 1, they did the command on self or if the page number is less than 100
                        if (a.length == 1)
                        {
                            if (page >= 100)
                                targetName = a[0];
                            else
                                pageOnSelf = true;
                        }
                    }
                    // if not integer and length 1, target name is a[0]
                    else if (a.length == 1)
                        targetName = a[0];

                    Player target = Bukkit.getPlayer(targetName);

                    if (target == null)
                    {
                        // if not online, we run the async offline records check, inner classes
                        int finalPage = page;
                        boolean finalPageOnSelf = pageOnSelf;
                        String finalTargetName = targetName;

                        new BukkitRunnable()
                        {
                            @Override
                            public void run()
                            {
                                sendStats(player, finalTargetName, true, finalPage, finalPageOnSelf);
                            }
                        }.runTaskAsynchronously(Momentum.getPlugin());
                    } else
                        sendStats(player, target.getName(), false, page, pageOnSelf); // do normal getter for online
                }
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

    private void sendStats(Player sender, String targetName, boolean offline, int page, boolean pageOnSelf)
    {
        // make sure we are not loading lbs
        if (!Momentum.getStatsManager().isLoadingLeaderboards())
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
                PlayerStats targetStats = Momentum.getStatsManager().getByName(targetName);

                if (targetStats != null)
                    records = targetStats.getRecords();
            }

            // make sure they exist first
            if (exists || pageOnSelf)
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
                    int max = page * 10;

                    String[] messageStr = new String[max];

                    // iterate through all levels
                    for (Level level : Momentum.getLevelManager().getLevels().values())
                    {
                        // stop when we have all the records we wanted
                        if (records > currentFound && max > currentFound)
                        {
                            List<LevelCompletion> leaderboard = level.getLeaderboard();

                            // if not empty, keep going
                            if (!leaderboard.isEmpty() && leaderboard.get(0).getPlayerName().equalsIgnoreCase(targetName))
                            {
                                // print to player and increment
                                long time = leaderboard.get(0).getCompletionTimeElapsed();

                                messageStr[currentFound] = Utils.translate("&7" + (currentFound + 1) + " &a" + level.getFormattedTitle() + " &7" + (((double) time) / 1000) + "s");
                                currentFound++;
                            }
                        }
                        else
                            break;
                    }

                    int i = max - 10;

                    if (messageStr[i] == null)
                        sender.sendMessage(Utils.translate("&7No page exists"));
                    else
                    // send page
                    for (; i < max; i++)
                        if (messageStr[i] != null)
                            sender.sendMessage(messageStr[i]);

                    // send next page option
                    if (records > max)
                        sender.sendMessage(Utils.translate("&9/records " + targetName + " " + (page + 1)));

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
