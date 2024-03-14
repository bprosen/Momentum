package com.renatusnetwork.momentum.api;

import com.renatusnetwork.momentum.data.levels.Level;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Collection;

public class LevelBuyEvent extends Event implements Cancellable
{
    private PlayerStats playerStats;
    private Collection<Level> levels;
    private int totalPrice;
    private boolean cancelled;

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList()
    {
        return HANDLERS;
    }

    public LevelBuyEvent(PlayerStats playerStats, Collection<Level> levels, int totalPrice)
    {
        this.playerStats = playerStats;
        this.levels = levels;
        this.totalPrice = totalPrice;
        this.cancelled = false;
    }

    public PlayerStats getPlayerStats()
    {
        return playerStats;
    }

    public Player getPlayer() { return playerStats.getPlayer(); }

    public Collection<Level> getLevels()
    {
        return levels;
    }

    public int getTotalPrice() { return totalPrice; }

    public void setTotalPrice(int totalPrice)
    {
        this.totalPrice = totalPrice;
    }

    @Override
    public boolean isCancelled()
    {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled)
    {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
