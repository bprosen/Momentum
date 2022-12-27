package com.renatusnetwork.parkour.commands;

import com.comphenix.protocol.PacketType;
import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.data.stats.StatsDB;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GrindCMD implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a)
    {

        if (sender instanceof Player)
        {
            Player player = (Player) sender;

            if (a.length == 0)
            {
                PlayerStats playerStats = Parkour.getStatsManager().get(player);

                if (playerStats != null)
                {
                    // Update cache and DB
                    playerStats.toggleGrinding();
                    StatsDB.updatePlayerGrinding(playerStats);

                    player.sendMessage(Utils.translate("&a&lGrind &8» &7You have turned grind mode " + (playerStats.isGrinding() ? "&aOn" : "&cOff")));
                }
            }
        }
        return false;
    }
}
