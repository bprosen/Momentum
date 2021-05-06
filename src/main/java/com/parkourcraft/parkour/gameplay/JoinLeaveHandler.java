package com.parkourcraft.parkour.gameplay;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.checkpoints.CheckpointDB;
import com.parkourcraft.parkour.data.events.EventManager;
import com.parkourcraft.parkour.data.plots.Plot;
import com.parkourcraft.parkour.data.races.RaceManager;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.utils.PlayerHider;
import com.parkourcraft.parkour.utils.Utils;
import com.parkourcraft.parkour.utils.dependencies.WorldGuard;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
            Location spawn = Parkour.getLocationManager().getLobbyLocation();
            if (spawn != null) {
                player.teleport(spawn);
                Bukkit.broadcastMessage(Utils.translate(
                        "&a&o" + player.getDisplayName() + "&7&o joined &b&l&oParkour &7&ofor the first time"
                ));
            }
        }
        UUID uuid = player.getUniqueId();
        Parkour.getStatsManager().add(player);
        PlayerHider.hideHiddenPlayersFromJoined(player);

        List<String> regions = WorldGuard.getRegions(player.getLocation());
        if (!regions.isEmpty()) {
            // make sure the area they are spawning in is a level
            if (Parkour.getLevelManager().get(regions.get(0)) != null) {
                Parkour.getStatsManager().get(player).setLevel(regions.get(0));

                // run async
                new BukkitRunnable() {
                    public void run() {
                        if (CheckpointDB.hasCheckpoint(uuid, regions.get(0)))
                            CheckpointDB.loadPlayer(uuid, regions.get(0));
                    }
                }.runTaskAsynchronously(Parkour.getPlugin());
            }
        }

        // send message to op people that there are undecided plots
        if (player.isOp()) {
            List<Plot> submittedPlotList = Parkour.getPlotsManager().getSubmittedPlots();

            if (!submittedPlotList.isEmpty())
                player.sendMessage(Utils.translate("&7There are &c&l" + submittedPlotList.size() + "" +
                                    " &6Submitted Plots &7that still need to be checked! &a/plot submit list"));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        Player player = event.getPlayer();
        PlayerStats playerStats = Parkour.getStatsManager().get(player);
        RaceManager raceManager = Parkour.getRaceManager();
        EventManager eventManager = Parkour.getEventManager();

        // if left with checkpoint, save it
        if (playerStats.getCheckpoint() != null)
            CheckpointDB.savePlayerAsync(player);

        // if left in spectator, remove it
        if (playerStats.getPlayerToSpectate() != null)
            SpectatorHandler.removeSpectatorMode(playerStats);

        // if left in practice mode, reset it
        if (playerStats.getPracticeLocation() != null)
            PracticeHandler.resetPlayer(player, false);

        // if left in race, end it
        if (playerStats.inRace())
            raceManager.endRace(raceManager.get(player).getOpponent(player));

        // if left as hidden, remove them
        if (PlayerHider.containsPlayer(player))
            PlayerHider.removeHiddenPlayer(player);

        // if event is running and they are a participant, remove
        if (playerStats.isEventParticipant())
            eventManager.removeParticipant(player, true);
    }
}
