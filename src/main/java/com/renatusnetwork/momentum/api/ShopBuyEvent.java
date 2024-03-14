package com.renatusnetwork.momentum.api;

import com.renatusnetwork.momentum.data.perks.Perk;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
public class ShopBuyEvent extends Event implements Cancellable
{
    private PlayerStats playerStats;
    private Perk perk;
    private int price;
    private boolean cancelled;

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList()
    {
        return HANDLERS;
    }

    public ShopBuyEvent(PlayerStats playerStats, Perk perk)
    {
        this.playerStats = playerStats;
        this.perk = perk;
        this.price = perk.getPrice();
        this.cancelled = false;
    }

    public PlayerStats getPlayerStats()
    {
        return playerStats;
    }

    public Player getPlayer() { return playerStats.getPlayer(); }

    public int getPrice() { return price; }

    public void setPrice(int price)
    {
        this.price = price;
    }

    public Perk getPerk() { return perk; }

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