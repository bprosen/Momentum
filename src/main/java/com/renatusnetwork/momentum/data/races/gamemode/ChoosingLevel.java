package com.renatusnetwork.momentum.data.races.gamemode;

import com.renatusnetwork.momentum.data.stats.PlayerStats;

public class ChoosingLevel
{
    private PlayerStats sender;
    private PlayerStats requested;
    private int bet;

    public ChoosingLevel(PlayerStats sender, PlayerStats requested, int bet)
    {
        this.sender = sender;
        this.requested = requested;
        this.bet = bet;
    }

    public PlayerStats getSender()
    {
        return sender;
    }

    public PlayerStats getRequested()
    {
        return requested;
    }

    public boolean hasBet()
    {
        return bet > 0;
    }

    public int getBet()
    {
        return bet;
    }
}
