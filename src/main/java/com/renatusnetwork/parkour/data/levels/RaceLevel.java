package com.renatusnetwork.parkour.data.levels;

import org.bukkit.Location;

public class RaceLevel extends Level
{
    private Location spawnLocation1;
    private Location spawnLocation2;

    public RaceLevel(String levelName, long completionSeconds)
    {
        super(levelName, completionSeconds);
    }

    public Location getSpawnLocation1()
    {
        return spawnLocation1;
    }

    public Location getSpawnLocation2()
    {
        return spawnLocation2;
    }
    public void setSpawnLocation1(Location location)
    {
        this.spawnLocation1 = location;
    }

    public void setSpawnLocation2(Location location)
    {
        this.spawnLocation2 = location;
    }
}
