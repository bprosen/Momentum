package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.data.stats.StatsManager;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FailsCMD implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a)
    {
        if (sender instanceof Player)
        {
            Player player = (Player) sender;
            StatsManager statsManager = Parkour.getStatsManager();
            PlayerStats playerStats = statsManager.get(player);

            if (a.length == 0)
            {
                if (playerStats.inLevel())
                    if (playerStats.inFailMode())
                        player.sendMessage(Utils.translate(
                                "&7You have &c" + Utils.formatNumber(playerStats.getFails()) +
                                     " &cFails &7on &a" + playerStats.getLevel().getFormattedTitle())
                        );
                    else
                        player.sendMessage(Utils.translate("&7You have &4Fails &cOff&7. Type &c/fails toggle &7to see your fails on a level"));
                else
                    player.sendMessage(Utils.translate("&cYou are not in a level"));
            }
            else if (a.length == 1 && a[0].equalsIgnoreCase("toggle"))
            {
                // update in cache
                playerStats.setFailMode(!playerStats.inFailMode());
                playerStats.resetFails();

                String booleanStr = "&aOn";

                if (!playerStats.inFailMode())
                    booleanStr = "&cOff";

                player.sendMessage(Utils.translate("&7You have toggled &cFails &7" + booleanStr));

                int failBit = 0;
                if (playerStats.inFailMode())
                    failBit = 1;

                // update in db
                Parkour.getDatabaseManager().runAsyncQuery("UPDATE players SET fail_mode=" + failBit + " WHERE uuid='" + player.getUniqueId() + "'");
            }
            else if (a.length == 1 && a[0].equalsIgnoreCase("help"))
            {
                sendHelp(player);
            }
            else if (a.length == 1)
            {
                String targetName = a[0];
                Player target = Bukkit.getPlayer(targetName);

                // continue if online
                if (target != null)
                {
                    PlayerStats targetStats = statsManager.get(target);

                    if (targetStats.inLevel())
                        if (targetStats.inFailMode())
                            player.sendMessage(Utils.translate(
                                    "&c" + targetName + " &7has &c" + Utils.formatNumber(targetStats.getFails()) +
                                            " &cFails &7on &a" + targetStats.getLevel().getFormattedTitle())
                            );
                        else
                            player.sendMessage(Utils.translate("&c" + targetName + " has &4Fails &cOff&7"));
                    else
                        player.sendMessage(Utils.translate("&c" + targetName + " is not in a level"));

                }
                else
                {
                    player.sendMessage(Utils.translate("&4" + targetName + " &cis not online"));
                }
            }
            else
            {
                sendHelp(player);
            }
        }
        return false;
    }

    private void sendHelp(Player player)
    {
        player.sendMessage(Utils.translate("&4&lFails Help"));
        player.sendMessage(Utils.translate("&c/fails  &7Tells you your fails"));
        player.sendMessage(Utils.translate("&c/fails (name)  &7Tells you another person's fails"));
        player.sendMessage(Utils.translate("&c/fails toggle  &7Toggle fail mode"));
        player.sendMessage(Utils.translate("&c/fails help  &7Displays this screen"));
    }
}
