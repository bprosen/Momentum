package com.renatusnetwork.momentum.data.leaderboards;

public class MasteryLBPosition {
    private String playerName;
    private int masteryCompletions;

    public MasteryLBPosition(String playerName, int masteryCompletions) {
        this.playerName = playerName;
        this.masteryCompletions = masteryCompletions;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getMasteryCompletions() {
        return masteryCompletions;
    }
}
