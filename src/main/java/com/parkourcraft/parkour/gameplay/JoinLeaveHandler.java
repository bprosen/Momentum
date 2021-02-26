package com.parkourcraft.parkour.gameplay;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.checkpoints.Checkpoint_DB;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.utils.PlayerHider;
import com.parkourcraft.parkour.utils.Utils;
import com.parkourcraft.parkour.utils.dependencies.WorldGuardUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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

        if (!player.hasPlayedBefore()) {
            Location spawn = Parkour.getSettingsManager().spawn_location;
            if (spawn != null) {
                player.teleport(Parkour.getSettingsManager().spawn_location);
                Bukkit.broadcastMessage(Utils.translate(
                        "&a&o" + player.getDisplayName() + "&7&o joined &b&l&oParkour &7&ofor the first time"
                ));
            }
        }
        UUID uuid = player.getUniqueId();
        Parkour.getStatsManager().add(player);
        PlayerHider.hideHiddenPlayersFromJoined(player);

        List<String> regions = WorldGuardUtils.getRegions(player.getLocation());
        if (!regions.isEmpty()) {
            Parkour.getStatsManager().get(player).setLevel(regions.get(0));
            // run async
            new BukkitRunnable() {
                public void run() {
                    if (Checkpoint_DB.hasCheckpoint(uuid, regions.get(0)))
                        Checkpoint_DB.loadPlayer(uuid, regions.get(0));
                }
            }.runTaskAsynchronously(Parkour.getPlugin());
        }
        //
        // Pending recode
        // Parkour.ghostFactory.addPlayer(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        Player player = event.getPlayer();
        PlayerStats playerStats = Parkour.getStatsManager().get(player);

        if (playerStats.getCheckpoint() != null)
            Checkpoint_DB.savePlayerAsync(player);

        if (playerStats.getPlayerToSpectate() != null)
            Parkour.getSpectatorManager().removeSpectatorMode(playerStats);

        if (PlayerHider.containsPlayer(player))
            PlayerHider.removeHiddenPlayer(player);
    }
}
