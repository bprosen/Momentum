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

                if (playerStats != null && playerStats.isLoaded())
                {
                    // Update cache and DB
                    playerStats.toggleGrinding();
                    StatsDB.updatePlayerGrinding(playerStats.getUUID(), playerStats.isGrinding());

                    player.sendMessage(Utils.translate("&7You have turned grind mode " + (playerStats.isGrinding() ? "&aOn" : "&cOff")));
                }
                else
                    player.sendMessage(Utils.translate("&cYou cannot do this while loading your stats"));
            }
        }
        return false;
    }
}
