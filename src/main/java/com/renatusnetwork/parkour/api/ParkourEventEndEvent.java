package com.renatusnetwork.parkour.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ParkourEventEndEvent extends Event implements Cancellable
{

    private Player winner;
    private int reward;
    private boolean cancelled;

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList()
    {
        return HANDLERS;
    }

    public ParkourEventEndEvent(Player winner, int reward)
    {
        this.winner = winner;
        this.reward = reward;
        this.cancelled = false;
    }

    public Player getWinner()
    {
        return winner;
    }

    public boolean hasWinner()
    {
        return winner != null;
    }

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
