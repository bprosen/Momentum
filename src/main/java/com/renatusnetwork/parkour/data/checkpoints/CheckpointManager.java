package com.renatusnetwork.parkour.data.checkpoints;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Location;

public class CheckpointManager {

    public void teleportPlayer(PlayerStats playerStats) {

        if (!playerStats.inRace()) {
            if (!playerStats.isEventParticipant()) {
                if (playerStats.getPlayerToSpectate() == null) {

                    Location loc = null;
                    boolean setDirection = false;
                    /*
                     check if there is a practice location,
                     if not then check if the loc is checkpoint and
                     adjust the x and z to teleport to middle
                     */
                    if (playerStats.getPracticeLocation() != null)
                        loc = playerStats.getPracticeLocation().clone();
                    else if (playerStats.getCheckpoint() != null) {
                        loc = playerStats.getCheckpoint().clone().add(0.5, 0, 0.5);
                        setDirection = true;
                    }

                    if (loc != null) {
                        if (setDirection) {
                            loc.setPitch(playerStats.getPlayer().getLocation().getPitch());
                            loc.setYaw(playerStats.getPlayer().getLocation().getYaw());
                        }
                        playerStats.getPlayer().teleport(loc);
                    // if the level has a stored start loc (not spawn), tp them to it
                    } else if (playerStats.getLevel() != null && Parkour.getLocationManager().exists(playerStats.getLevel().getName() + "-spawn"))
                            playerStats.getPlayer().teleport(playerStats.getLevel().getStartLocation());
                    else
                        playerStats.getPlayer().sendMessage(Utils.translate("&cNo location loaded to teleport you to"));
                } else {
                    playerStats.getPlayer().sendMessage(Utils.translate("&cYou cannot do this while spectating"));
                }
            } else {
                playerStats.getPlayer().sendMessage(Utils.translate("&cYou cannot do this while in an event"));
            }
        } else {
            playerStats.getPlayer().sendMessage(Utils.translate("&cYou cannot do this while in a race"));
        }
    }
}
