package com.parkourcraft.parkour.gameplay;

import com.parkourcraft.parkour.Parkour;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

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
