package com.parkourcraft.parkour.data.spectator;
import com.connorlinfoot.titleapi.TitleAPI;
import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class SpectatorManager {

    public SpectatorManager(Plugin plugin) {
        startScheduler(plugin);
    }

    public void startScheduler(Plugin plugin) {

        // update any current spectators every second
        new BukkitRunnable() {
            public void run() {
                updateSpectators();
            }
        }.runTaskTimer(plugin, 20, 20);
    }

    public void spectateToPlayer(Player spectator, Player player) {
        if (player.isOnline() && spectator.isOnline()) {

            spectator.teleport(player.getLocation());

            TitleAPI.sendTitle(
                    spectator, 10, 40, 10,
                    "", Utils.translate("&7Teleported to " + player.getDisplayName() +
                            "&7, use &2/spectate &7 to stop"));
        }
    }

    public void respawnToLobby(Player player) {
        Location lobby = Parkour.getLocationManager().getLobbyLocation();
        player.teleport(lobby);
        TitleAPI.sendTitle(
                player, 10, 40, 10,
                "",
                Utils.translate("&7You are no longer spectating anyone"));
    }

    public void setSpectatorMode(Player spectator, Player player) {
        spectator.setAllowFlight(true);
        spectator.setFlying(true);
        spectator.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
        spectateToPlayer(spectator, player);
    }

    public void removeSpectatorMode(PlayerStats spectatorStats) {

        Player player = spectatorStats.getPlayer();

        spectatorStats.setPlayerToSpectate(null);
        player.setFlying(false);
        player.setAllowFlight(false);
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        respawnToLobby(player);
    }

    public void updateSpectators() {
        for (PlayerStats playerStats : Parkour.getStatsManager().getPlayerStats()) {
            if (playerStats.isLoaded() && playerStats.getPlayer().isOnline() && playerStats.getPlayerToSpectate() != null)
                updateSpectator(playerStats);
        }
    }

    public void updateSpectator(PlayerStats spectator) {
        PlayerStats playerStats = spectator.getPlayerToSpectate();

        if (playerStats != null && playerStats.getPlayer().isOnline() && playerStats.isSpectatable()) {
            if (spectator.getPlayer().getLocation().distance(playerStats.getPlayer().getLocation()) > 20)
                spectateToPlayer(spectator.getPlayer(), playerStats.getPlayer());
        } else {
            removeSpectatorMode(spectator);
        }
    }

    public void shutdown() {
        for (PlayerStats playerStats : Parkour.getStatsManager().getPlayerStats()) {
            if (playerStats.isLoaded() && playerStats.getPlayer().isOnline() && playerStats.getPlayerToSpectate() != null)
                removeSpectatorMode(playerStats);
        }
    }
}
