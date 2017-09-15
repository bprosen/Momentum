package com.parkourcraft.Parkour.stats.objects;


public class LevelCompletion {

    private long timeOfCompletion;
    private long completionTimeElapsed; // time elapsed
    private boolean inDatabase;

    public LevelCompletion(long timeOfCompletion, long completionTimeElapsed, boolean inDatabase) {
        this.timeOfCompletion = timeOfCompletion;
        this.completionTimeElapsed = completionTimeElapsed;
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

}
