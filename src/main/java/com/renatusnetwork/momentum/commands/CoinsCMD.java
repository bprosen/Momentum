package com.renatusnetwork.momentum.commands;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.data.stats.StatsDB;
import com.renatusnetwork.momentum.data.stats.StatsManager;
import com.renatusnetwork.momentum.utils.Utils;
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
        StatsManager statsManager = Momentum.getStatsManager();

        // COINS
        if (a.length == 1 && a[0].equalsIgnoreCase("top"))
            StatsCMD.printCoinsLB(sender);
        else if (a.length == 1 && a[0].equalsIgnoreCase("total"))
            sender.sendMessage(Utils.translate("&7Server total coins are &6" + String.format("%,d", Momentum.getStatsManager().getTotalCoins()) + " &eCoins"));
        // coins set
        else if (a.length == 3 && (
                a[0].equalsIgnoreCase("set") ||
                a[0].equalsIgnoreCase("add") ||
                a[0].equalsIgnoreCase("remove")))
        {
            if (sender.hasPermission("momentum.admin"))
            {
                // variable
                String targetName = a[1];
                double coins = Double.parseDouble(a[2]);

                // NO NEGATIVES
                if (coins < 0)
                    coins = 0;

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
                    sender.sendMessage(Utils.translate("&7You have set &6" + targetName + " &e&lCoins &7to &6" + coins));
                }
                else if (a[0].equalsIgnoreCase("add"))
                {
                    double total;

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

                        total = coins + targetStats.getCoins();
                        statsManager.addCoins(targetStats, coins);
                    }

                    // msg
                    sender.sendMessage(Utils.translate(
                            "&7You have added &6" + coins + " &e&lCoins &e(" + total + ") &7to &6" + targetName
                    ));
                }
                else if (a[0].equalsIgnoreCase("remove"))
                {
                    double total;

                    // if null, update in db
                    if (target == null)
                    {
                        total = StatsDB.getCoinsFromName(targetName) - coins;
                        StatsDB.updateCoinsName(targetName, total);
                    } else
                    // otherwise cache and db
                    {
                        PlayerStats targetStats = statsManager.get(target);

                        total = targetStats.getCoins() - coins;
                        statsManager.removeCoins(targetStats, coins);
                    }

                    // NO NEGATIVES
                    if (total < 0)
                        total = 0;

                    // msg
                    sender.sendMessage(Utils.translate(
                            "&7You have removed &6" + coins + " &e&lCoins &e(" + total + ") &7to &6" + targetName
                    ));
                }
            }
            else
            {
                sender.sendMessage(Utils.translate("&cYou do not have permission"));
            }
        }
        else if (a.length >= 0 && a.length <= 1)
        {
            if (a.length == 0)
            {
                // only players
                if (sender instanceof Player)
                {
                    Player player = (Player) sender;
                    player.sendMessage(Utils.translate("&7You have &6" + Utils.formatNumber(statsManager.get(player).getCoins()) + " &e&lCoins"));
                }
                else
                {
                    sender.sendMessage("Console cannot do this");
                }
            }
            else if (a.length == 1)
            {
                if (!a[0].equalsIgnoreCase("help"))
                {
                    String playerName = a[0];
                    Player target = Bukkit.getPlayer(playerName);

                    int coins = 0;
                    boolean exists = true;

                    // if null, update in db
                    if (target == null)
                    {
                        if (StatsDB.isPlayerInDatabase(playerName))
                            coins = (int) StatsDB.getCoinsFromName(playerName);
                        else
                            exists = false;
                    }
                    else
                        coins = (int) statsManager.get(target).getCoins();

                    if (exists)
                        sender.sendMessage(Utils.translate("&e" + playerName + " &7has &6" + Utils.formatNumber(coins) + " &e&lCoins"));
                    else
                        sender.sendMessage(Utils.translate("&c" + playerName + " &7has not joined the server"));
                }
                else
                    sendHelp(sender);
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
        sender.sendMessage(Utils.translate("&e/coins <IGN>  &7Displays someone else's coins"));
        sender.sendMessage(Utils.translate("&e/coins top  &7Displays the top 10 players with the most coins"));

        if (sender.hasPermission("momentum.admin"))
        {
            sender.sendMessage(Utils.translate("&e/coins set <IGN> <coins>  &7Sets coins"));
            sender.sendMessage(Utils.translate("&e/coins add <IGN> <coins>  &7Add coins"));
            sender.sendMessage(Utils.translate("&e/coins remove <IGN> <coins>  &7Remove coins"));
        }

        sender.sendMessage(Utils.translate("&e/coins help  &7Shows this display"));
    }
}
