package com.renatusnetwork.momentum.data.leaderboards;

public class CoinsLBPosition
{
    private String playerName;
    private int coins;

    public CoinsLBPosition(String playerName, int coins)
    {
        this.playerName = playerName;
        this.coins = coins;
    }

    public int getCoins() { return coins; }

    public String getName() { return playerName; }
}
