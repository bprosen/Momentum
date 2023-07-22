package com.renatusnetwork.parkour.data.events.types;

import com.renatusnetwork.parkour.data.levels.Level;

public class MazeEvent extends Event
{

    public MazeEvent(Level level)
    {
        super(level, "Maze");
    }

    @Override
    public void end()
    {
        // empty method
    }
}
