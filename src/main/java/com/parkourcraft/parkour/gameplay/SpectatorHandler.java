package com.parkourcraft.parkour.gameplay;

import com.connorlinfoot.titleapi.TitleAPI;
import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class SpectatorHandler implements Listener {

    public static void startScheduler(Plugin plugin) {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            public void run() {
                updateSpectators();
            }
        }, 0L, 10L);
    }

    public static void spectateToPlayer(Player spectator, Player player) {
        if (player.isOnline()
                && spectator.isOnline()) {
            spectator.teleport(player.getLocation());

            TitleAPI.sendTitle(
                    spectator, 10, 40, 10,
                    "", Utils.translate("&7Teleported to " + player.getDisplayName() +
                            "&7, use &2/spectate &7 to stop"));
        }
    }

    public static void respawnToLobby(Player player) {
        Location lobby = Parkour.getLocationManager().getLobbyLocation();
        player.teleport(lobby);
        TitleAPI.sendTitle(
                player, 10, 40, 10,
                "",
                Utils.translate("&7You are no longer spectating anyone"));
    }

    public static void setSpectatorMode(Player spectator) {
        // For pending recode
        //Parkour.getSpectatorManager().setGhost(spectator, true);
        spectator.setAllowFlight(true);
        spectator.setFlying(true);
    }

    public static void removeSpectatorMode(PlayerStats playerStats) {
        playerStats.setPlayerToSpectate(null);
        // For pending recode
        //Parkour.ghostFactory.setGhost(playerStats.getPlayer(), false);
        playerStats.getPlayer().setFlying(false);
        playerStats.getPlayer().setAllowFlight(false);
        respawnToLobby(playerStats.getPlayer());
    }

    public static void updateSpectators() {
        for (PlayerStats playerStats : Parkour.getStatsManager().getPlayerStats()) {
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
