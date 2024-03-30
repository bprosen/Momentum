package com.renatusnetwork.momentum.data.bank.items;

import com.renatusnetwork.momentum.data.modifiers.Modifier;
import com.renatusnetwork.momentum.data.stats.PlayerStats;

public abstract class BankItem
{
    private BankItemType type;
    private String name;
    private long totalBalance;
    private int nextBid;
    private String title;
    private int minimumLock;
    private String formattedType;
    private String currentHolder;
    private String description;
    private Modifier modifier;
    private boolean locked;

    public BankItem(BankItemType type)
    {
        this.type = type;
    }

    public abstract void calcNextBid();

    public void setTotalBalance(int total)
    {
        this.totalBalance = total;
        calcNextBid();
    }

    public boolean isLocked()
    {
        return locked;
    }

    public void setName(String name) { this.name = name; }

    public String getName() { return name; }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public void setLocked(boolean isLocked)
    {
        this.locked = isLocked;
    }

    public String getFormattedType() { return formattedType; }

    public String getDescription() { return description; }

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

    public String getTitle() { return title; }

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

    public boolean isCurrentHolder(PlayerStats playerStats)
    {
        return hasCurrentHolder() && this.currentHolder.equalsIgnoreCase(playerStats.getName());
    }

    public boolean hasCurrentHolder() { return currentHolder != null && !currentHolder.isEmpty(); }

    public void setCurrentHolder(String currentHolder) { this.currentHolder = currentHolder; }
}
