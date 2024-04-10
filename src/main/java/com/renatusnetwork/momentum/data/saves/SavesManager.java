package com.renatusnetwork.momentum.data.saves;

import com.renatusnetwork.momentum.data.levels.Level;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SavesManager
{
    // add save
    public void addSave(PlayerStats playerStats, Level level, Location location)
    {
        playerStats.addSave(level, location);
        SavesDB.addSave(playerStats.getUUID(), level.getName(), location);
    }

    public void removeSave(PlayerStats playerStats, Level level)
    {
        playerStats.removeSave(level);
        SavesDB.removeSave(playerStats.getUUID(), level.getName());
    }

    public void teleportAndRemoveSave(PlayerStats playerStats, Level level, Location location)
    {
        playerStats.teleport(location);
        removeSave(playerStats, level);
    }

    public void updateSave(PlayerStats playerStats, Level level, Location location)
    {
        playerStats.updateSave(level, location);
        SavesDB.updateSave(playerStats.getUUID(), level.getName(), location);
    }

    public void saveLevel(PlayerStats playerStats, Level level, Location location)
    {
        // update here
        if (playerStats.hasSave(level))
            updateSave(playerStats, level, location);
        else
            // add here
            addSave(playerStats, level, location);
    }

    public void autoSave(PlayerStats playerStats)
    {
        if (
                playerStats.isAttemptingMastery() ||
                playerStats.isInTutorial() ||
                playerStats.isEventParticipant() ||
                playerStats.isPreviewingLevel() ||
                playerStats.inRace() ||
                playerStats.isInInfinite() ||
                !playerStats.inLevel() ||
                playerStats.getLevel().isAscendance() ||
                playerStats.isSpectating() ||
                !playerStats.hasAutoSave() ||
                isNearSpawnOrCheckpoint(playerStats)
        )
            return;

        Location location = playerStats.inPracticeMode() ? playerStats.getPracticeStart() : playerStats.getPlayer().getLocation();
        Level level = playerStats.getLevel();

        saveLevel(playerStats, level, location);
        playerStats.sendMessage(Utils.translate("&7Your progress on &c" + level.getTitle() + "&7 has been automatically saved"));
    }

    private boolean isNearSpawnOrCheckpoint(PlayerStats playerStats)
    {
        // no point in saving if they are right beside spawn or checkpoint
        Player player = playerStats.getPlayer();

        return (playerStats.inLevel() &&
                (
                    (playerStats.hasCurrentCheckpoint() && playerStats.getCurrentCheckpoint().distance(player.getLocation()) <= 1.0) ||
                    playerStats.getLevel().getStartLocation().distance(player.getLocation()) <= 1.0
                ));
    }
}