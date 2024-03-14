package com.renatusnetwork.momentum.data.events.types;

import com.renatusnetwork.momentum.data.levels.Level;
import com.renatusnetwork.momentum.utils.dependencies.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public abstract class Event {

    private Level level;
    private ProtectedRegion region;
    private String formattedName;

    public Event(Level level, String formattedName)
    {
        this.formattedName = formattedName;
        this.level = level;
        this.region = WorldGuard.getRegion(level.getStartLocation());
    }

    public abstract void end();

    public String getFormattedName()
    {
        return formattedName;
    }
    public Level getLevel() {
        return level;
    }

    public ProtectedRegion getRegion() { return region; }
}

