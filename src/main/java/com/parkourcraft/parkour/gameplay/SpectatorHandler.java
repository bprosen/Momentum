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

    public static void respawnToLobby(Player player) {
        Location lobby = Parkour.getLocationManager().getLobbyLocation();
        player.teleport(lobby);
        TitleAPI.sendTitle(
                player, 10, 40, 10,
                "",
                Utils.translate("&7You are no longer spectating anyone"));
    }

    public static void setSpectatorMode(Player spectator, Player player) {
        spectator.setAllowFlight(true);
        spectator.setFlying(true);
        PlayerHider.hidePlayer(spectator);
        spectateToPlayer(spectator, player);
    }

    public static void removeSpectatorMode(PlayerStats spectatorStats) {

        Player player = spectatorStats.getPlayer();

        spectatorStats.setPlayerToSpectate(null);
        player.setFlying(false);
        player.setAllowFlight(false);
        PlayerHider.showPlayer(player);
        respawnToLobby(player);
    }

    public static void updateSpectators() {
        for (PlayerStats playerStats : Parkour.getStatsManager().getPlayerStats()) {
            if (playerStats.isLoaded() && playerStats.getPlayer().isOnline() && playerStats.getPlayerToSpectate() != null)
                updateSpectator(playerStats);
        }
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
        for (PlayerStats playerStats : Parkour.getStatsManager().getPlayerStats()) {
            if (playerStats.isLoaded() && playerStats.getPlayer().isOnline() && playerStats.getPlayerToSpectate() != null)
                removeSpectatorMode(playerStats);
        }
    }
}
