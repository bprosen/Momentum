package com.renatusnetwork.momentum.data.levels;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
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
        return level.getStartLocation().distance(current) > Momentum.getSettingsManager().preview_max_distance;
    }

    public void teleport()
    {
        playerStats.getPlayer().teleport(level.getStartLocation());
    }

    public void reset()
    {
        playerStats.getPlayer().teleport(oldLocation);
    }
}
