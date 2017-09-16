package com.parkourcraft.Parkour.stats.objects;


public class LevelCompletion {

    private long timeOfCompletion;
    private long completionTimeElapsed; // time elapsed
    private boolean inDatabase;

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

}
