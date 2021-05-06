package com.parkourcraft.parkour.data.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class EventParticipant {

    private Player participant;
    private Location originalLocation;
    private String originalLevelName;

    public EventParticipant(Player participant, String originalLevelName) {
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

    public String getOriginalLevel() {
        return originalLevelName;
    }
}
