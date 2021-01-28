package com.parkourcraft.Parkour.gameplay;

import com.parkourcraft.Parkour.Parkour;
import com.parkourcraft.Parkour.data.stats.StatsManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinLeaveHandler implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // Add to playerStats map (Async)
        Parkour.getStatsManager().add(event.getPlayer());
        //
        // Pending recode
        // Parkour.ghostFactory.addPlayer(event.getPlayer());
    }

    /*
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        playerLeft(event.getPlayer());
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        playerLeft(event.getPlayer());
    }*/
}
