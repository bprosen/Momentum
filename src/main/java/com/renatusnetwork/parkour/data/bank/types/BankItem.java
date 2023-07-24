package com.renatusnetwork.parkour.data.bank.types;

import com.renatusnetwork.parkour.data.bank.BankDB;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;

import java.util.HashMap;

public abstract class BankItem
{
    private BankItemType type;
    private long currentTotal;
    private long nextBidMinimum;
    private float minimumNextBidRate;
    private String displayName;
    private String currentHolder;

    private String formattedType;

    private HashMap<String, Long> playerBids;

    public BankItem(BankItemType type, int minimumStartingBid, String displayName, String formattedType)
    {
        this.type = type;
        this.displayName = displayName;
        this.nextBidMinimum = minimumStartingBid;
        this.formattedType = formattedType;

        currentTotal = 0;
        currentHolder = null;

        playerBids = BankDB.getBids(type);
    }

    public long getBid(String name)
    {
        return playerBids.get(name);
    }

    public boolean hasBid(String name)
    {
        return playerBids.containsKey(name);
    }

    public String getFormattedType() { return formattedType; }

    public String getDisplayName() { return displayName; }

    public long getCurrentTotal() { return currentTotal; }

    public long getNextBidMinimum() { return nextBidMinimum; }

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

    public boolean hasCurrentHolder() { return currentHolder != null; }

    private void calcMinimumNextBid()
    {
        nextBidMinimum = currentTotal + ((int) (currentTotal * minimumNextBidRate));
    }

    public long getMinimumNextBid()
    {
        return nextBidMinimum;
    }

    public void setCurrentHolder(String currentHolder) { this.currentHolder = currentHolder; }

    public void broadcastNewBid(PlayerStats playerStats, int bidAmount)
    {
        Bukkit.broadcastMessage(Utils.translate("&d&m----------------------------------------"));
        Bukkit.broadcastMessage(Utils.translate(" &d&lNEW " + formattedType + " &d&lBANK BID"));
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(Utils.translate(
                " &d" + playerStats.getPlayer().getDisplayName() + " &7put &6" + Utils.formatNumber(bidAmount) + " &eCoins &7for " + getDisplayName()
        ));
        Bukkit.broadcastMessage(Utils.translate("   " + getDisplayName() + " &7total is now &6" + Utils.formatNumber(getCurrentTotal()) + " &eCoins &7in the " + formattedType + " &d&lBank"));
        Bukkit.broadcastMessage(Utils.translate("   &7Bid &6" + Utils.formatNumber(getMinimumNextBid()) + " &eCoins &7at &c/spawn &7to overtake " + playerStats.getPlayer().getDisplayName()));
        Bukkit.broadcastMessage(Utils.translate("&d&m----------------------------------------"));
    }
}
