package com.renatusnetwork.parkour.data.stats;

public class LevelCompletion {

    private long timeOfCompletion;
    private long completionTimeElapsed; // time elapsed
    private String playerName;
    private String uuid;

    public LevelCompletion(String uuid, String playerName, long timeOfCompletion, long completionTimeElapsed)
    {
        this.uuid = uuid;
        this.playerName = playerName;

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

    public String getPlayerName() {
        return playerName;
    }

    public String getUUID() { return uuid; }
}