package com.renatusnetwork.parkour.data.events;

public class EventLBPosition
{
    private String playerUUID;
    private String playerName;
    private int wins;

    public EventLBPosition(String playerUUID, String playerName, int wins)
    {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.wins = wins;
    }

    public int getWins() { return wins; }

    public void setWins(int wins) { this.wins = wins; }

    public String getUUID() { return playerUUID; }

    public String getName() { return playerName; }
}
