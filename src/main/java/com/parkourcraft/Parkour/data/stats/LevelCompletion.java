package com.parkourcraft.Parkour.data.stats;

public class LevelCompletion {

    private long timeOfCompletion;
    private long completionTimeElapsed; // time elapsed
    private boolean inDatabase;
    private int playerID = -1;

    public LevelCompletion(long timeOfCompletion, long completionTimeElapsed, boolean inDatabase) {
        this.timeOfCompletion = timeOfCompletion;
        if (completionTimeElapsed < 72000000L)
            this.completionTimeElapsed = completionTimeElapsed;
        else
            this.completionTimeElapsed = 0L;
        this.inDatabase = inDatabase;
    }

    public long getTimeOfCompletion() {
        return timeOfCompletion;
    }

    public long getCompletionTimeElapsed() {
        return completionTimeElapsed;
    }

    public boolean inDatabase() {
        return inDatabase;
    }

    public void enteredIntoDatabase() {
        inDatabase = true;
    }

    public void setPlayerID(int ID) {
        playerID = ID;
    }

    public int getPlayerID() {
        return playerID;
    }

}
