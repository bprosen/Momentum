package com.parkourcraft.parkour.data.infinite;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class InfinitePK {

    private Player player;
    private Location currentBlockLoc;
    private int score = 0;

    public InfinitePK(Player player) {
        this.player = player;
        this.currentBlockLoc = player.getLocation();
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

    public void updateCurrentBlockLoc(Location loc) {
        this.currentBlockLoc = loc;
    }

    public Location getCurrentBlockLoc() {
        return currentBlockLoc;
    }
}
