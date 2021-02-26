package com.parkourcraft.parkour.commands;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.checkpoints.Checkpoint_DB;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Spawn_CMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        if (player.hasPermission("pc-parkour.admin")) {
            if (a.length == 0) {
                teleportToSpawn(player);
            } else if (a.length == 1) {

                String victim = a[0];
                Player victimPlayer = Bukkit.getPlayer(victim);

                if (victimPlayer == null) {
                    player.sendMessage(Utils.translate("&4" + victim + " &cis not online"));
                    return true;
                }

                teleportToSpawn(victimPlayer);
                player.sendMessage(Utils.translate("&cYou teleported &4" + victim + " &cto spawn"));
            }
        } else if (a.length == 0) {
            teleportToSpawn(player);
        }
        return false;
    }

    private void teleportToSpawn(Player player) {
        Location loc = Parkour.getSettingsManager().spawn_location;

        if (loc != null) {
            player.teleport(loc);
            PlayerStats playerStats = Parkour.getStatsManager().get(player);

            if (playerStats.getCheckpoint() != null) {
                Checkpoint_DB.savePlayerAsync(player);
                playerStats.resetCheckpoint();
            }

            playerStats.resetLevel();
        } else
            Parkour.getPluginLogger().info("Unable to teleport " + player.getName() + " to spawn, null location?");
    }
}
