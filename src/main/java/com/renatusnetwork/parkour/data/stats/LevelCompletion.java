package com.renatusnetwork.parkour.data.stats;

public class LevelCompletion
{
    private String levelName;
    private long timeOfCompletion;
    private long completionTimeElapsed; // time elapsed
    private String playerName;
    private String uuid;

    public LevelCompletion(String levelName, String uuid, String playerName, long timeOfCompletion, long completionTimeElapsed)
    {
        this.levelName = levelName;
        this.uuid = uuid;
        this.playerName = playerName;

        if (completionTimeElapsed < 72000000L)
            this.completionTimeElapsed = completionTimeElapsed;
        else
            this.completionTimeElapsed = 0L;
    }

    public String getLevelName() { return levelName; }

    public long getTimeOfCompletion() {
        return timeOfCompletion;
    }

    public long getCompletionTimeElapsed() {
        return completionTimeElapsed;
    }

    public long getCompletionTimeElapsedSeconds() { return (completionTimeElapsed / 1000); }

    public String getPlayerName() {
        return playerName;
    }

    public String getUUID() { return uuid; }
}