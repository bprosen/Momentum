package com.renatusnetwork.parkour.data.infinite.types;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Sprint extends Infinite
{
    private int timerMillis;
    private long startingMillis;
    private BukkitTask task;

    public Sprint(PlayerStats playerStats)
    {
        super(playerStats, InfiniteType.SPRINT, 2);
        this.timerMillis = Parkour.getSettingsManager().sprint_starting_timer * 1000;
        this.startingMillis = System.currentTimeMillis();
    }

    // by default respawn will end it
    public void respawn()
    {
        teleportToFirst();
    }

    @Override
    public void start()
    {
        super.start();
        super.next(); // do next jump

        startTask();
    }

    public void end()
    {
        super.end();
    }

    public void next()
    {
        super.next();

        float newGain = Parkour.getSettingsManager().sprint_time_gain;

        // loop through reduction score
        for (Integer reductionScore : Parkour.getSettingsManager().reduction_factors.keySet())
            if (getScore() > reductionScore)
                newGain -= Parkour.getSettingsManager().reduction_factors.get(reductionScore);

        timerMillis += (int) (newGain * 1000);

        int maxMillis = Parkour.getSettingsManager().sprint_max_timer * 1000;
        if (timerMillis > maxMillis)
            timerMillis = maxMillis;

        startTask();
    }

    public void startTask()
    {
        if (task != null)
            task.cancel();

        task = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                Parkour.getInfiniteManager().endPK(getPlayer()); // end when time is up
            }
        }.runTaskLater(Parkour.getPlugin(), 20 * ((startingMillis + timerMillis) - System.currentTimeMillis()) / 1000);
    }

    public double getTimeLeft()
    {
        return Math.round((((startingMillis + timerMillis) - System.currentTimeMillis()) / 1000f) * 10) / 10.0;
    }
}
