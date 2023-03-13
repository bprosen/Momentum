package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.checkpoints.CheckpointDB;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.data.stats.StatsManager;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

public class SpawnCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        PlayerStats playerStats = Parkour.getStatsManager().get(player);

        if (player.hasPermission("rn-parkour.admin"))
        {

            if (a.length == 0)
                checkTutorial(playerStats);
            else if (a.length == 1)
            {

                String victim = a[0];
                Player victimPlayer = Bukkit.getPlayer(victim);

                if (victimPlayer == null) {
                    player.sendMessage(Utils.translate("&4" + victim + " &cis not online"));
                    return true;
                }

                PlayerStats victimStats = Parkour.getStatsManager().get(victimPlayer);

                checkTutorial(victimStats);
                player.sendMessage(Utils.translate("&cYou teleported &4" + victim + " &cto spawn"));
            }
        } else if (a.length == 0) {
            checkTutorial(playerStats);
        }
        return false;
    }

    private static void checkTutorial(PlayerStats playerStats)
    {
        // check for tutorial
        if (!playerStats.isInTutorial())
            teleportToSpawn(playerStats);
        else
            playerStats.getPlayer().sendMessage(Utils.translate("&cYou cannot do this while in the tutorial, use &a/tutorial skip &cif you wish to skip"));
    }

    public static void teleportToSpawn(PlayerStats playerStats) {
        Location loc = Parkour.getLocationManager().getLobbyLocation();
        Player player = playerStats.getPlayer();

        if (loc != null) {

            if (!playerStats.isEventParticipant()) {
                if (!playerStats.inRace()) {
                    if (playerStats.getPlayerToSpectate() == null) {
                        // toggle off elytra armor
                        Parkour.getStatsManager().toggleOffElytra(playerStats);

                        player.teleport(loc);

                        playerStats.resetCurrentCheckpoint();
                        playerStats.resetPracticeMode();
                        playerStats.resetLevel();

                        playerStats.clearPotionEffects();

                    } else {
                        player.sendMessage(Utils.translate("&cYou cannot do this while spectating someone"));
                    }
                } else {
                    player.sendMessage(Utils.translate("&cYou cannot do this while in a race"));
                }
            } else {
                player.sendMessage(Utils.translate("&cYou cannot do this while in an event"));
            }
        } else {
            Parkour.getPluginLogger().info("Unable to teleport " + player.getName() + " to spawn, null location?");
        }
    }
}
