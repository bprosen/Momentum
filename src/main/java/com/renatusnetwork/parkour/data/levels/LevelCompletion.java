package com.renatusnetwork.parkour.data.levels;

public class LevelCompletion
{
    private String levelName;
    private long timeOfCompletion;
    private long completionTimeElapsed; // time elapsed
    private String name;
    private String uuid;

    public LevelCompletion(String levelName, String uuid, String name, long timeOfCompletion, long completionTimeElapsed)
    {
        this.levelName = levelName;
        this.uuid = uuid;
        this.name = name;
        this.timeOfCompletion = timeOfCompletion;

        if (completionTimeElapsed < 72000000L)
            this.completionTimeElapsed = completionTimeElapsed;
        else
            this.completionTimeElapsed = 0L;
    }

    public String getLevelName() { return levelName; }

    public boolean wasTimed() { return completionTimeElapsed > 0; }

    public long getTimeOfCompletionMillis() {
        return timeOfCompletion;
    }

    public double getTimeOfCompletionSeconds() { return timeOfCompletion / 1000d; }

    public long getCompletionTimeElapsedMillis() {
        return completionTimeElapsed;
    }

    public double getCompletionTimeElapsedSeconds() { return completionTimeElapsed / 1000d; }

    public String getName() {
        return name;
    }

    public String getUUID() { return uuid; }
}