package com.parkourcraft.parkour.data.playersubmitted;

import org.bukkit.Location;
import org.bukkit.entity.Player;


public class Plot {

    private String ownerName;
    private String ownerUUID;
    private Location spawnLoc;

    // add via player object
    public Plot(Player owner, Location spawnLoc) {
        this.ownerName = owner.getName();
        this.ownerUUID = owner.getUniqueId().toString();
        this.spawnLoc = spawnLoc;
    }

    // no player object addition
    public Plot(String ownerName, String ownerUUID, Location spawnLoc) {
        this.ownerName = ownerName;
        this.ownerUUID = ownerUUID;
        this.spawnLoc = spawnLoc;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getOwnerUUID() {
        return ownerUUID;
    }

    public Location getSpawnLoc() {
        return spawnLoc;
    }
}
