package com.renatusnetwork.parkour.data.stats;

public class CoinsLBPosition
{
    private String playerName;
    private double coins;

    public CoinsLBPosition(String playerName, double coins)
    {
        this.playerName = playerName;
        this.coins = coins;
    }

    public double getCoins() { return coins; }

    public String getName() { return playerName; }
}
