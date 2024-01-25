package com.renatusnetwork.parkour.data.levels;

public class LevelCompletion
{
    private int id;
    private String levelName;
    private long timeOfCompletion;
    private long completionTimeElapsed; // time elapsed
    private String name;
    private String uuid;

    public LevelCompletion(int id, String levelName, String uuid, String name, long timeOfCompletion, long completionTimeElapsed)
    {
        this.id = id;
        this.levelName = levelName;
        this.uuid = uuid;
        this.name = name;
        this.timeOfCompletion = timeOfCompletion;

        if (completionTimeElapsed < 72000000L)
            this.completionTimeElapsed = completionTimeElapsed;
        else
            this.completionTimeElapsed = 0L;
    }

    public int getID() { return id; }

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

    public boolean equals(LevelCompletion other)
    {
        return other.getID() == this.id;
    }

    public boolean equals(int id)
    {
        return id == this.id;
    }
}