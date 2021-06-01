package com.parkourcraft.parkour.data.infinite;

public class InfinitePKLBPosition {

    private String playerUUID;
    private String playerName;
    private int score;
    private int position;

    public InfinitePKLBPosition(String playerUUID, String playerName, int score, int position) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.score = score;
        this.position = position;
    }

    public int getScore() { return score; }

    public void setScore(int score) { this.score = score; }

    public String getUUID() { return playerUUID; }

    public String getName() { return playerName; }

    public int getPosition() { return position; }
}
