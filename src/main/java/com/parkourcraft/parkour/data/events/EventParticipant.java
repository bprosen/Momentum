package com.parkourcraft.parkour.data.events;

import com.parkourcraft.parkour.data.levels.Level;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class EventParticipant {

    private Player participant;
    private Location originalLocation;
    private Level originalLevel;

    public EventParticipant(Player participant, Level originalLevel) {
        this.participant = participant;
        this.originalLocation = participant.getLocation();
        this.originalLevel = originalLevel;
    }

    public Player getPlayer() {
        return participant;
    }

    public Location getOriginalLocation() {
        return originalLocation;
    }

    public Level getOriginalLevel() {
        return originalLevel;
    }
}
