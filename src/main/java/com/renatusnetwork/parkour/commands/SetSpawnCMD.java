package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.locations.LocationManager;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetSpawnCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        if (player.hasPermission("rn-parkour.admin"))
        {
            Parkour.getLocationManager().set("spawn", player.getLocation());
            Parkour.getLocationManager().reloadCachedLocations();
            player.sendMessage(Utils.translate("&7You have set spawn to your location"));
        }

        return false;
    }
}
