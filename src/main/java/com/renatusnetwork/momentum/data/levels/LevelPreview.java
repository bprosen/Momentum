package com.renatusnetwork.momentum.data.levels;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.Location;

public class LevelPreview
{
    private PlayerStats playerStats;
    private Level level;
    private Location oldLocation;

    public LevelPreview(PlayerStats playerStats, Level level, Location oldLocation)
    {
        this.playerStats = playerStats;
        this.level = level;
        this.oldLocation = oldLocation;
    }

    public Level getLevel()
    {
        return level;
    }

    public boolean shouldTeleport(Location current)
    {
        float distance = Momentum.getSettingsManager().preview_max_distance;

        return !Utils.isNearby(level.getStartLocation(), current, distance);
    }

    public void teleport()
    {
        playerStats.teleport(level.getStartLocation(), false);
    }

    public void reset()
    {
        playerStats.teleport(oldLocation, true);
    }
}
