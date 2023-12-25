package com.renatusnetwork.parkour.data.checkpoints;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.storage.mysql.DatabaseQueries;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Location;

public class CheckpointManager {

    public void deleteCheckpoint(PlayerStats playerStats, Level level)
    {
        if (playerStats.hasCurrentCheckpoint())
        {
            // remove all checkpoint data
            playerStats.removeCheckpoint(level.getName());
            playerStats.resetCurrentCheckpoint();

            DatabaseQueries.runAsyncQuery("DELETE FROM checkpoints WHERE level_name='" + level.getName() + "'" +
                    " AND player_name='" + playerStats.getPlayerName() + "'");
        }
    }

    public void teleportToPracCP(PlayerStats playerStats) {
        if (!playerStats.inRace()) {
            if (!playerStats.isEventParticipant()) {
                if (!playerStats.isSpectating()) {
                    if (playerStats.inPracticeMode())
                        playerStats.getPlayer().teleport(playerStats.getPracticeLocation());
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

    public void teleportToCP(PlayerStats playerStats) {

        if (!playerStats.inRace()) {
            if (!playerStats.isEventParticipant()) {
                if (!playerStats.isInInfinite()) {
                    if (!playerStats.isSpectating()) {

                        Location loc = null;
                        boolean setDirection = false;
                        /*
                         check if there is a practice location,
                         if not then check if the loc is checkpoint and
                         adjust the x and z to teleport to middle
                         */
                        if (playerStats.inPracticeMode())
                            loc = playerStats.getPracticeLocation().clone();
                        else if (playerStats.hasCurrentCheckpoint()) {
                            loc = playerStats.getCurrentCheckpoint().clone().add(0.5, 0, 0.5);
                            setDirection = true;
                        }

                        if (loc != null) {
                            if (setDirection) {
                                loc.setPitch(playerStats.getPlayer().getLocation().getPitch());
                                loc.setYaw(playerStats.getPlayer().getLocation().getYaw());
                            }

                            if (loc.getWorld().getName().equalsIgnoreCase(playerStats.getPlayer().getWorld().getName()) &&
                                playerStats.getPlayer().getLocation().distance(loc) > 1.0 && playerStats.getPracticeLocation() == null) // only add a fail when no practice
                                playerStats.addFail();

                            playerStats.getPlayer().teleport(loc);

                            // if the level has a stored start loc (not spawn), tp them to it
                        }
                        else if (playerStats.getLevel() != null && Parkour.getLocationManager().exists(playerStats.getLevel().getName() + "-spawn"))
                        {
                            loc = playerStats.getLevel().getStartLocation();

                            if (loc.getWorld().getName().equalsIgnoreCase(playerStats.getPlayer().getWorld().getName()) &&
                                playerStats.getPlayer().getLocation().distance(loc) > 1.0)
                                playerStats.addFail();

                            playerStats.getPlayer().teleport(loc);
                        }
                        else
                            playerStats.getPlayer().sendMessage(Utils.translate("&cNo location loaded to teleport you to"));
                    } else {
                        playerStats.getPlayer().sendMessage(Utils.translate("&cYou cannot do this while spectating"));
                    }
                } else {
                    playerStats.getPlayer().sendMessage(Utils.translate("&cYou cannot do this while in Infinite Parkour"));
                }
            } else {
                playerStats.getPlayer().sendMessage(Utils.translate("&cYou cannot do this while in an event"));
            }
        } else {
            playerStats.getPlayer().sendMessage(Utils.translate("&cYou cannot do this while in a race"));
        }
    }
}
