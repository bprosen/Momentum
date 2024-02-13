package com.renatusnetwork.parkour.data.leaderboards;

import com.renatusnetwork.parkour.Parkour;

public class ELOLBPosition
{
    private String playerName;
    private int elo;

    public ELOLBPosition(String playerName, int elo)
    {
        this.playerName = playerName;
        this.elo = elo;
    }

    public String getName()
    {
        return playerName;
    }

    public int getELO()
    {
        return elo;
    }
}
