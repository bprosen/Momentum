package com.renatusnetwork.parkour.data.events.types;

import com.renatusnetwork.parkour.data.levels.Level;

public class PvPEvent extends Event
{

    public PvPEvent(Level level)
    {
        super(level, "PvP");
    }

    @Override
    public void end()
    {
        // empty method!
    }
}
