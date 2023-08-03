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
    private String formattedType;
    private String currentHolder;
    private Modifier modifier;

    public BankItem(BankItemType type)
    {
        this.type = type;
        this.totalBalance = BankYAML.getTotal(type);
        this.currentHolder = BankYAML.getHolder(type);
        this.modifier = Parkour.getModifiersManager().getModifier(BankYAML.getModifier(type));
    }

    public Modifier getModifier() { return modifier; }

    public void setModifier(Modifier modifier) { this.modifier = modifier; }

    public String getDisplayName() { return displayName; }

    public void setFormattedType(String formattedType)
    {
        this.formattedType = formattedType;
    }

    public long getTotalBalance() { return totalBalance; }

    public int getNextBid() { return nextBid; }

    public void addBid(PlayerStats playerStats, int amount)
    {
        this.currentHolder = playerStats.getPlayerName();
        this.totalBalance += amount;
        calcNextBid();
    }

    private void calcNextBid()
    {
        this.nextBid = (int) (Math.sqrt(100 * totalBalance));
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
