package com.parkourcraft.Parkour.data.stats;

public class LevelCompletion {

    private long timeOfCompletion;
    private long completionTimeElapsed; // time elapsed
    private int playerID = -1;
    private String playerName;

    public LevelCompletion(long timeOfCompletion, long completionTimeElapsed) {
        this.timeOfCompletion = timeOfCompletion;
        if (completionTimeElapsed < 72000000L)
            this.completionTimeElapsed = completionTimeElapsed;
        else
            this.completionTimeElapsed = 0L;
    }

    public long getTimeOfCompletion() {
        return timeOfCompletion;
    }

    public long getCompletionTimeElapsed() {
        return completionTimeElapsed;
    }

    public void setPlayerID(int ID) {
        playerID = ID;
    }

    public int getPlayerID() {
        return playerID;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return playerName;
    }

}
