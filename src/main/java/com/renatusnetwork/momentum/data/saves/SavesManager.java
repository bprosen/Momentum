package com.renatusnetwork.momentum.data.saves;

import com.renatusnetwork.momentum.data.levels.Level;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import org.bukkit.Location;

public class SavesManager
{
    // add save
    public void addSave(PlayerStats playerStats, Location location, Level level)
    {
        playerStats.addSave(level.getName(), location);
        SavesDB.addSave(playerStats, level.getName(), location);
    }

    public void loadSave(PlayerStats playerStats, Location location, Level level)
    {
        // teleport and then remove from cache and db
        playerStats.getPlayer().teleport(location);
        removeSave(playerStats, level);
    }

    public void removeSave(PlayerStats playerStats, Level level)
    {
        playerStats.removeSave(level.getName());
        SavesDB.removeSave(playerStats, level.getName());
    }
}
