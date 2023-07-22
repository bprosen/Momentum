package com.renatusnetwork.parkour.data.events.types;

import com.renatusnetwork.parkour.data.levels.Level;
import org.bukkit.scheduler.BukkitTask;

public class AscentEvent extends Event
{

    public AscentEvent(Level level)
    {
        super(level, "Ascent");
    }

    @Override
    public void end()
    {
        // empty method
    }
}
