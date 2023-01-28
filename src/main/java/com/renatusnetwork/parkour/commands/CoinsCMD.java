package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.data.stats.StatsDB;
import com.renatusnetwork.parkour.data.stats.StatsManager;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CoinsCMD implements CommandExecutor
{

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a)
    {
        /*
            /coins - shows coins
            /coins set (playerName) (coins) - sets coins
            /coins add (playerName) (coins) - adds coins
            /coins remove (playerName) (coins) - removes coins
            /coins top - displays lb
         */
        StatsManager statsManager = Parkour.getStatsManager();

        // COINS
        if ((a.length == 0 && (a[0].equalsIgnoreCase("coinstop") || a[0].equalsIgnoreCase("cointop"))) &&
             a.length == 1 && a[0].equalsIgnoreCase("top"))
        {
            StatsCMD.printCoinsLB(sender);
        }
        // coins set
        else if (a.length == 3 && (
                a[0].equalsIgnoreCase("set") ||
                a[0].equalsIgnoreCase("add") ||
                a[0].equalsIgnoreCase("remove")))
        {
            if (sender.hasPermission("rn-parkour.admin"))
            {
                // variable
                String targetName = a[1];
                double coins = Double.parseDouble(a[2]);
                Player target = Bukkit.getPlayer(targetName);

                if (a[0].equalsIgnoreCase("set"))
                {
                    // if null, update in db
                    if (target == null)
                        StatsDB.updateCoinsName(targetName, coins);
                    else
                    // otherwise cache and db
                    {
                        PlayerStats targetStats = statsManager.get(target);
                        statsManager.updateCoins(targetStats, coins);
                    }

                    // msg
                    sender.sendMessage(Utils.translate("&7You have set &6" + targetName + " &e&lCoins &7to &6" + Utils.formatNumber(coins)));
                }
                else if (a[0].equalsIgnoreCase("add"))
                {
                    double total = coins;

                    // if null, update in db
                    if (target == null)
                    {
                        total = StatsDB.getCoinsFromName(targetName) + coins;
                        StatsDB.updateCoinsName(targetName, total);
                    }
                    else
                    // otherwise cache and db
                    {
                        PlayerStats targetStats = statsManager.get(target);
                        statsManager.addCoins(targetStats, coins);
                    }

                    // msg
                    sender.sendMessage(Utils.translate(
                            "&7You have added &6" + Utils.formatNumber(coins) + " &e&lCoins &e(" + Utils.formatNumber(total) + ") &7to &6" + targetName
                    ));
                } else if (a[0].equalsIgnoreCase("remove"))
                {
                    double total = coins;

                    // if null, update in db
                    if (target == null)
                    {
                        total = StatsDB.getCoinsFromName(targetName) - coins;
                        StatsDB.updateCoinsName(targetName, total);
                    } else
                    // otherwise cache and db
                    {
                        PlayerStats targetStats = statsManager.get(target);
                        statsManager.removeCoins(targetStats, coins);
                    }

                    // msg
                    sender.sendMessage(Utils.translate(
                            "&7You have removed &6" + Utils.formatNumber(coins) + " &e&lCoins &e(" + Utils.formatNumber(total) + ") &7to &6" + targetName
                    ));
                }
            }
            else
            {
                sender.sendMessage(Utils.translate("&cYou do not have permission"));
            }
        }
        else if (a.length == 0)
        {
            // only players
            if (sender instanceof Player)
            {
                Player player = (Player) sender;
                player.sendMessage(Utils.translate("&7You have &6" + Utils.formatNumber(statsManager.get(player).getCoins()) + " &e&lCoins"));
            }
        }
        else
        {
            sendHelp(sender);
        }
        return false;
    }

    private void sendHelp(CommandSender sender)
    {
        sender.sendMessage(Utils.translate("&e/coins  &7Displays your coins"));
        sender.sendMessage(Utils.translate("&e/coins top  &7Displays the top 10 players with the most coins"));

        if (sender.hasPermission("rn-parkour.admin"))
        {
            sender.sendMessage(Utils.translate("&e/coins set <IGN> <coins>  &7Sets coins"));
            sender.sendMessage(Utils.translate("&e/coins add <IGN> <coins>  &7Add coins"));
            sender.sendMessage(Utils.translate("&e/coins remove <IGN> <coins>  &7Remove coins"));
        }

        sender.sendMessage(Utils.translate("&e/coins help  &7Shows this display"));
    }
}
