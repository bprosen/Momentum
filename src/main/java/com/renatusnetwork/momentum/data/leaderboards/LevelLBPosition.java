package com.renatusnetwork.momentum.data.leaderboards;

public class LevelLBPosition {

    private String levelName;
    private String playerName;
    private long timeTaken;

    public LevelLBPosition(String levelName, String playerName, long timeTaken) {
        this.levelName = levelName;
        this.playerName = playerName;
        this.timeTaken = timeTaken;
    }

    public String getLevelName() {
        return levelName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public long getTimeTaken() {
        return timeTaken;
    }

    public double getTimeTakenSeconds() {
        return timeTaken / 1000d;
    }
}
