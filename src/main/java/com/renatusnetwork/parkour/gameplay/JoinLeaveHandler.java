package com.renatusnetwork.parkour.gameplay;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.checkpoints.CheckpointDB;
import com.renatusnetwork.parkour.data.clans.ClansManager;
import com.renatusnetwork.parkour.data.events.EventManager;
import com.renatusnetwork.parkour.data.infinite.InfinitePKManager;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.plots.Plot;
import com.renatusnetwork.parkour.data.races.RaceManager;
import com.renatusnetwork.parkour.data.saves.SavesDB;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.data.stats.StatsDB;
import com.renatusnetwork.parkour.data.stats.StatsManager;
import com.renatusnetwork.parkour.utils.PlayerHider;
import com.renatusnetwork.parkour.utils.Utils;
import com.renatusnetwork.parkour.utils.dependencies.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.List;

public class JoinLeaveHandler implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        if (!player.hasPlayedBefore())
        {
            Location spawn = Parkour.getLocationManager().getTutorialLocation();
            if (spawn != null)
            {
                player.teleport(spawn);

                StatsManager statsManager = Parkour.getStatsManager();

                statsManager.addTotalPlayer();
                Bukkit.broadcastMessage(Utils.translate(
                        "&7Welcome &a" + player.getDisplayName() + " &7to &b&lParkour &d#" + Utils.formatNumber(statsManager.getTotalPlayers())
                ));
            }
        }
        PlayerHider.hideHiddenPlayersFromJoined(player);

        // send message to op people that there are undecided plots
        if (player.isOp()) {
            List<Plot> submittedPlotList = Parkour.getPlotsManager().getSubmittedPlots();

            if (!submittedPlotList.isEmpty())
                player.sendMessage(Utils.translate("&7There are &c&l" + submittedPlotList.size() + "" +
                        " &6Submitted Plots &7that still need to be checked! &a/plot submit list"));
        }

        // set inventory
        Utils.setHotbar(player);

        new BukkitRunnable() {
            @Override
            public void run() {

                StatsManager statsManager = Parkour.getStatsManager();
                statsManager.add(player);
                PlayerStats playerStats = statsManager.get(player);

                // now load stats in async
                StatsDB.loadPlayerStats(playerStats);

                // load checkpoints
                CheckpointDB.loadCheckpoints(playerStats);
                SavesDB.loadSaves(playerStats);

                // run most of this in async (region lookup, stat editing, etc)
                new BukkitRunnable() {
                    @Override
                    public void run() {

                        // if not spectating
                        if (playerStats.getPlayerToSpectate() == null) {
                            // load level, checkpoint info here
                            ProtectedRegion region = WorldGuard.getRegion(player.getLocation());
                            if (region != null) {

                                Level level = Parkour.getLevelManager().get(region.getId());

                                // make sure the area they are spawning in is a level
                                if (level != null) {
                                    playerStats.setLevel(level);

                                    // toggle tutorial
                                    if (level.getName().equalsIgnoreCase(Parkour.getLevelManager().getTutorialLevel().getName()))
                                        playerStats.setTutorial(true);

                                    // if the level they are being added to is an ascendance level, add them to the list
                                    if (level.isAscendanceLevel())
                                        statsManager.enteredAscendance(playerStats);

                                    Location checkpoint = playerStats.getCheckpoint(level.getName());
                                    if (checkpoint != null)
                                        playerStats.setCurrentCheckpoint(checkpoint);

                                    // is elytra level, then set elytra in sync (player inventory changes)
                                    if (level.isElytraLevel())
                                        new BukkitRunnable() {
                                            @Override
                                            public void run() {
                                                Parkour.getStatsManager().toggleOnElytra(playerStats);
                                            }
                                        }.runTask(Parkour.getPlugin());
                                }
                            }
                        }
                    }
                }.runTaskLaterAsynchronously(Parkour.getPlugin(), 20 * 3);
            }
        }.runTaskAsynchronously(Parkour.getPlugin());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        Player player = event.getPlayer();
        PlayerStats playerStats = Parkour.getStatsManager().get(player);
        RaceManager raceManager = Parkour.getRaceManager();
        EventManager eventManager = Parkour.getEventManager();
        InfinitePKManager infinitePKManager = Parkour.getInfinitePKManager();
        ClansManager clansManager = Parkour.getClansManager();

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

        // if in infinite pk, end it
        if (playerStats.isInInfinitePK())
            infinitePKManager.endPK(player, true);

        // if night vision is enabled, clear it
        if (playerStats.hasNVStatus())
            playerStats.clearPotionEffects();

        // if in dropper, respawn them
        if (playerStats.inLevel() && playerStats.getLevel().isDropperLevel())
            player.teleport(playerStats.getLevel().getStartLocation());

        // toggle off elytra armor
        Parkour.getStatsManager().toggleOffElytra(playerStats);

        // do not need to check, as method already checks
        clansManager.toggleClanChat(player.getName(), null);
        clansManager.toggleChatSpy(player.getName(), true);

        playerStats.getBoard().delete();

        // finally, remove them from the stats list
        Parkour.getStatsManager().remove(playerStats);
    }
}
