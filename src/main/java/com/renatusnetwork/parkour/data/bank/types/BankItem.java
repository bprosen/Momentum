package com.renatusnetwork.parkour.data.bank.types;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.bank.BankYAML;
import com.renatusnetwork.parkour.data.modifiers.Modifier;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;

public abstract class BankItem
{
    private BankItemType type;
    private long totalBalance;
    private int nextBid;
    private String displayName;
    private int minimumLock;
    private String formattedType;
    private String currentHolder;
    private Modifier modifier;
    private boolean locked;

    public BankItem(BankItemType type)
    {
        this.type = type;
        this.totalBalance = BankYAML.getTotal(type);
        this.currentHolder = BankYAML.getHolder(type);
        this.modifier = Parkour.getModifiersManager().getModifier(BankYAML.getModifier(type));
        this.locked = false;
    }

    public boolean isLocked()
    {
        return locked;
    }

    public void setLocked(boolean isLocked)
    {
        this.locked = isLocked;
    }

    public String getFormattedType() { return formattedType; }

    public Modifier getModifier() { return modifier; }

    public void setMinimumLock(int minimumLock)
    {
        this.minimumLock = minimumLock;
    }

    public int getMinimumLock()
    {
        return minimumLock;
    }

    public void setModifier(Modifier modifier) { this.modifier = modifier; }

    public String getDisplayName() { return displayName; }

    public void setFormattedType(String formattedType)
    {
        this.formattedType = formattedType;
    }

    public long getTotalBalance() { return totalBalance; }

    public int getNextBid() { return nextBid; }

    public void addTotal(int amount)
    {
        this.totalBalance += amount;
    }

    public abstract void calcNextBid();

    public void setNextBid(int nextBid)
    {
        this.nextBid = nextBid;
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

    public void setCurrentHolder(String currentHolder) { this.currentHolder = currentHolder; }

    public void broadcastNewBid(PlayerStats playerStats, int bidAmount)
    {
        Bukkit.broadcastMessage(Utils.translate("&d&m----------------------------------------"));
        Bukkit.broadcastMessage(Utils.translate("&d&lNEW " + formattedType + " &d&lBANK BID"));
        Bukkit.broadcastMessage(Utils.translate("&d" + playerStats.getPlayer().getDisplayName() + " &7bid &6" + Utils.formatNumber(bidAmount) + " &eCoins &7for " + getDisplayName()));
        Bukkit.broadcastMessage(Utils.translate("&7Bid &6" + Utils.formatNumber(nextBid) + " &eCoins &7at &c/spawn &7to overtake " + playerStats.getPlayer().getDisplayName()));
        Bukkit.broadcastMessage(Utils.translate( getDisplayName() + " &7total is now &6" + Utils.formatNumber(totalBalance) + " &eCoins &7in the " + formattedType + " &d&lBank"));
        Bukkit.broadcastMessage(Utils.translate("&d&m----------------------------------------"));
    }
}
