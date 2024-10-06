package com.renatusnetwork.momentum.data.leaderboards;

public class ELOLBPosition {

    private String playerName;
    private int elo;
    private int position;

    public ELOLBPosition(String playerName, int elo, int position) {
        this.playerName = playerName;
        this.elo = elo;
        this.position = position;
    }

    public String getName() {
        return playerName;
    }

    public int getELO() {
        return elo;
    }

    public int getPosition() {
        return position;
    }
}
