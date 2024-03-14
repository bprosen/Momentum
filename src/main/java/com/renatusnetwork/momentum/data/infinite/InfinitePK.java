package com.renatusnetwork.momentum.data.infinite;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class InfinitePK {

    private Player player;
    private Location originalLoc;
    private Location currentBlockLoc = null;
    private Location lastBlockLoc = null;
    private int score = 0;
    private InfinitePKDirection directionType;

    public InfinitePK(Player player) {
        this.player = player;
        this.originalLoc = player.getLocation();
        this.directionType = InfinitePKDirection.FORWARDS;
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

    public void setOriginalLoc(Location originalLoc) { this.originalLoc = originalLoc; }

    public Location getLastBlockLoc() { return lastBlockLoc; }

    public Location getCurrentBlockLoc() {
        return currentBlockLoc;
    }

    public Location getPressutePlateLoc() { return currentBlockLoc.clone().add(0, 1, 0); }

    public InfinitePKDirection getDirectionType() { return this.directionType; }

    public void setDirectionType(InfinitePKDirection directionType) {
        this.directionType = directionType;
    }

    public void flipDirectionType() {
        if (directionType == InfinitePKDirection.BACKWARDS)
            directionType = InfinitePKDirection.FORWARDS;
        else
            directionType = InfinitePKDirection.BACKWARDS;
    }
}
