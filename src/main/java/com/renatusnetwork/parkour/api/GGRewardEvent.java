package com.renatusnetwork.parkour.api;

import com.renatusnetwork.parkour.data.stats.PlayerStats;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GGRewardEvent extends Event implements Cancellable
{
    private PlayerStats playerStats;
    private int reward;
    private boolean cancelled;

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList()
    {
        return HANDLERS;
    }

    public GGRewardEvent(PlayerStats playerStats, int reward)
    {
        this.playerStats = playerStats;
        this.reward = reward;
        this.cancelled = false;
    }

    public PlayerStats getPlayerStats()
    {
        return playerStats;
    }

    public Player getPlayer() { return playerStats.getPlayer(); }

    public void setReward(int reward)
    {
        this.reward = reward;
    }

    public int getReward()
    {
        return reward;
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
