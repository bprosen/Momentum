package com.renatusnetwork.parkour.data.infinite.types;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Sprint extends Infinite
{
    private float timer;
    private BukkitTask task;

    public Sprint(PlayerStats playerStats)
    {
        super(playerStats, 2);
        this.timer = Parkour.getSettingsManager().sprint_starting_timer;
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

        if (task != null)
            task.cancel();
    }

    public void next()
    {
        super.next();

        float newGain = Parkour.getSettingsManager().sprint_time_gain;

        // loop through reduction score
        for (Integer reductionScore : Parkour.getSettingsManager().reduction_factors.keySet())
            if (getScore() > reductionScore)
                newGain -= Parkour.getSettingsManager().reduction_factors.get(reductionScore);

        timer += newGain;

        int max = Parkour.getSettingsManager().sprint_max_timer;
        if (timer > max)
            timer = max;
    }

    public void startTask()
    {

        task = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                timer -= 0.1f; // remove 1 second

                // end
                if (timer <= 0.0)
                {
                    cancel();
                    // force sync
                    new BukkitRunnable()
                    {
                        @Override
                        public void run()
                        {
                            Parkour.getInfiniteManager().endPK(getPlayer()); // end when time is up
                        }
                    }.runTask(Parkour.getPlugin());
                }
            }
        }.runTaskTimerAsynchronously(Parkour.getPlugin(), 2, 2);
    }

    public double getTimeLeft()
    {
        return Math.round(timer * 10) / 10.0;
    }
}
