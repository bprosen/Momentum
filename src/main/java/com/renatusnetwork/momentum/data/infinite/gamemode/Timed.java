package com.renatusnetwork.momentum.data.infinite.gamemode;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Timed extends Infinite {

    private long endMillis;
    private BukkitTask task;

    public Timed(PlayerStats playerStats) {
        super(playerStats, 2);
        this.endMillis = System.currentTimeMillis() + (Momentum.getSettingsManager().timed_timer * 1000L);
    }

    public void respawn() {
        teleportToFirst();
    }

    @Override
    public void start() {
        super.start();
        super.next(); // do next jump
        startTimer(); // start timer
    }

    public void end() {
        super.end();

        if (task != null) {
            task.cancel();
        }
    }

    private void startTimer() {
        task = new BukkitRunnable() {
            @Override
            public void run() {
                Momentum.getInfiniteManager().endPK(getPlayer()); // end when time is up
            }
        }.runTaskLater(Momentum.getPlugin(), 20 * Momentum.getSettingsManager().timed_timer);
    }

    public double getTimeLeft() {
        return Math.round(((endMillis - System.currentTimeMillis()) / 1000f) * 10) / 10.0;
    }
}
