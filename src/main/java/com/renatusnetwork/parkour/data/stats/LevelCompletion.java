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
        this.timeOfCompletion = timeOfCompletion;

        if (completionTimeElapsed < 72000000L)
            this.completionTimeElapsed = completionTimeElapsed;
        else
            this.completionTimeElapsed = 0L;
    }

    public String getLevelName() { return levelName; }

    public long getTimeOfCompletionMillis() {
        return timeOfCompletion;
    }

    public long getTimeOfCompletionSeconds() { return timeOfCompletion / 1000; }

    public long getCompletionTimeElapsed() {
        return completionTimeElapsed;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getUUID() { return uuid; }
}