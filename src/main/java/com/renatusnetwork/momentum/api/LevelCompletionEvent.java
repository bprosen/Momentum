package com.renatusnetwork.momentum.api;

import com.renatusnetwork.momentum.data.levels.Level;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class LevelCompletionEvent extends Event implements Cancellable {

    private PlayerStats playerStats;
    private Level level;
    private boolean cancelled;
    private int reward;

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public LevelCompletionEvent(PlayerStats playerStats, Level level) {
        this.playerStats = playerStats;
        this.level = level;
        this.reward = level.getReward();
        this.cancelled = false;
    }

    public void setReward(int reward) {
        this.reward = reward;
    }

    public int getReward() {
        return reward;
    }

    public PlayerStats getPlayerStats() {
        return playerStats;
    }

    public Player getPlayer() {
        return playerStats.getPlayer();
    }

    public Level getLevel() {
        return level;
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
