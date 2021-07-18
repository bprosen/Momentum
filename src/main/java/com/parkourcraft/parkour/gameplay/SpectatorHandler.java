package com.parkourcraft.parkour.gameplay;
import com.connorlinfoot.titleapi.TitleAPI;
import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.utils.PlayerHider;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class SpectatorHandler {

    public static void startScheduler(Plugin plugin) {

        // update any current spectators every second
        new BukkitRunnable() {
            public void run() {
                updateSpectators();
            }
        }.runTaskTimer(plugin, 20, 20);
    }

    public static void spectateToPlayer(Player spectator, Player player) {
        if (player.isOnline() && spectator.isOnline()) {

            spectator.teleport(player.getLocation());

            TitleAPI.sendTitle(
                    spectator, 10, 40, 10,
                    "", Utils.translate("&7Teleported to " + player.getDisplayName() +
                            "&7, use &2/spectate &7 to stop"));
        }
    }

    public static void respawnToLastLocation(PlayerStats playerStats) {
        Location loc = playerStats.getSpectateSpawn();
        Player player = playerStats.getPlayer();

        if (loc != null) {
            player.teleport(loc);
            TitleAPI.sendTitle(
                    player, 10, 40, 10,
                    "",
                    Utils.translate("&7You are no longer spectating anyone"));
            playerStats.resetSpectateSpawn();
        }
    }

    public static void setSpectatorMode(PlayerStats spectatorStats, PlayerStats playerStats, boolean changeLocation) {

        Player spectator = spectatorStats.getPlayer();
        Player player = playerStats.getPlayer();

        spectator.setAllowFlight(true);
        spectator.setFlying(true);
        spectatorStats.setPlayerToSpectate(playerStats);

        // in case they /spectate while spectating
        if (changeLocation)
            spectatorStats.setSpectateSpawn(spectator.getLocation());

        PlayerHider.hidePlayer(spectator, true);
        spectateToPlayer(spectator, player);
    }

    public static void removeSpectatorMode(PlayerStats spectatorStats) {

        Player player = spectatorStats.getPlayer();

        spectatorStats.setPlayerToSpectate(null);
        player.setFlying(false);
        player.setAllowFlight(false);
        PlayerHider.showPlayer(player, true);
        respawnToLastLocation(spectatorStats);
    }

    public static void updateSpectators() {
        for (PlayerStats playerStats : Parkour.getStatsManager().getPlayerStats().values())
            if (playerStats.isLoaded() && playerStats.getPlayer().isOnline() && playerStats.getPlayerToSpectate() != null)
                updateSpectator(playerStats);
    }

    public static void updateSpectator(PlayerStats spectator) {
        PlayerStats playerStats = spectator.getPlayerToSpectate();

        if (playerStats != null && playerStats.getPlayer().isOnline() && playerStats.isSpectatable()) {
            if (spectator.getPlayer().getLocation().distance(playerStats.getPlayer().getLocation()) > 20)
                spectateToPlayer(spectator.getPlayer(), playerStats.getPlayer());
        } else {
            removeSpectatorMode(spectator);
        }
    }

    public static void shutdown() {
        for (Map.Entry<String, PlayerStats> entry : Parkour.getStatsManager().getPlayerStats().entrySet()) {
            
            PlayerStats playerStats = entry.getValue();
            if (playerStats.isLoaded() && playerStats.getPlayer().isOnline() && playerStats.getPlayerToSpectate() != null)
                removeSpectatorMode(playerStats);
        }
    }
}
