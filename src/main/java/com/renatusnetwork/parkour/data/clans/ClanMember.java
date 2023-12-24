package com.renatusnetwork.parkour.data.clans;

public class ClanMember
{
    private String uuid;
    private String playerName;

    public ClanMember(String uuid, String playerName)
    {
        this.uuid = uuid;
        this.playerName = playerName;
    }

    public String getUUID() {
        return uuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) { this.playerName = playerName; }
}
