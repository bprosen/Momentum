package com.renatusnetwork.parkour.data.events.types;

import org.bukkit.scheduler.BukkitTask;

public interface SchedulerInterface
{
    void runScheduler();

    BukkitTask getScheduler();

    void cancel();
}
