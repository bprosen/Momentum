package com.renatusnetwork.parkour.data.infinite.types;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import org.bukkit.Material;

public class Speedrun extends Infinite
{
    public Speedrun(PlayerStats playerStats)
    {
        super(playerStats, InfiniteType.SPEEDRUN, 3);
    }

    @Override
    public void start()
    {
        super.start();
        super.next(); // do next jump
        next(); // two jumps ahead
    }

    public void next()
    {
        super.next();
        // need to manually clear it
        getLastBlock().getLocation().add(0, 1, 0).getBlock().setType(Material.AIR);
    }
}
