package com.renatusnetwork.momentum.data.infinite.gamemode;

import com.renatusnetwork.momentum.data.stats.PlayerStats;

public class Classic extends Infinite
{

    public Classic(PlayerStats playerStats)
    {
        super(playerStats, 2);
    }

    @Override
    public void start()
    {
        super.start();
        super.next(); // do next jump
    }
}
