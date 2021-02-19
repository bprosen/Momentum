package com.parkourcraft.parkour.gameplay;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.checkpoints.Checkpoint_DB;
import com.parkourcraft.parkour.utils.PlayerHider;
import com.parkourcraft.parkour.utils.dependencies.WorldGuardUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;

public class JoinLeaveHandler implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        Parkour.getStatsManager().add(player);

        List<String> regions = WorldGuardUtils.getRegions(player.getLocation());
        if (!regions.isEmpty())
            Parkour.getLevelManager().addToLevelMap(player.getName(), regions.get(0));

        PlayerHider.hideHiddenPlayersFromJoined(player);

        // run async
        new BukkitRunnable() {
            public void run() {
                if (Checkpoint_DB.hasCheckpoint(uuid))
                    Checkpoint_DB.loadPlayer(uuid);
            }
        }.runTaskAsynchronously(Parkour.getPlugin());
        //
        // Pending recode
        // Parkour.ghostFactory.addPlayer(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        Player player = event.getPlayer();

        if (Parkour.getLevelManager().getPlayerRegionMap().containsKey(player.getName()))
            Parkour.getLevelManager().removeFromLevelMap(player.getName());

        if (Parkour.getStatsManager().get(player).getCheckpoint() != null)
            Checkpoint_DB.savePlayerAsync(player);

        if (PlayerHider.containsPlayer(player))
            PlayerHider.removeHiddenPlayer(player);
    }
}
