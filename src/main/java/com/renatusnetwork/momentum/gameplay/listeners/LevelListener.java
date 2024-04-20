package com.renatusnetwork.momentum.gameplay.listeners;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.checkpoints.CheckpointDB;
import com.renatusnetwork.momentum.data.events.EventManager;
import com.renatusnetwork.momentum.data.events.types.AscentEvent;
import com.renatusnetwork.momentum.data.events.types.MazeEvent;
import com.renatusnetwork.momentum.data.events.types.RisingWaterEvent;
import com.renatusnetwork.momentum.data.infinite.gamemode.Infinite;
import com.renatusnetwork.momentum.data.levels.Level;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.data.stats.StatsManager;
import com.renatusnetwork.momentum.utils.TimeUtils;
import com.renatusnetwork.momentum.utils.Utils;
import com.renatusnetwork.momentum.utils.dependencies.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class LevelListener implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {

        Player player = event.getPlayer();
        // In water
        if (event.getTo().getBlock().isLiquid())
        {
            PlayerStats playerStats = Momentum.getStatsManager().get(player);

            if (playerStats != null && playerStats.isLoaded() && playerStats.inLevel())
            {

                EventManager eventManager = Momentum.getEventManager();

                // if they are participant and fall into water, eliminate them
                if (eventManager.isEventRunning() && playerStats.isEventParticipant())
                {
                    if (eventManager.isRisingWaterEvent() && ((RisingWaterEvent) eventManager.getRunningEvent()).isStartCoveredInWater())
                    {
                        Utils.spawnFirework(player.getLocation(), Color.RED, Color.RED, false);
                        eventManager.removeParticipant(player, false);
                        eventManager.addEliminated(player);
                        player.sendMessage(Utils.translate("&7You are &beliminated &7out of the event!"));
                    }
                    else if (eventManager.isAscentEvent())
                    {
                        // level down
                        ((AscentEvent) eventManager.getRunningEvent()).levelDown(player);
                    }
                    else if (eventManager.isMazeEvent())
                    {
                        // respawn
                        ((MazeEvent) eventManager.getRunningEvent()).respawn(player);
                    }
                }
                else if (!playerStats.isSpectating())
                {
                    Level level = playerStats.getLevel();
                    if (level != null && !level.isDropper() && level.doesLiquidResetPlayer())
                    {

                        // if is elytra level, set gliding to false
                        if (level.isElytra())
                            player.setGliding(false);

                        if (playerStats.hasCurrentCheckpoint() || playerStats.inPracticeMode())
                            Momentum.getCheckpointManager().teleportToCheckpoint(playerStats);
                        else
                            Momentum.getLevelManager().respawnPlayer(playerStats, level);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onStepOnPressurePlate(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        // Start timer
        if (event.getAction().equals(Action.PHYSICAL) && !player.getWorld().getName().equalsIgnoreCase(Momentum.getSettingsManager().player_submitted_world))
        {
            PlayerStats playerStats = Momentum.getStatsManager().get(player);

            // stone plate = timer start
            if (block.getType() == Material.STONE_PLATE)
            {
                event.setCancelled(true);
                if (
                    playerStats != null &&
                    playerStats.isLoaded() &&
                    playerStats.inLevel() &&
                    !playerStats.inPracticeMode() &&
                    !playerStats.isSpectating() &&
                    !playerStats.isPreviewingLevel() &&
                    !playerStats.hasCurrentCheckpoint()
                )
                    // cancel so no click sound and no hogging plate
                    playerStats.startedLevel();

            }
            else if (block.getType() == Material.GOLD_PLATE)
            {
                event.setCancelled(true);

                // gold plate = checkpoint
                if (
                    playerStats != null &&
                    playerStats.isLoaded() &&
                    playerStats.inLevel() &&
                    !playerStats.inPracticeMode() &&
                    !playerStats.isSpectating() &&
                    !playerStats.isAttemptingMastery() &&
                    !playerStats.isPreviewingLevel() &&
                    (!playerStats.hasCurrentCheckpoint() || !Utils.isNearby(block.getLocation(), playerStats.getCurrentCheckpoint(), 1.5))
                )
                    setCheckpoint(playerStats, block.getLocation());
            }
            else if (block.getType() == Material.IRON_PLATE)
            {
                // cancel so no click sound and no hogging plate
                event.setCancelled(true);
                EventManager eventManager = Momentum.getEventManager();

                if (playerStats != null && playerStats.isLoaded())
                {
                    if (playerStats.isInInfinite())
                    {
                        // prevent double clicking
                        Infinite infinite = Momentum.getInfiniteManager().get(player.getName());
                        Location blockLoc = infinite.getPlateBlock().getLocation();

                        if (blockLoc.getBlockX() == block.getLocation().getBlockX() && blockLoc.getBlockZ() == block.getLocation().getBlockZ())
                        {
                            infinite.addScore();
                            infinite.next();
                        }
                    }
                    else if (eventManager.isEventRunning() && playerStats.isEventParticipant())
                    {
                        if (eventManager.isAscentEvent())
                            // level up
                            ((AscentEvent) eventManager.getRunningEvent()).levelUp(player);
                        else if (eventManager.isMazeEvent())
                            // end event!
                            eventManager.endEvent(player, false, false);
                    }
                }
            }
        }
    }

    private void setCheckpoint(PlayerStats playerStats, Location location)
    {
        Player player = playerStats.getPlayer();
        boolean inRace = playerStats.inRace();

        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.25f, 0f);

        // delete if they have a cp
        if (!inRace)
            if (playerStats.hasCurrentCheckpoint())
                CheckpointDB.updateCheckpoint(playerStats, location);
            else
                CheckpointDB.insertCheckpoint(playerStats, location);

        playerStats.setCurrentCheckpoint(location);

        if (!inRace)
            playerStats.removeCheckpoint(playerStats.getLevel());

        // update if in ascendance realm
        if (location.getWorld().getName().equalsIgnoreCase(Momentum.getSettingsManager().ascendant_realm_world))
        {
            // check region null
            ProtectedRegion region = WorldGuard.getRegion(player.getLocation());
            if (region != null)
            {
                Level level = Momentum.getLevelManager().get(region.getId());

                // make sure the area they are spawning in is a level and not equal
                if (level != null && !level.getName().equalsIgnoreCase(playerStats.getLevel().getName()))
                    playerStats.setLevel(level);
            }
        }

        if (!inRace)
            playerStats.addCheckpoint(playerStats.getLevel(), location);

        String msgString = "&eYour checkpoint has been set";
        if (playerStats.getLevelStartTime() > 0)
        {
            long timeElapsed = System.currentTimeMillis() - playerStats.getLevelStartTime();
            msgString += " &6(" + TimeUtils.formatCompletionTimeTaken(timeElapsed, 3) + ")";
        }

        player.sendMessage(Utils.translate(msgString));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSignClick(PlayerInteractEvent event)
    {
        if ((event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.LEFT_CLICK_BLOCK))
             && event.getClickedBlock().getType().equals(Material.WALL_SIGN)
             && !event.getClickedBlock().getWorld().getName().equalsIgnoreCase(Momentum.getSettingsManager().player_submitted_world))
        {
            // return if gamemode 0, opped and left clicked
            Player player = event.getPlayer();
            if (player.isOp() && player.getGameMode() == GameMode.CREATIVE && event.getAction().equals(Action.LEFT_CLICK_BLOCK))
                return;

            Sign sign = (Sign) event.getClickedBlock().getState();
            String[] signLines = sign.getLines();

            if (ChatColor.stripColor(signLines[0]).contains(Momentum.getSettingsManager().signs_first_line))
            {
                PlayerStats playerStats = Momentum.getStatsManager().get(player);

                if (playerStats != null && playerStats.isLoaded())
                {
                    if (ChatColor.stripColor(signLines[1]).contains(Momentum.getSettingsManager().signs_second_line_completion))
                    {
                        Level level = playerStats.getLevel();

                        if (level != null)
                        {
                            // check region null
                            ProtectedRegion region = WorldGuard.getRegion(event.getClickedBlock().getLocation());
                            if (region != null)
                            {
                                Level levelTo = Momentum.getLevelManager().get(region.getId());
                                // make sure the area they are spawning in is a level and not equal
                                if (levelTo != null && !levelTo.getName().equalsIgnoreCase(level.getName()))
                                {
                                    // if they are glitching elytra -> !elytra, remove elytra!
                                    if (level.isElytra() && !levelTo.isElytra())
                                        Momentum.getStatsManager().toggleOffElytra(playerStats);

                                    playerStats.setLevel(levelTo);
                                }
                            }
                            Momentum.getLevelManager().validateAndRunLevelCompletion(playerStats, level);
                        }
                    }
                    else if (ChatColor.stripColor(signLines[1]).contains(Momentum.getSettingsManager().signs_second_line_spawn))
                    {
                        Location lobby = Momentum.getLocationManager().getSpawnLocation();

                        if (lobby != null)
                        {
                            if (playerStats.inLevel() && playerStats.hasAutoSave() && !playerStats.getPlayer().isOnGround())
                            {
                                player.sendMessage(Utils.translate("&cYou cannot leave the level while in midair with auto-save enabled"));
                                return;
                            }

                            Momentum.getStatsManager().leaveLevelAndReset(playerStats, true);
                            player.teleport(lobby);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        PlayerStats playerStats = Momentum.getStatsManager().get(player);

        // this is mainly QOL for staff!
        if (playerStats != null && playerStats.isLoaded() && !playerStats.isSpectating() &&
           !playerStats.isEventParticipant() && player.hasPermission("momentum.staff"))
        {

            // boolean for resetting level
            boolean resetLevel = false;
            ProtectedRegion region = WorldGuard.getRegion(event.getTo());
            if (region != null) {

                // make sure the area they are spawning in is a level
                Level levelTo = Momentum.getLevelManager().get(region.getId());

                if (levelTo != null) {
                    // if player has level and if not same level, then run level change
                    if (playerStats.inLevel() && levelTo.getName().equalsIgnoreCase(playerStats.getLevel().getName()))
                        return;

                    // if they are in a level and have a cp, continue
                    if (playerStats.inLevel() && playerStats.hasCurrentCheckpoint()) {
                        ProtectedRegion currentCPRegion = WorldGuard.getRegion(playerStats.getCurrentCheckpoint());

                        // if the cp region isnt null, continue and get level
                        if (currentCPRegion != null) {
                            Level currentLevel = Momentum.getLevelManager().get(currentCPRegion.getId());

                            // if they cp level isnt null and the cp level is NOT the same as the level theyre teleporting to, save the cp
                            if (currentLevel != null && !currentLevel.getName().equalsIgnoreCase(levelTo.getName()))
                            {
                                playerStats.resetCurrentCheckpoint();

                                // set cp if finds one
                                Location newCP = playerStats.getCheckpoint(levelTo);
                                playerStats.setCurrentCheckpoint(newCP);
                            }
                        }
                    }
                    playerStats.setLevel(levelTo);

                    // enable tutorial if they tp to it and not in tutorial
                    if (levelTo.getName().equalsIgnoreCase(Momentum.getLevelManager().getTutorialLevel().getName()) && !playerStats.isInTutorial())
                        playerStats.setTutorial(true);

                } else if (playerStats.inLevel())
                    resetLevel = true;

            } else if (playerStats.inLevel())
                resetLevel = true;

            if (resetLevel)
            {
                // save checkpoint if had one
                playerStats.resetCurrentCheckpoint();
                playerStats.resetLevel();

                // disable tutorial
                if (playerStats.isInTutorial())
                    playerStats.setTutorial(false);
            }
        }
    }
}