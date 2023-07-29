package com.renatusnetwork.parkour.data.blackmarket;

import com.renatusnetwork.parkour.data.stats.PlayerStats;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class BlackMarketEvent
{
    private ArrayList<PlayerStats> players;
    private LinkedHashMap<PlayerStats, Integer> bids; // descending by bid amount
    private PlayerStats highestBidder;
    private int highestBid;
    private BlackMarketItem blackMarketItem;

    public BlackMarketEvent(BlackMarketItem blackMarketItem)
    {
        bids = new LinkedHashMap<>();
        players = new ArrayList<>();
        this.blackMarketItem = blackMarketItem;
    }

    public BlackMarketItem getBlackMarketItem()
    {
        return blackMarketItem;
    }

    public PlayerStats getHighestBidder()
    {
        return highestBidder;
    }

    public int getHighestBid() { return highestBid; }

    public boolean isHighestBidder(PlayerStats playerStats)
    {
        return highestBidder.equals(playerStats);
    }

    public void highestBidderLeft()
    {
        bids.remove(highestBidder);

        Map.Entry<PlayerStats, Integer> highestEntry = null;

        for (Map.Entry<PlayerStats, Integer> entry : bids.entrySet())
        {
            if (highestEntry == null || highestEntry.getValue() < entry.getValue())
                highestEntry = entry;
        }

        if (highestEntry == null)
        {
            // TODO: force end
        }
        else
        {
            highestBidder = highestEntry.getKey();
            highestBid = highestEntry.getValue();
        }
    }

    public void increaseBid(PlayerStats playerStats, int bid)
    {
        bids.replace(playerStats, bid);

        this.highestBidder = playerStats;
        this.highestBid = bid;
    }

    public void addPlayer(PlayerStats playerStats)
    {
        players.add(playerStats);
    }

    public void removePlayer(PlayerStats playerStats)
    {
        players.remove(playerStats);
    }

    public void broadcastToPlayers(String message)
    {
        for (PlayerStats player : players)
            player.getPlayer().sendMessage(message);
    }

    public boolean inEvent(PlayerStats playerStats)
    {
        return players.contains(playerStats);
    }

    public int getPlayerCount()
    {
        return players.size();
    }
}
