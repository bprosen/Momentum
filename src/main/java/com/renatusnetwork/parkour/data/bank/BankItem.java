package com.renatusnetwork.parkour.data.bank;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.HashMap;

public class BankItem
{
    private BankItemType type;
    private long currentTotal;
    private long nextBidMinimum;
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

    public long getBid(String name)
    {
        return playerBids.get(name);
    }

    public boolean hasBid(String name)
    {
        return playerBids.containsKey(name);
    }

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
        float bidMinimum = 0.0f;

        switch (type)
        {
            case RADIANT:
                bidMinimum = Parkour.getSettingsManager().radiantNextBidMinimum;
                break;
            case BRILLIANT:
                bidMinimum = Parkour.getSettingsManager().brilliantNextBidMinimum;
                break;
            case LEGENDARY:
                bidMinimum = Parkour.getSettingsManager().legendaryNextBidMinimum;
                break;
        }
        nextBidMinimum = currentTotal + ((int) (currentTotal * bidMinimum));
    }
    public long getMinimumNextBid()
    {
        return nextBidMinimum;
    }

    public void setCurrentHolder(String currentHolder) { this.currentHolder = currentHolder; }

    public void broadcastNewBid(PlayerStats playerStats, int bidAmount)
    {


        Bukkit.broadcastMessage(Utils.translate("&d&m-----------------------------------------------------------\n"));
        Bukkit.broadcastMessage(Utils.translate(" &d&lNEW BANK BID"));
        Bukkit.broadcastMessage(Utils.translate(
                " &d" + playerStats.getPlayer().getDisplayName() + " &7put &6" + Utils.formatNumber(bidAmount) + " &eCoins &7for " + displayName
        ));
        Bukkit.broadcastMessage(Utils.translate(" " + displayName + " &7total is now &6" + Utils.formatNumber(currentTotal) + "&eCoins &7in the &d&lBank"));
        Bukkit.broadcastMessage(Utils.translate(" \n&7Next bid starts at &6" + Utils.formatNumber(nextBidMinimum) + " &eCoins"));
        Bukkit.broadcastMessage(Utils.translate(" &7Type &c/bank bid " + ChatColor.stripColor(displayName) + " &c(at least " + Utils.formatNumber(nextBidMinimum)
                + ") &7to overtake &c" + playerStats.getPlayer().getDisplayName()));
        Bukkit.broadcastMessage(Utils.translate("\n&d&m----------------------------------------------------------"));
    }
}
