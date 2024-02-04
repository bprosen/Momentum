package com.renatusnetwork.parkour.gameplay.handlers;
import com.connorlinfoot.titleapi.TitleAPI;
import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
public class SpectatorHandler {

    public static void spectateToPlayer(Player spectator, Player player, boolean initialSpectate) {
        if (player.isOnline() && spectator.isOnline()) {

            spectator.teleport(player.getLocation());

            // this is done AFTER teleport to override some world changes that can happen
            if (initialSpectate)
            {
                spectator.setAllowFlight(true);
                spectator.setFlying(true);
            }

            TitleAPI.sendTitle(
                    spectator, 10, 40, 10,
                    Utils.translate("&7Teleported to " + player.getDisplayName()), Utils.translate("&2/spectate &7 to exit"
                    ));
        }
    }

    public static void respawnToLastLocation(PlayerStats playerStats) {
        Location loc = playerStats.getSpectateSpawn();
        Player player = playerStats.getPlayer();

        if (loc != null)
        {

            player.teleport(loc);
            TitleAPI.sendTitle(
                    player, 10, 40, 10,
                    "",
                    Utils.translate("&7You are no longer spectating anyone"));
            playerStats.resetSpectateSpawn();

            Parkour.getLevelManager().regionLevelCheck(playerStats, loc);
        }
    }

    public static void setSpectatorMode(PlayerStats spectatorStats, PlayerStats playerStats, boolean initialSpectate) {

        Player spectator = spectatorStats.getPlayer();
        Player player = playerStats.getPlayer();

        spectatorStats.setPlayerToSpectate(playerStats);

        // in case they /spectate while spectating
        if (initialSpectate)
        {
            spectatorStats.setSpectateSpawn(spectator.getLocation());
            Parkour.getStatsManager().toggleOffElytra(spectatorStats);
        }

        spectateToPlayer(spectator, player, initialSpectate);
    }

    public static void removeSpectatorMode(PlayerStats spectatorStats) {

        Player player = spectatorStats.getPlayer();

        spectatorStats.setPlayerToSpectate(null);

        if (!player.isOp())
        {
            player.setFlying(false);
            player.setAllowFlight(false);
        }

        respawnToLastLocation(spectatorStats);
    }

    public static void shutdown()
    {
        for (PlayerStats playerStats : Parkour.getStatsManager().getOnlinePlayers())
        {
            if (playerStats != null && playerStats.isSpectating())
                removeSpectatorMode(playerStats);
        }
    }
}
