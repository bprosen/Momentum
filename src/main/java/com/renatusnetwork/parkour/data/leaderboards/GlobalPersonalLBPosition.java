package com.renatusnetwork.parkour.data.leaderboards;

public class GlobalPersonalLBPosition
{
    private String playerName;
    private int completions;

    public GlobalPersonalLBPosition(String playerName, int completions)
    {
        this.playerName = playerName;
        this.completions = completions;
    }

    public double getCompletions() { return completions; }

    public String getName() { return playerName; }
}
