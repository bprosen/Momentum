package com.renatusnetwork.parkour.data.saves;

import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import org.bukkit.Location;

public class SavesManager
{
    // add save
    public void addSave(PlayerStats playerStats, Location location, Level level)
    {
        playerStats.addSave(level, location);
        SavesDB.addSave(playerStats.getUUID(), level.getName(), location);
    }

    public void loadSave(PlayerStats playerStats, Location location, Level level)
    {
        // teleport and then remove from cache and db
        playerStats.getPlayer().teleport(location);
        removeSave(playerStats, level);
    }

    public void removeSave(PlayerStats playerStats, Level level)
    {
        playerStats.removeSave(level);
        SavesDB.removeSave(playerStats.getUUID(), level.getName());
    }
}
