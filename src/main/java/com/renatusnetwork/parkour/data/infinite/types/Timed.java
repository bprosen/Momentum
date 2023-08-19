package com.renatusnetwork.parkour.data.infinite.types;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import org.bukkit.scheduler.BukkitRunnable;

public class Timed extends Infinite
{
    private long endMillis;

    public Timed(PlayerStats playerStats)
    {
        super(playerStats, 2);
        this.endMillis = System.currentTimeMillis() + (Parkour.getSettingsManager().timed_timer * 1000L);
    }

    public void respawn()
    {
        teleportToFirst();
    }

    @Override
    public void start()
    {
        super.start();
        super.next(); // do next jump
        startTimer(); // start timer
    }

    public void end()
    {
        super.end();
    }

    private void startTimer()
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                Parkour.getInfiniteManager().endPK(getPlayer()); // end when time is up
            }
        }.runTaskLater(Parkour.getPlugin(), 20 * Parkour.getSettingsManager().timed_timer);
    }

    public double getTimeLeft()
    {
        return Math.round(((endMillis - System.currentTimeMillis()) / 1000f) * 10) / 10.0;
    }
}
