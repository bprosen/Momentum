package com.parkourcraft.parkour.data.spectator;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Spectator {

    private String uuid;
    private String name;
    private Player player;
    private Location lastLocation;

    public Spectator(Player player) {
        this.player = player;
        this.name = player.getName();
        this.uuid = player.getUniqueId().toString();
        this.lastLocation = player.getLocation();
    }

    public Player getPlayer() {
        return player;
    }

    public String getName() {
        return name;
    }

    public String getUUID() {
        return uuid;
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(Location lastLocation) {
        this.lastLocation = lastLocation;
    }
}
