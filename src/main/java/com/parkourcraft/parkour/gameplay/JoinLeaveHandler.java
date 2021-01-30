package com.parkourcraft.parkour.gameplay;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.utils.dependencies.WorldGuardUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

public class JoinLeaveHandler implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Add to playerStats map (Async)
        Parkour.getStatsManager().add(player);

        List<String> regions = WorldGuardUtils.getRegions(player.getLocation());
        if (!regions.isEmpty())
            Parkour.getLevelManager().addToLevelMap(player.getName(), regions.get(0));

        //
        // Pending recode
        // Parkour.ghostFactory.addPlayer(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        Player player = event.getPlayer();

        if (Parkour.getLevelManager().getPlayerRegionMap().containsKey(player.getName()))
            Parkour.getLevelManager().removeFromLevelMap(player.getName());
    }
}
