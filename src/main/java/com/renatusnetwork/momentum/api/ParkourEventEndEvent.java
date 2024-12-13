package com.renatusnetwork.momentum.api;

import com.renatusnetwork.momentum.data.stats.PlayerStats;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ParkourEventEndEvent extends Event implements Cancellable {

    private PlayerStats winner;
    private int reward;
    private boolean cancelled;

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public ParkourEventEndEvent(PlayerStats winner, int reward) {
        this.winner = winner;
        this.reward = reward;
        this.cancelled = false;
    }

    public PlayerStats getWinner() {
        return winner;
    }

    public Player getWinnerPlayer() {
        return winner.getPlayer();
    }

    public boolean hasWinner() {
        return winner != null;
    }

    public void setReward(int reward) {
        this.reward = reward;
    }

    public int getReward() {
        return reward;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
