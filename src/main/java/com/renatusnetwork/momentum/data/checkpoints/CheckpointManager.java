package com.renatusnetwork.momentum.data.checkpoints;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.levels.Level;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.Location;

public class CheckpointManager {

    public void deleteCheckpoint(String playerName, Level level)
    {
        PlayerStats playerStats = Momentum.getStatsManager().getByName(playerName);

        if (playerStats != null)
            deleteCheckpointData(playerStats, level);

        CheckpointDB.deleteCheckpointFromName(playerName, level.getName());
    }

    public void deleteCheckpointData(PlayerStats playerStats, Level level)
    {
        // reset cache only
        if (playerStats.hasCurrentCheckpoint() && playerStats.inLevel() && playerStats.getLevel().equals(level))
            playerStats.resetCurrentCheckpoint();

        playerStats.removeCheckpoint(level);

    }

    public void deleteCheckpoint(PlayerStats playerStats, Level level)
    {
        deleteCheckpointData(playerStats, level);
        CheckpointDB.deleteCheckpointFromName(playerStats.getName(), level.getName());
    }

    public void teleportToPracCP(PlayerStats playerStats)
    {
        if (!playerStats.isEventParticipant())
        {
            if (!playerStats.isSpectating())
            {
                if (!playerStats.isPreviewingLevel())
                {
                    if (!playerStats.isInInfinite())
                    {
                        if (!playerStats.isInBlackMarket())
                        {
                            if (playerStats.inPracticeMode())
                                playerStats.getPlayer().teleport(playerStats.getPracticeLocation());
                            else
                                playerStats.sendMessage(Utils.translate("&cNo location loaded to teleport you to"));
                        }
                        else
                            playerStats.sendMessage(Utils.translate("&cYou cannot do this while in black market"));
                    }
                    else
                        playerStats.sendMessage(Utils.translate("&cYou cannot do this while in infinite"));
                }
                else
                    playerStats.sendMessage(Utils.translate("&cYou cannot do this while previewing a level"));
            }
            else
                playerStats.sendMessage(Utils.translate("&cYou cannot do this while spectating"));
        }
        else
            playerStats.sendMessage(Utils.translate("&cYou cannot do this while in an event"));
    }

    public void teleportToCP(PlayerStats playerStats)
    {
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
                    if (!playerStats.isPreviewingLevel())
                    {
                        if (playerStats.inPracticeMode())
                            loc = playerStats.getPracticeLocation().clone();
                        else if (playerStats.hasCurrentCheckpoint())
                        {
                            loc = playerStats.getCurrentCheckpoint().clone().add(0.5, 0, 0.5);
                            setDirection = true;
                        }
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
                    } else if (playerStats.getLevel() != null && Momentum.getLocationManager().exists(playerStats.getLevel().getName() + "-spawn")) {
                        loc = playerStats.getLevel().getStartLocation();

                        if (loc.getWorld().getName().equalsIgnoreCase(playerStats.getPlayer().getWorld().getName()) &&
                                playerStats.getPlayer().getLocation().distance(loc) > 1.0)
                            playerStats.addFail();

                        playerStats.getPlayer().teleport(loc);
                    } else
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
    }
}
