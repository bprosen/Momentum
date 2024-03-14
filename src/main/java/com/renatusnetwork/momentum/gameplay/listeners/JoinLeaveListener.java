package com.renatusnetwork.momentum.gameplay.listeners;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.blackmarket.BlackMarketManager;
import com.renatusnetwork.momentum.data.clans.ClansManager;
import com.renatusnetwork.momentum.data.events.EventManager;
import com.renatusnetwork.momentum.data.infinite.InfiniteManager;
import com.renatusnetwork.momentum.data.levels.Level;
import com.renatusnetwork.momentum.data.plots.Plot;
import com.renatusnetwork.momentum.data.races.gamemode.RaceEndReason;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.data.stats.StatsManager;
import com.renatusnetwork.momentum.utils.Utils;
import com.renatusnetwork.momentum.utils.dependencies.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;
import java.util.List;

public class JoinLeaveListener implements Listener
{
    // We use PlayerSpawnLocationEvent instead of PlayerJoinEvent since we rely on their spawn location to load their data
    @EventHandler
    public void onJoin(PlayerSpawnLocationEvent event)
    {
        Player player = event.getPlayer();
        StatsManager statsManager = Momentum.getStatsManager();

        if (!player.hasPlayedBefore())
        {
            Location spawn = Momentum.getLocationManager().getTutorialLocation();
            if (spawn != null)
            {
                event.setSpawnLocation(spawn);
                statsManager.addTotalPlayer();

                // set inventory
                Utils.setHotbar(player);

                Bukkit.broadcastMessage(Utils.translate(
                        "&7Welcome &a" + player.getDisplayName() + " &7to &b&lParkour &d#" + Utils.formatNumber(statsManager.getTotalPlayers())
                ));
            }
        }
        statsManager.hideHiddenPlayersFromJoined(player);

        // send message to op people that there are undecided plots
        if (player.isOp())
        {
            List<Plot> submittedPlotList = Momentum.getPlotsManager().getSubmittedPlots();

            if (!submittedPlotList.isEmpty())
                player.sendMessage(Utils.translate("&7There are &c&l" + submittedPlotList.size() +
                        " &6Submitted Plots &7that still need to be checked! &a/plot submit list"));
        }

        Location spawnLoc = event.getSpawnLocation();
        PlayerStats playerStats = statsManager.getOffline(player.getUniqueId().toString());
        boolean fromOffline = playerStats != null;

        if (!fromOffline)
            playerStats = statsManager.add(player);
        else
            statsManager.addFromOffline(playerStats, player); // add from offline

        PlayerStats finalPlayerStats = playerStats;
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                finalPlayerStats.initBoard(); // init board always on join

                // dont need to load if not offline
                if (!fromOffline)
                    statsManager.loadStats(finalPlayerStats);

                ProtectedRegion region = WorldGuard.getRegion(spawnLoc);

                if (region != null)
                {
                    Level level = Momentum.getLevelManager().get(region.getId());

                    // make sure the area they are spawning in is a level
                    if (level != null)
                    {
                        // if the level they are being added to is an ascendance level, add them to the list
                        if (level.isAscendance())
                            statsManager.enteredAscendance(finalPlayerStats);

                        Location checkpoint = finalPlayerStats.getCheckpoint(level);
                        if (checkpoint != null && !finalPlayerStats.isAttemptingMastery()) // only load cp if they are not in a mastery attempt
                            finalPlayerStats.setCurrentCheckpoint(checkpoint);

                        // toggle tutorial
                        if (level.equals(Momentum.getLevelManager().getTutorialLevel()))
                            finalPlayerStats.setTutorial(true);

                        // set level AFTER checkpoint, and other important stats have been set
                        finalPlayerStats.setLevel(level);

                        // is elytra level, then set elytra in sync (player inventory changes)
                        if (level.isElytra())
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    Momentum.getStatsManager().toggleOnElytra(finalPlayerStats);
                                }
                            }.runTask(Momentum.getPlugin());
                    }
                }

                // mark player as finished loading
                finalPlayerStats.loaded();
            }
        }.runTaskAsynchronously(Momentum.getPlugin());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();

        StatsManager statsManager = Momentum.getStatsManager();
        PlayerStats playerStats = statsManager.get(player);
        EventManager eventManager = Momentum.getEventManager();
        InfiniteManager infiniteManager = Momentum.getInfiniteManager();
        ClansManager clansManager = Momentum.getClansManager();
        BlackMarketManager blackMarketManager = Momentum.getBlackMarketManager();

        // if left in spectator, remove it
        if (playerStats.isSpectating())
            statsManager.resetSpectatorMode(playerStats);

        // if left in practice mode, reset it
        if (playerStats.inPracticeMode())
            statsManager.resetPracticeMode(playerStats, false);

        // if left in race, end it, give winner
        if (playerStats.inRace())
            playerStats.endRace(playerStats.getRace().getOpponent(), RaceEndReason.FORFEIT);

        // if left in black market, remove them
        if (playerStats.isInBlackMarket())
            blackMarketManager.playerLeft(playerStats, true);

        // if left as hidden, remove them
        if (statsManager.containsHiddenPlayer(player))
            statsManager.togglePlayerHiderOff(player, false);

        // if event is running and they are a participant, remove
        if (playerStats.isEventParticipant())
            eventManager.removeParticipant(player, true);

        // if in infinite pk, end it
        if (playerStats.isInInfinite())
            infiniteManager.endPK(player);

        // if night vision is enabled, clear it
        if (playerStats.hasNightVision())
            playerStats.clearPotionEffects();

        // reset preview if in a level previewing it
        playerStats.resetPreviewLevel();

        if (playerStats.inLevel())
        {
            // if in dropper, respawn them
            if (playerStats.getLevel().isDropper())
                player.teleport(playerStats.getLevel().getStartLocation());

            // run reset logic
            playerStats.resetLevel();
        }

        // toggle off elytra armor
        statsManager.toggleOffElytra(playerStats);

        // do not need to check, as method already checks
        clansManager.toggleClanChat(player.getName(), null);
        clansManager.toggleChatSpy(player.getName(), true);

        playerStats.deleteBoard();

        // finally, remove them from the stats list
        statsManager.remove(playerStats);

        statsManager.addOffline(playerStats); // add to offline cache
    }
}
