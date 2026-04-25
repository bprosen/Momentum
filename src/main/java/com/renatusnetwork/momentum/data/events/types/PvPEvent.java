package com.renatusnetwork.momentum.data.events.types;

import com.renatusnetwork.momentum.data.levels.Level;

public class PvPEvent extends Event {

    public PvPEvent(Level level) {
        super(level, "PvP");
    }

    @Override
    public void end() {
        // empty method!
    }
}
