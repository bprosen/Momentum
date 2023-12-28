package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.menus.MenuItemAction;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JoinCMD implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a)
    {
        if (sender instanceof Player)
        {

            Player player = (Player) sender;
            if (a.length == 1)
            {
                Player target = Bukkit.getPlayer(a[0]);

                if (target != null)
                {
                    PlayerStats playerStats = Parkour.getStatsManager().get(player);
                    PlayerStats targetStats = Parkour.getStatsManager().get(target);

                    if (targetStats.inLevel())
                        Parkour.getLevelManager().teleportToLevel(playerStats, targetStats.getLevel());
                    else
                        player.sendMessage(Utils.translate("&4" + targetStats.getName() + " &cis not in a level"));
                }
                else
                {
                    player.sendMessage(Utils.translate("&4" + a[0] + " &cis not online"));
                }
            }
            else
            {
                player.sendMessage(Utils.translate("&cInvalid usage, do &4/join (playerName)"));
            }
        }
        return false;
    }
}
