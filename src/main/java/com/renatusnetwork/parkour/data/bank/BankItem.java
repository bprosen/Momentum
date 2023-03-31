package com.renatusnetwork.parkour.data.bank;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.HashMap;

public abstract class BankItem
{
    private BankItemType type;
    private long currentTotal;
    private long nextBidMinimum;
    private float minimumNextBidRate;
    private String displayName;
    private String currentHolder;

    private HashMap<String, Long> playerBids;

    public BankItem(BankItemType type, String displayName)
    {
        this.type = type;
        this.displayName = displayName;

        currentTotal = 0;
        currentHolder = null;

        playerBids = BankDB.getBids(type);
    }

    // abstract methods
    public abstract void broadcastNewBid(PlayerStats playerStats, int bidAmount);

    public long getBid(String name)
    {
        return playerBids.get(name);
    }

    public boolean hasBid(String name)
    {
        return playerBids.containsKey(name);
    }

    public String getDisplayName() { return displayName; }

    public long getCurrentTotal() { return currentTotal; }

    public long getNextBidMinimum() { return nextBidMinimum; }

    public void setNextBidMinimum(long nextBidMinimum) { this.nextBidMinimum = nextBidMinimum; }

    public void setMinimumNextBidRate(float minimumNextBidRate) { this.minimumNextBidRate = minimumNextBidRate; }

    public void addBid(PlayerStats playerStats, int amount)
    {
        this.currentHolder = playerStats.getPlayerName();
        this.currentTotal += amount;

        calcMinimumNextBid();
    }

    public BankItemType getType()
    {
        return type;
    }

    public String getCurrentHolder()
    {
        return currentHolder;
    }

    private void calcMinimumNextBid()
    {
        nextBidMinimum = currentTotal + ((int) (currentTotal * minimumNextBidRate));
    }

    public long getMinimumNextBid()
    {
        return nextBidMinimum;
    }

    public void setCurrentHolder(String currentHolder) { this.currentHolder = currentHolder; }
}
