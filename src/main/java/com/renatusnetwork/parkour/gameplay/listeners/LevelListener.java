package com.renatusnetwork.parkour.gameplay.listeners;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.checkpoints.CheckpointDB;
import com.renatusnetwork.parkour.data.events.EventManager;
import com.renatusnetwork.parkour.data.events.types.AscentEvent;
import com.renatusnetwork.parkour.data.events.types.MazeEvent;
import com.renatusnetwork.parkour.data.events.types.RisingWaterEvent;
import com.renatusnetwork.parkour.data.infinite.gamemode.Infinite;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.races.gamemode.Race;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.gameplay.handlers.LevelHandler;
import com.renatusnetwork.parkour.gameplay.handlers.PracticeHandler;
import com.renatusnetwork.parkour.utils.Utils;
import com.renatusnetwork.parkour.utils.dependencies.WorldGuard;
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
        if (event.getTo().getBlock().isLiquid()) {
            PlayerStats playerStats = Parkour.getStatsManager().get(player);

            if (playerStats.getLevel() != null)
            {

                EventManager eventManager = Parkour.getEventManager();

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
                            Parkour.getCheckpointManager().teleportToCP(playerStats);
                        else
                            LevelHandler.respawnPlayer(playerStats, level);
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
        if (event.getAction().equals(Action.PHYSICAL))
        {
            PlayerStats playerStats = Parkour.getStatsManager().get(player);

            if (playerStats != null)
            {
                // stone plate = timer start
                if (block.getType() == Material.STONE_PLATE)
                {
                    event.setCancelled(true);
                    if (playerStats.inLevel() && !playerStats.inPracticeMode() && !playerStats.isSpectating() && !playerStats.isPreviewingLevel() && !playerStats.hasCurrentCheckpoint())
                        // cancel so no click sound and no hogging plate
                        playerStats.startedLevel();

                }
                else if (block.getType() == Material.GOLD_PLATE)
                {
                    event.setCancelled(true);

                    // gold plate = checkpoint
                    if (
                            playerStats.inLevel() &&
                            !playerStats.inRace() &&
                            !playerStats.inPracticeMode() &&
                            !playerStats.isSpectating() &&
                            !playerStats.isAttemptingMastery() &&
                            !playerStats.isPreviewingLevel())
                    {
                        if (playerStats.hasCurrentCheckpoint())
                        {
                            int blockX = playerStats.getCurrentCheckpoint().getBlockX();
                            int blockZ = playerStats.getCurrentCheckpoint().getBlockZ();

                            if (!(blockX == block.getLocation().getBlockX() && blockZ == block.getLocation().getBlockZ()))
                                setCheckpoint(playerStats, block.getLocation());
                        }
                        else
                            setCheckpoint(playerStats, block.getLocation());
                    }
                }
                else if (block.getType() == Material.IRON_PLATE)
                {
                    // cancel so no click sound and no hogging plate
                    event.setCancelled(true);
                    EventManager eventManager = Parkour.getEventManager();

                    if (playerStats.isInInfinite())
                    {
                        // prevent double clicking
                        Infinite infinite = Parkour.getInfiniteManager().get(player.getName());
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

        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.25f, 0f);

        // delete if they have a cp
        if (playerStats.hasCurrentCheckpoint())
            CheckpointDB.deleteCheckpoint(playerStats.getUUID(), playerStats.getLevel().getName());

        playerStats.setCurrentCheckpoint(location);
        playerStats.removeCheckpoint(playerStats.getLevel());

        // update if in ascendance realm
        if (location.getWorld().getName().equalsIgnoreCase(Parkour.getSettingsManager().ascendant_realm_world))
        {
            // check region null
            ProtectedRegion region = WorldGuard.getRegion(player.getLocation());
            if (region != null)
            {
                Level level = Parkour.getLevelManager().get(region.getId());

                // make sure the area they are spawning in is a level and not equal
                if (level != null && !level.getName().equalsIgnoreCase(playerStats.getLevel().getName()))
                    playerStats.setLevel(level);
            }
        }

        playerStats.addCheckpoint(playerStats.getLevel(), location);

        String msgString = "&eYour checkpoint has been set";
        if (playerStats.getLevelStartTime() > 0)
        {
            double timeElapsed = System.currentTimeMillis() - playerStats.getLevelStartTime();
            msgString += " &6(" + (timeElapsed / 1000.0) + "s)";
        }

        player.sendMessage(Utils.translate(msgString));
        CheckpointDB.insertCheckpoint(playerStats, location);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSignClick(PlayerInteractEvent event)
    {
        if ((event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.LEFT_CLICK_BLOCK))
             && event.getClickedBlock().getType().equals(Material.WALL_SIGN)
             && !event.getClickedBlock().getWorld().getName().equalsIgnoreCase(Parkour.getSettingsManager().player_submitted_world))
        {
            // return if gamemode 0, opped and left clicked
            Player player = event.getPlayer();
            if (player.isOp() && player.getGameMode() == GameMode.CREATIVE && event.getAction().equals(Action.LEFT_CLICK_BLOCK))
                return;

            Sign sign = (Sign) event.getClickedBlock().getState();
            String[] signLines = sign.getLines();

            if (ChatColor.stripColor(signLines[0]).contains(Parkour.getSettingsManager().signs_first_line))
            {
                if (ChatColor.stripColor(signLines[1]).contains(Parkour.getSettingsManager().signs_second_line_completion))
                {
                    PlayerStats playerStats = Parkour.getStatsManager().get(player);
                    Level level = playerStats.getLevel();

                    if (level != null)
                    {
                        // check region null
                        ProtectedRegion region = WorldGuard.getRegion(event.getClickedBlock().getLocation());
                        if (region != null)
                        {
                            Level levelTo = Parkour.getLevelManager().get(region.getId());
                            // make sure the area they are spawning in is a level and not equal
                            if (levelTo != null && !levelTo.getName().equalsIgnoreCase(level.getName()))
                            {
                                // if they are glitching elytra -> !elytra, remove elytra!
                                if (level.isElytra() && !levelTo.isElytra())
                                    Parkour.getStatsManager().toggleOffElytra(playerStats);

                                playerStats.setLevel(levelTo);
                            }
                        }
                        LevelHandler.levelCompletion(playerStats, level);
                    }
                }
                else if (ChatColor.stripColor(signLines[1]).contains(Parkour.getSettingsManager().signs_second_line_spawn))
                {
                    Location lobby = Parkour.getLocationManager().getLobbyLocation();

                    if (lobby != null)
                    {
                        PlayerStats playerStats = Parkour.getStatsManager().get(player);

                        // toggle off elytra armor
                        Parkour.getStatsManager().toggleOffElytra(playerStats);

                        playerStats.resetCurrentCheckpoint();
                        PracticeHandler.resetDataOnly(playerStats);
                        playerStats.resetLevel();

                        if (playerStats.isAttemptingRankup())
                            Parkour.getStatsManager().leftRankup(playerStats);

                        if (playerStats.isAttemptingMastery())
                            Parkour.getStatsManager().leftMastery(playerStats);

                        playerStats.clearPotionEffects();

                        player.teleport(lobby);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        PlayerStats playerStats = Parkour.getStatsManager().get(player);

        // this is mainly QOL for staff!
        if (playerStats != null && !playerStats.isSpectating() &&
           !playerStats.isEventParticipant() && player.hasPermission("rn-parkour.staff"))
        {

            // boolean for resetting level
            boolean resetLevel = false;
            ProtectedRegion region = WorldGuard.getRegion(event.getTo());
            if (region != null) {

                // make sure the area they are spawning in is a level
                Level levelTo = Parkour.getLevelManager().get(region.getId());

                if (levelTo != null) {
                    // if player has level and if not same level, then run level change
                    if (playerStats.inLevel() && levelTo.getName().equalsIgnoreCase(playerStats.getLevel().getName()))
                        return;

                    // if they are in a level and have a cp, continue
                    if (playerStats.inLevel() && playerStats.hasCurrentCheckpoint()) {
                        ProtectedRegion currentCPRegion = WorldGuard.getRegion(playerStats.getCurrentCheckpoint());

                        // if the cp region isnt null, continue and get level
                        if (currentCPRegion != null) {
                            Level currentLevel = Parkour.getLevelManager().get(currentCPRegion.getId());

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
                    if (levelTo.getName().equalsIgnoreCase(Parkour.getLevelManager().getTutorialLevel().getName()) && !playerStats.isInTutorial())
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