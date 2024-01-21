package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.levels.CompletionsDB;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.levels.LevelCompletion;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.data.stats.StatsDB;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class RecordsCMD implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a)
    {
        if (sender instanceof Player)
        {
            Player player = (Player) sender;

            if (a.length == 1 && a[0].equalsIgnoreCase("top"))
                StatsCMD.printRecordsLB(sender);
            else if (a.length == 1 && a[0].equalsIgnoreCase("help"))
                sendHelp(player);
            else if (a.length >= 0 && a.length <= 2)
            {
                if (a.length == 0)
                    sendStats(player, player.getName(), 1);
                else
                {
                    String arg = a[0];
                    String targetName = player.getName();

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
                        String finalTargetName = targetName;

                        new BukkitRunnable()
                        {
                            @Override
                            public void run()
                            {
                                sendStats(player, finalTargetName, finalPage);
                            }
                        }.runTaskAsynchronously(Parkour.getPlugin());
                    } else
                        sendStats(player, target.getName(), page); // do normal getter for online
                }
            }
            else
                sendHelp(player);
        }
        else
            sender.sendMessage("Console cannot do this");

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

    private void sendStats(Player sender, String targetName, int page)
    {
        // make sure we are not loading lbs
        if (!Parkour.getStatsManager().isLoadingLeaderboards())
        {
            if (StatsDB.isPlayerInDatabase(targetName))
            {
                PlayerStats targetStats = Parkour.getStatsManager().getByName(targetName);

                HashMap<Level, Double> records = new HashMap<>();

                // online then offline lookup process
                if (targetStats != null)
                {
                    HashSet<LevelCompletion> completions = targetStats.getRecords();

                    for (LevelCompletion completion : completions)
                        records.put(Parkour.getLevelManager().get(completion.getLevelName()), completion.getCompletionTimeElapsedSeconds());
                }
                else
                {
                    HashMap<Level, Long> offlineCompletions = CompletionsDB.getRecordsFromName(targetName);

                    for (Map.Entry<Level, Long> offlineCompletion : offlineCompletions.entrySet())
                        records.put(offlineCompletion.getKey(), (offlineCompletion.getValue() / 1000d));
                }

                // if they are equal, we print out "Your records"
                if (sender.getName().equalsIgnoreCase(targetName))
                    sender.sendMessage(Utils.translate("&9&lYour Records"));
                else // otherwise, print out their name
                    sender.sendMessage(Utils.translate("&9&l" + targetName + "'s Records"));

                // only continue if they have records!
                if (!records.isEmpty())
                {
                    int currentNum = 0;
                    int numRecords = records.size();
                    int max = page * 10;

                    if ((max - 10) >= records.size())
                        sender.sendMessage(Utils.translate("&7No page exists"));
                    else
                        // iterate through all records
                        for (Map.Entry<Level, Double> record : records.entrySet())
                        {
                            if (numRecords > currentNum && max > currentNum)
                            {
                                sender.sendMessage(Utils.translate(
                                        "&7" + (currentNum + 1) + " &a" + record.getKey().getTitle() + "&7 " + record.getValue() + "s"
                                ));
                                currentNum++;
                            }
                            else break;
                        }

                    // send next page option
                    if (records.size() > max)
                        sender.sendMessage(Utils.translate("&9/records " + targetName + " " + (page + 1)));

                    sender.sendMessage(Utils.translate("&e✦ " + records.size() + " &7Records"));
                }
                else
                    sender.sendMessage(Utils.translate("&7None"));
            }
            else
                sender.sendMessage(Utils.translate("&4" + targetName + " &chas not joined the server"));
        }
        else
            sender.sendMessage(Utils.translate("&cStill loading records..."));
    }
}
