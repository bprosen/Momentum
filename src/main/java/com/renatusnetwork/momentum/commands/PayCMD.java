package com.renatusnetwork.momentum.commands;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.data.stats.StatsManager;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class PayCMD implements CommandExecutor
{

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a)
    {
        if (!(sender instanceof ConsoleCommandSender))
        {
            Player user = (Player) sender;

            if (a.length == 2)
            {
                String playerName = a[0];
                Player target = Bukkit.getPlayer(playerName);

                // make sure the target is online
                if (target != null)
                {
                    if (!user.getName().equalsIgnoreCase(target.getName()))
                    {
                        if (Utils.isInteger(a[1]))
                        {
                            int coins = Integer.parseInt(a[1]);

                            // if they dont pay 10 coins (no spam)
                            if (coins >= Momentum.getSettingsManager().minimum_pay_amount)
                            {
                                StatsManager statsManager = Momentum.getStatsManager();
                                PlayerStats userStats = statsManager.get(user);

                                // if they have the coins to pay up
                                if (coins <= userStats.getCoins())
                                {
                                    PlayerStats targetStats = statsManager.get(target);

                                    // add and remove coins
                                    statsManager.addCoins(targetStats, coins);
                                    statsManager.removeCoins(userStats, coins);

                                    // send messages
                                    user.sendMessage(Utils.translate("&7You have paid &c" + target.getDisplayName() + " &6" + Utils.formatNumber(coins) + " &eCoins"));
                                    target.sendMessage(Utils.translate("&c" + user.getDisplayName() + " &7has paid you &6" + Utils.formatNumber(coins) + " &eCoins"));
                                }
                                else
                                {
                                    user.sendMessage(Utils.translate("&cYou do not have &6" + Utils.formatNumber(coins) + " &eCoins"));
                                }
                            }
                            else
                            {
                                user.sendMessage(Utils.translate("&cYou cannot pay less than &6" + Utils.formatNumber(Momentum.getSettingsManager().minimum_pay_amount) + " &eCoins"));
                            }
                        }
                        else
                        {
                            user.sendMessage(Utils.translate("&c" + a[1] + " is not a number"));
                        }
                    }
                    else
                    {
                        user.sendMessage(Utils.translate("&cYou cannot pay yourself"));
                    }
                }
                else
                {
                    user.sendMessage(Utils.translate("&c" + playerName + " is not online"));
                }
            }
            else
            {
                user.sendMessage(Utils.translate("&cInvalid usage, do &4/pay (player) (coins)"));
            }
        }
        return false;
    }
}
