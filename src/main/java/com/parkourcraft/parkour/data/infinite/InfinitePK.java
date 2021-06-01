package com.parkourcraft.parkour.data.infinite;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class InfinitePK {

    private Player player;
    private Location originalLoc;
    private Location currentBlockLoc = null;
    private Location lastBlockLoc = null;
    private int score = 0;

    public InfinitePK(Player player) {
        this.player = player;
        this.originalLoc = player.getLocation();
    }

    public Player getPlayer() {
        return player;
    }

    public String getUUID() {
        return player.getUniqueId().toString();
    }

    public String getName() {
        return player.getName();
    }

    public void addScore() {
        score++;
    }

    public void removeScore() {
        score--;
    }

    public void resetScore() {
        score = 0;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public void updateBlockLoc(Location loc) {
        this.lastBlockLoc = currentBlockLoc;
        this.currentBlockLoc = loc;
    }

    public void setCurrentBlockLoc(Location currentBlockLoc) {
        this.currentBlockLoc = currentBlockLoc;
    }

    public Location getOriginalLoc() { return originalLoc; }

    public Location getLastBlockLoc() { return lastBlockLoc; }

    public Location getCurrentBlockLoc() {
        return currentBlockLoc;
    }

    public Location getPressutePlateLoc() { return currentBlockLoc.clone().add(0, 1, 0); }
}
