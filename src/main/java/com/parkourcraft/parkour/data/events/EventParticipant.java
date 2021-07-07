package com.parkourcraft.parkour.data.events;

import com.parkourcraft.parkour.data.levels.Level;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class EventParticipant {

    private Player participant;
    private Location originalLocation;
    private Level originalLevelName;

    public EventParticipant(Player participant, Level originalLevelName) {
        this.participant = participant;
        this.originalLocation = participant.getLocation();
        this.originalLevelName = originalLevelName;
    }

    public Player getPlayer() {
        return participant;
    }

    public Location getOriginalLocation() {
        return originalLocation;
    }

    public Level getOriginalLevel() {
        return originalLevelName;
    }
}
