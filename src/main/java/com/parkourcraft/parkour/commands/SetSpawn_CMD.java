package com.parkourcraft.parkour.commands;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetSpawn_CMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        if (player.hasPermission("pc-parkour.admin")) {
            if (a.length == 0) {

                Location loc = player.getLocation();
                String world = loc.getWorld().getName();
                double x = loc.getX();
                double y = loc.getY();
                double z = loc.getZ();
                float yaw = loc.getYaw();
                float pitch = loc.getPitch();

                Parkour.getConfigManager().get("settings")
                        .set("spawn.location", world + ":" + x + ":" + y + ":" + z + ":" + yaw + ":" + pitch);
                Utils.loadSpawn();
                player.sendMessage(Utils.translate("&cYou set the global spawnpoint"));
            }
        }
        return false;
    }
}
