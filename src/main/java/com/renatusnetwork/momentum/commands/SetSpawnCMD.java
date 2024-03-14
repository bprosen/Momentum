package com.renatusnetwork.momentum.commands;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.utils.Utils;
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
        if (player.hasPermission("momentum.admin"))
        {
            Momentum.getLocationManager().set("spawn", player.getLocation());
            Momentum.getLocationManager().reloadCachedLocations();
            player.sendMessage(Utils.translate("&7You have set spawn to your location"));
        }

        return false;
    }
}
