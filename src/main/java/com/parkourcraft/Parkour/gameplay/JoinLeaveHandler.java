package com.parkourcraft.Parkour.gameplay;

import com.parkourcraft.Parkour.data.LocationManager;
import com.parkourcraft.Parkour.storage.mysql.DatabaseManager;
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
        DatabaseManager.addToLoadPlayersCache(event.getPlayer());

        if (!event.getPlayer().isOp())
        LocationManager.teleport(event.getPlayer(), "spawn");
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        playerLeft(event.getPlayer());
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        playerLeft(event.getPlayer());
    }

    private void playerLeft(Player player) {
        //Player Head Cache
        //PlayerHeadCache.remove(player.getName());

        //Remove from playerStats list and uploads player information
        //Disabled due to the cache getting cleaned and updating information anyway
        //DatabaseManager.addToUpdatePlayersCache(player.getUniqueId().toString());
    }
}
