package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.blackmarket.BlackMarketManager;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BlackMarketCMD implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a)
    {
        BlackMarketManager blackMarketManager = Parkour.getBlackMarketManager();

        if (sender instanceof Player && !sender.hasPermission("rn-parkour.admin"))
            sender.sendMessage(Utils.translate("&cYou do not have permission to do this"));
        //
        // /blackmarket load
        // /blackmarket start
        // /blackmarket end
        // /blackmarket add (player)
        // /blackmarket help
        //
        else if (a.length == 1 && a[0].equalsIgnoreCase("load"))
        {
            Parkour.getConfigManager().load("blackmarket");
            sender.sendMessage(Utils.translate("&7Loaded &8blackmarket.yml &7from disk"));
            blackMarketManager.load();
        }
        else if (a.length == 1 && a[0].equalsIgnoreCase("start"))
        {
            if (!blackMarketManager.isRunning())
                Parkour.getBlackMarketManager().start();
            else
                sender.sendMessage(Utils.translate("&cThe Black Market is currently running. Do &4/blackmarket end &cto stop"));
        }
        else if (a.length == 1 && a[0].equalsIgnoreCase("end"))
        {
            if (blackMarketManager.isRunning())
            {
                Parkour.getBlackMarketManager().forceEnd();
                sender.sendMessage(Utils.translate("&7You successfully force ended the &8&lBlack Market"));
            }
            else
                sender.sendMessage(Utils.translate("&cThe Black Market is not currently running. Do &4/blackmarket start &cto start"));
        }
        else if (a.length == 2 && a[0].equalsIgnoreCase("add"))
        {
            String playerName = a[1];
            Player player = Bukkit.getPlayer(playerName);

            if (player != null)
            {
                PlayerStats playerStats = Parkour.getStatsManager().get(player);

                if (!playerStats.isSpectating())
                {
                    // add player
                    sender.sendMessage(Utils.translate("&7Added &c" + player.getName() + " &7to the &8&lBlack Market"));
                    blackMarketManager.playerJoined(Parkour.getStatsManager().get(player));
                }
                else
                {
                    sender.sendMessage(Utils.translate("&cCannot add spectating player: &4" + playerName));
                }
            }
            else
            {
                sender.sendMessage(Utils.translate("&c" + playerName + " is not online"));
            }
        }
        else
        {
            sender.sendMessage(Utils.translate("&8&lBlackMarket Help"));
            sender.sendMessage(Utils.translate(" &8/blackmarket load  &7Loads Black Market data from config"));
            sender.sendMessage(Utils.translate(" &8/blackmarket start  &7Starts the Black Market if it is not already running"));
            sender.sendMessage(Utils.translate(" &8/blackmarket end  &7Stops the Black Market if it is already running"));
            sender.sendMessage(Utils.translate(" &8/blackmarket add (playerName)  &7Adds the player to the event if it is running"));
            sender.sendMessage(Utils.translate(" &8/blackmarket help  &7Displays this page"));
        }
        return false;
    }
}
