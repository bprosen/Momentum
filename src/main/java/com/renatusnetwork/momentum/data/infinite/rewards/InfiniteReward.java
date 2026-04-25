package com.renatusnetwork.momentum.data.infinite.rewards;

import com.renatusnetwork.momentum.data.infinite.gamemode.InfiniteType;
import com.renatusnetwork.momentum.data.stats.PlayerStats;

import java.util.List;

public class InfiniteReward {

    private int scoreNeeded;
    private List<String> commands;
    private String display;
    private InfiniteType type;

    public InfiniteReward(InfiniteType type, int scoreNeeded, List<String> commands, String display) {
        this.type = type;
        this.scoreNeeded = scoreNeeded;
        this.commands = commands;
        this.display = display;
    }

    public int getScoreNeeded() {
        return scoreNeeded;
    }

    public List<String> getCommands() {
        return commands;
    }

    public String getDisplay() {
        return display;
    }

    public boolean hasReward(PlayerStats playerStats) {
        return playerStats.getBestInfiniteScore(type) >= scoreNeeded;
    }
}
