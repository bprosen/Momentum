package com.renatusnetwork.parkour.data.infinite.rewards;

import java.util.List;

public class InfiniteReward {

    private int scoreNeeded;
    private List<String> commands;
    private String display;

    public InfiniteReward(int scoreNeeded, List<String> commands, String display)
    {
        this.scoreNeeded = scoreNeeded;
        this.commands = commands;
        this.display = display;
    }

    public int getScoreNeeded() { return scoreNeeded; }

    public List<String> getCommands() { return commands; }

    public String getDisplay() { return display; }
}
