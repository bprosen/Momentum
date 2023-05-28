package com.renatusnetwork.parkour.api;

import com.renatusnetwork.parkour.data.clans.Clan;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ClanXPRewardEvent extends Event implements Cancellable
{
    private Clan clan;
    private int xp;
    private boolean cancelled;

    private static final HandlerList HANDLERS = new HandlerList();

    public ClanXPRewardEvent(Clan clan, int xp)
    {
        this.clan = clan;
        this.xp = xp;
        this.cancelled = false;
    }

    public Clan getClan() {
        return clan;
    }

    public int getXP() {
        return xp;
    }

    public void setXP(int xp) {
        this.xp = xp;
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
