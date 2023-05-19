package com.renatusnetwork.parkour.data.blackmarket;

import java.util.ArrayList;

public class BlackMarketEvent
{
    private ArrayList<String> players;
    private String highestBidName;
    private long highestBid;
    private BlackMarketItem blackMarketItem;

    public BlackMarketEvent(BlackMarketItem blackMarketItem)
    {
        players = new ArrayList<>();
        this.blackMarketItem = blackMarketItem;
    }

    public BlackMarketItem getBlackMarketItem()
    {
        return blackMarketItem;
    }

    public String getHighestBidName()
    {
        return highestBidName;
    }

    public long getHighestBid()
    {
        return highestBid;
    }

    public void increaseBid(String playerName, long bid)
    {
        this.highestBidName = playerName;
        this.highestBid = bid;
    }

    public void addPlayer(String playerName)
    {
        players.add(playerName);
    }

    public void removePlayer(String playerName)
    {
        players.remove(playerName);
    }

    public boolean inEvent(String playerName)
    {
        return players.contains(playerName);
    }
}
