package com.renatusnetwork.momentum.data.leaderboards;

public class RecordsLBPosition {

    private String playerName;
    private int records;

    public RecordsLBPosition(String playerName, int records) {
        this.playerName = playerName;
        this.records = records;
    }

    public int getRecords() {
        return records;
    }

    public String getName() {
        return playerName;
    }
}
