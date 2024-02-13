package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ELOCMD implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a)
    {
        /*
            /elo set (player) (elo)
            /elo help
         */
        if (sender instanceof Player)
        {
            Player player = (Player) sender;
            PlayerStats playerStats = Parkour.getStatsManager().get(player);

            if (player.hasPermission("rn-parkour.admin") && a.length == 3 && a[0].equalsIgnoreCase("set"))
            {
                PlayerStats targetStats = Parkour.getStatsManager().get(Bukkit.getPlayer(a[1]));

                if (targetStats != null)
                {
                    if (Utils.isInteger(a[2]))
                    {
                        int elo = Math.max(Integer.parseInt(a[2]), 0);

                        Parkour.getStatsManager().updateELO(targetStats, elo);
                        player.sendMessage(Utils.translate("&7You have set &c" + targetStats.getDisplayName() + "&7's &aELO&7 to &2" + Utils.formatNumber(elo)));
                    }
                    else
                        player.sendMessage(Utils.translate("&4" + a[2] + " is not an integer"));
                }
                else
                    player.sendMessage(Utils.translate("&4" + a[1] + " &cis not online"));
            }
            else if (a.length == 1 && a[0].equalsIgnoreCase("help"))
            {
                sendHelp(player);
            }
            else if (a.length == 1 && a[0].equalsIgnoreCase("top"))
            {
                StatsCMD.printELOLB(sender);
            }
            else if (a.length == 0)
            {
                player.sendMessage(Utils.translate("&7You have &2" + Utils.formatNumber(playerStats.getELO()) + " &aELO"));
            }
            else if (a.length == 1)
            {
                PlayerStats targetStats = Parkour.getStatsManager().get(Bukkit.getPlayer(a[1]));

                if (targetStats != null)
                {
                    player.sendMessage(Utils.translate("&c" + targetStats.getDisplayName() + "&7 has &2" + Utils.formatNumber(targetStats.getELO()) + " &aELO"));
                }
                else
                    player.sendMessage(Utils.translate("&4" + a[1] + " &cis not online"));
            }
            else
                sendHelp(player);
        }
        else
            sender.sendMessage(Utils.translate("&cConsole cannot do this"));

        return false;
    }

    private void sendHelp(Player player)
    {
        player.sendMessage(Utils.translate("&2&lELO Help"));
        player.sendMessage(Utils.translate("&a/elo  &7Shows your ELO"));
        player.sendMessage(Utils.translate("&a/elo (player)  &7Shows their ELO"));
        player.sendMessage(Utils.translate("&a/elo top  &7Shows the ELO leaderboard"));

        if (player.hasPermission("rn-parkour.admin"))
            player.sendMessage(Utils.translate("&a/elo set (player) (elo)  &7Sets the person's ELO"));

        player.sendMessage(Utils.translate("&a/elo help  &7Displays this page"));
    }
}
