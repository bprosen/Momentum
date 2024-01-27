package com.renatusnetwork.parkour.data.leaderboards;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.levels.Level;

public class LevelLBPosition
{
    private String levelName;
    private String playerName;
    private long timeTaken;

    public LevelLBPosition(String levelName, String playerName, long timeTaken)
    {
        this.levelName = levelName;
        this.playerName = playerName;
        this.timeTaken = timeTaken;
    }

    public String getLevelName()
    {
        return levelName;
    }

    public String getPlayerName()
    {
        return playerName;
    }

    public long getTimeTaken()
    {
        return timeTaken;
    }

    public double getTimeTakenSeconds()
    {
        return timeTaken / 1000d;
    }
}
