package com.renatusnetwork.momentum.data.checkpoints;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.SettingsManager;
import com.renatusnetwork.momentum.data.levels.Level;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.Location;

public class CheckpointManager {

    public void deleteCheckpoint(String playerName, Level level) {
        PlayerStats playerStats = Momentum.getStatsManager().getByName(playerName);

        if (playerStats != null) {
            deleteCheckpointData(playerStats, level);
        }

        CheckpointDB.deleteCheckpointFromName(playerName, level.getName());
    }

    public void deleteCheckpointData(PlayerStats playerStats, Level level) {
        // reset cache only
        if (playerStats.hasCurrentCheckpoint() && playerStats.inLevel() && playerStats.getLevel().equals(level)) {
            playerStats.resetCurrentCheckpoint();
        }

        playerStats.removeCheckpoint(level);

    }

    public void deleteCheckpoint(PlayerStats playerStats, Level level) {
        deleteCheckpointData(playerStats, level);
        CheckpointDB.deleteCheckpointFromName(playerStats.getName(), level.getName());
    }

    public void teleportToPracticeCheckpoint(PlayerStats playerStats) {
        if (playerStats.isEventParticipant()) {
            playerStats.sendMessage(Utils.translate("&cYou cannot do this while in an event"));
            return;
        }

        if (playerStats.isSpectating()) {
            playerStats.sendMessage(Utils.translate("&cYou cannot do this while spectating"));
            return;
        }

        if (playerStats.isPreviewingLevel()) {
            playerStats.sendMessage(Utils.translate("&cYou cannot do this while previewing a level"));
            return;
        }

        if (playerStats.isInInfinite()) {
            playerStats.sendMessage(Utils.translate("&cYou cannot do this while in infinite"));
            return;
        }

        if (playerStats.isInBlackMarket()) {
            playerStats.sendMessage(Utils.translate("&cYou cannot do this while in black market"));
            return;
        }

        if (!playerStats.inPracticeMode()) {
            playerStats.sendMessage(Utils.translate("&cNo location loaded to teleport you to"));
            return;
        }

        playerStats.teleport(playerStats.getPracticeCheckpoint(), true);
    }

    public void teleportToCheckpoint(PlayerStats playerStats) {
        if (playerStats.isEventParticipant()) {
            playerStats.sendMessage(Utils.translate("&cYou cannot do this while in an event"));
            return;
        }

        if (playerStats.isInInfinite()) {
            playerStats.sendMessage(Utils.translate("&cYou cannot do this while in infinite"));
            return;
        }

        if (playerStats.isSpectating()) {
            playerStats.sendMessage(Utils.translate("&cYou cannot do this while spectating"));
            return;
        }


        Location loc = null;
        boolean setDirection = false;
        /*
         check if there is a practice location,
         if not then check if the loc is checkpoint and
         adjust the x and z to teleport to middle
         */
        if (!playerStats.isPreviewingLevel()) {
            if (playerStats.inPracticeMode()) {
                loc = playerStats.getPracticeCheckpoint().clone();
            } else if (playerStats.hasCurrentCheckpoint()) {
                loc = playerStats.getCurrentCheckpoint().clone().add(0.5, 0, 0.5);
                setDirection = true;
            }
        }

        if (loc != null) {
            if (setDirection) {
                loc.setPitch(playerStats.getPlayer().getLocation().getPitch());
                loc.setYaw(playerStats.getPlayer().getLocation().getYaw());
            }

            if (
                    loc.getWorld().getName().equalsIgnoreCase(playerStats.getPlayer().getWorld().getName()) &&
                    !Utils.isNearby(playerStats.getPlayer().getLocation(), loc, 1.0) && !playerStats.inPracticeMode()
            ) // only add a fail when no practice
            {
                playerStats.addFail();
            }

            playerStats.teleport(loc, true);

            // if the level has a stored start loc (not spawn), tp them to it
        } else if (
                playerStats.inLevel() &&
                Momentum.getLocationManager().exists(SettingsManager.LEVEL_SPAWN_FORMAT.replace("%level%", playerStats.getLevel().getName()))
        ) {
            loc = playerStats.getLevel().getStartLocation();

            if (
                    loc.getWorld().getName().equalsIgnoreCase(playerStats.getPlayer().getWorld().getName()) &&
                    !Utils.isNearby(playerStats.getPlayer().getLocation(), loc, 1.0)
            ) {
                playerStats.addFail();
            }

            playerStats.teleport(loc, true);
        } else {
            playerStats.sendMessage(Utils.translate("&cNo location loaded to teleport you to"));
        }
    }
}
