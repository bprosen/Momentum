package com.parkourcraft.Parkour.gameplay;


import com.connorlinfoot.titleapi.TitleAPI;
import com.parkourcraft.Parkour.Parkour;
import com.parkourcraft.Parkour.data.LocationManager;
import com.parkourcraft.Parkour.data.StatsManager;
import com.parkourcraft.Parkour.data.stats.PlayerStats;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class SpectatorHandler implements Listener {

    public static void spectateToPlayer(Player spectator, Player player) {
        if (player.isOnline()
                && spectator.isOnline()) {
            spectator.teleport(player.getLocation());

            TitleAPI.sendTitle(
                    spectator, 10, 40, 10,
                    "",
                    ChatColor.GRAY + "Teleported to " + player.getDisplayName()
                    + ChatColor.GRAY + ", use "
                    + ChatColor.GREEN + "/spectate"
                    + ChatColor.GRAY + " to stop"
            );
        }
    }

    public static void respawnToLobby(Player player) {
        Location lobby = Parkour.locationManager.getLobbyLocation();

        player.teleport(lobby);

        TitleAPI.sendTitle(
                player, 10, 40, 10,
                "",
                ChatColor.GRAY + "You are no longer spectating anyone"
        );
    }

    public static void setSpectatorMode(Player spectator) {
        Parkour.ghostFactory.setGhost(spectator, true);
        spectator.setAllowFlight(true);
        spectator.setFlying(true);
    }

    public static void removeSpectatorMode(PlayerStats playerStats) {
        playerStats.setPlayerToSpectate(null);
        Parkour.ghostFactory.setGhost(playerStats.getPlayer(), false);
        playerStats.getPlayer().setFlying(false);
        playerStats.getPlayer().setAllowFlight(false);
        respawnToLobby(playerStats.getPlayer());
    }

    public static void updateSpectators() {
        for (PlayerStats playerStats : StatsManager.getPlayerStats()) {
            if (playerStats.isLoaded()
                    && playerStats.getPlayer().isOnline()
                    && playerStats.getPlayerToSpectate() != null)
                updateSpectator(playerStats);
        }
    }

    public static void updateSpectator(PlayerStats spectatorStats) {
        PlayerStats playerStats = spectatorStats.getPlayerToSpectate();

        if (playerStats != null
                && playerStats.getPlayer().isOnline()
                && playerStats.isSpectatable()) {
            if (playerStats.getPlayer().getLocation().distance(spectatorStats.getPlayer().getLocation()) > 20)
                spectateToPlayer(spectatorStats.getPlayer(), playerStats.getPlayer());
        } else
            removeSpectatorMode(spectatorStats);
    }

}
