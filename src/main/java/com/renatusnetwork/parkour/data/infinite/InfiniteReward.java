package com.renatusnetwork.parkour.data.infinite;

public class InfiniteReward {

    private int scoreNeeded;
    private String command;
    private String name;

    public InfiniteReward(int scoreNeeded, String command, String name) {
        this.scoreNeeded = scoreNeeded;
        this.command = command;
        this.name = name;
    }

    public int getScoreNeeded() { return scoreNeeded; }

    public String getCommand() { return command; }

    public String getName() { return name; }
}
