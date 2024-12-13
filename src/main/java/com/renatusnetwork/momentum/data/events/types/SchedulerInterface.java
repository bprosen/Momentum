package com.renatusnetwork.momentum.data.events.types;

import org.bukkit.scheduler.BukkitTask;

public interface SchedulerInterface {
    void runScheduler();

    BukkitTask getScheduler();

    void cancel();
}
