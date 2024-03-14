package com.renatusnetwork.momentum.data.clans;

public class ClanMember {

    private int playerID;
    private String UUID;
    private String playerName;

    public ClanMember(int playerID, String UUID, String playerName) {
        this.playerID = playerID;
        this.UUID = UUID;
        this.playerName = playerName;
    }

    public int getPlayerID() {
        return playerID;
    }

    public String getUUID() {
        return UUID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) { this.playerName = playerName; }
}
