package com.renatusnetwork.parkour.gameplay;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.checkpoints.CheckpointDB;
import com.renatusnetwork.parkour.data.events.EventManager;
import com.renatusnetwork.parkour.data.events.EventType;
import com.renatusnetwork.parkour.data.infinite.InfinitePK;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.races.Race;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import com.renatusnetwork.parkour.utils.dependencies.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
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
import org.bukkit.potion.PotionEffect;

import java.util.List;

public class LevelListener implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {

        Player player = event.getPlayer();
        // In water
        if (event.getTo().getBlock().isLiquid()) {
            PlayerStats playerStats = Parkour.getStatsManager().get(player);

            if (playerStats.getLevel() != null) {

                EventManager eventManager = Parkour.getEventManager();

                // if they are participant and fall into water, eliminate them
                if (eventManager.isEventRunning() &&
                    playerStats.isEventParticipant() &&
                    eventManager.getEventType() == EventType.RISING_WATER) {

                    eventManager.doFireworkExplosion(player.getLocation());
                    eventManager.removeParticipant(player, false);
                    eventManager.addEliminated(player);
                    player.sendMessage(Utils.translate("&7You fell in water and got &beliminated out &7of the event!"));

                } else if (playerStats.inRace()) {

                    Race race = Parkour.getRaceManager().get(player);
                    if (race != null) {
                        if (race.isPlayer1(player))
                            race.getPlayer1().teleport(race.getRaceLevel().getRaceLocation1());
                        // swap tp to loc 2 if player 2
                        else
                            race.getPlayer2().teleport(race.getRaceLevel().getRaceLocation2());
                    }
                // if they are not spectating anyone, continue
                } else if (playerStats.getPlayerToSpectate() == null) {
                    Level level = playerStats.getLevel();
                    if (level != null && !level.isDropperLevel() && level.doesLiquidResetPlayer()) {

                        // if is elytra level, set gliding to false
                        if (level.isElytraLevel())
                            player.setGliding(false);

                        if (playerStats.getCheckpoint() != null || playerStats.getPracticeLocation() != null)
                            Parkour.getCheckpointManager().teleportPlayer(playerStats);
                        else
                            LevelHandler.respawnPlayer(player, level);
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
        if (event.getAction().equals(Action.PHYSICAL)) {
            // stone plate = timer start
            if (block.getType() == Material.STONE_PLATE) {

                PlayerStats playerStats = Parkour.getStatsManager().get(player);
                if (playerStats != null && playerStats.getLevel() != null && playerStats.getPracticeLocation() == null && playerStats.getPlayerToSpectate() == null) {

                    // cancel so no click sound and no hogging plate
                    event.setCancelled(true);
                    LevelHandler.startedLevel(player);
                }
            } else if (block.getType() == Material.GOLD_PLATE) {
                // gold plate = checkpoint
                PlayerStats playerStats = Parkour.getStatsManager().get(player);
                if (playerStats != null && playerStats.getLevel() != null && playerStats.getPracticeLocation() == null && playerStats.getPlayerToSpectate() == null) {
                    // cancel so no click sound and no hogging plate
                    event.setCancelled(true);

                    if (playerStats.getCheckpoint() != null) {

                        int blockX = playerStats.getCheckpoint().getBlockX();
                        int blockZ = playerStats.getCheckpoint().getBlockZ();

                        if (!(blockX == block.getLocation().getBlockX() && blockZ == block.getLocation().getBlockZ()))
                            setCheckpoint(player, playerStats, block.getLocation());
                    } else {
                        setCheckpoint(player, playerStats, block.getLocation());
                    }
                }
            } else if (block.getType() == Material.IRON_PLATE) {
                // iron plate = infinite pk or race end
                PlayerStats playerStats = Parkour.getStatsManager().get(player);

                if (playerStats != null) {
                    // cancel so no click sound and no hogging plate
                    event.setCancelled(true);

                    // end if in race
                    if (playerStats.inRace())
                        Parkour.getRaceManager().endRace(player);
                    else if (playerStats.isInInfinitePK()) {

                        // prevent double clicking
                        InfinitePK infinitePK = Parkour.getInfinitePKManager().get(player.getName());

                        if (infinitePK.getPressutePlateLoc().getBlockX() == block.getLocation().getBlockX() &&
                                infinitePK.getPressutePlateLoc().getBlockZ() == block.getLocation().getBlockZ()) {

                            block.setType(Material.AIR);
                            player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 0.35f, 2f);
                            Parkour.getInfinitePKManager().doNextJump(player, false);
                        }
                    }
                }
            }
        }
    }

    private void setCheckpoint(Player player, PlayerStats playerStats, Location location) {
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.25f, 0f);
        playerStats.setCheckpoint(location);

        // update if in ascendance realm
        if (location.getWorld().getName().equalsIgnoreCase(Parkour.getSettingsManager().ascendant_realm_world)) {

            // check region null
            ProtectedRegion region = WorldGuard.getRegion(player.getLocation());
            if (region != null) {

                Level level = Parkour.getLevelManager().get(region.getId());
                // make sure the area they are spawning in is a level and not equal
                if (level != null && !level.getName().equalsIgnoreCase(playerStats.getLevel().getName()))
                    playerStats.setLevel(level);
            }
            // update in ascendance map
            playerStats.updateAscendanceCheckpoint(playerStats.getLevel().getName(), location);
        }

        String msgString = "&eYour checkpoint has been set";
        if (playerStats.getLevelStartTime() > 0) {
            double timeElapsed = System.currentTimeMillis() - playerStats.getLevelStartTime();
            msgString += " &7- &6" + (timeElapsed / 1000.0) + "s";
        }
        player.sendMessage(Utils.translate(msgString));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSignClick(PlayerInteractEvent event) {
        if ((event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.LEFT_CLICK_BLOCK))
             && event.getClickedBlock().getType().equals(Material.WALL_SIGN)) {

            Sign sign = (Sign) event.getClickedBlock().getState();
            String[] signLines = sign.getLines();
            Player player = event.getPlayer();

            if (ChatColor.stripColor(signLines[0]).contains(Parkour.getSettingsManager().signs_first_line) &&
                ChatColor.stripColor(signLines[1]).contains(Parkour.getSettingsManager().signs_second_line_completion)) {

                PlayerStats playerStats = Parkour.getStatsManager().get(player);
                Level level = playerStats.getLevel();

                // level null check
                if (level != null) {
                    // update if in ascendance realm
                    if (player.getWorld().getName().equalsIgnoreCase(Parkour.getSettingsManager().ascendant_realm_world)) {

                        // check region null
                        ProtectedRegion region = WorldGuard.getRegion(event.getClickedBlock().getLocation());
                        if (region != null) {

                            Level levelTo = Parkour.getLevelManager().get(region.getId());
                            // make sure the area they are spawning in is a level and not equal
                            if (levelTo != null && !levelTo.getName().equalsIgnoreCase(level.getName()))
                                playerStats.setLevel(levelTo);
                        }
                    }
                    LevelHandler.levelCompletion(player, playerStats.getLevel().getName());
                }
            } else if (ChatColor.stripColor(signLines[1]).contains(Parkour.getSettingsManager().signs_second_line_spawn)) {
                Location lobby = Parkour.getLocationManager().getLobbyLocation();

                if (lobby != null) {
                    PlayerStats playerStats = Parkour.getStatsManager().get(player);

                    // toggle off elytra armor
                    Parkour.getStatsManager().toggleOffElytra(playerStats);

                    if (playerStats.getCheckpoint() != null) {
                        CheckpointDB.savePlayerAsync(playerStats);
                        playerStats.resetCheckpoint();
                    }

                    playerStats.resetPracticeMode();
                    playerStats.resetLevel();

                    for (PotionEffect potionEffect : player.getActivePotionEffects())
                        player.removePotionEffect(potionEffect.getType());

                    player.teleport(lobby);
                }
            }
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        PlayerStats playerStats = Parkour.getStatsManager().get(player);

        // this is mainly QOL for staff!
        if (playerStats != null && playerStats.getPlayerToSpectate() == null &&
           !playerStats.isEventParticipant() && player.hasPermission("rn-parkour.staff")) {

            // boolean for resetting level
            boolean resetLevel = false;
            ProtectedRegion region = WorldGuard.getRegion(event.getTo());
            if (region != null) {

                // make sure the area they are spawning in is a level
                Level level = Parkour.getLevelManager().get(region.getId());

                if (level != null) {
                    // if player has level and if not same level, then run level change
                    if (playerStats.inLevel() && level.getName().equalsIgnoreCase(playerStats.getLevel().getName()))
                        return;

                    // if they are in a level and have a cp, continue
                    if (playerStats.inLevel() && playerStats.getCheckpoint() != null) {
                        ProtectedRegion checkpointTo = WorldGuard.getRegion(playerStats.getCheckpoint());

                        // if the cp region isnt null, continue and get level
                        if (checkpointTo != null) {
                            Level checkpointLevel = Parkour.getLevelManager().get(checkpointTo.getId());

                            // if they cp level isnt null and the cp level is NOT the same as the level theyre teleporting to, save the cp
                            if (checkpointLevel != null && !checkpointLevel.getName().equalsIgnoreCase(level.getName())) {
                                CheckpointDB.savePlayerAsync(playerStats);
                                playerStats.resetCheckpoint();
                            }
                        }
                    }
                    playerStats.setLevel(level);
                } else if (playerStats.inLevel())
                    resetLevel = true;

            } else if (playerStats.inLevel())
                resetLevel = true;

            if (resetLevel) {
                // save checkpoint if had one
                if (playerStats.getCheckpoint() != null) {
                    CheckpointDB.savePlayerAsync(playerStats);
                    playerStats.resetCheckpoint();
                }
                playerStats.resetLevel();
            }
        }

        if (playerStats != null &&
            playerStats.getPlayerToSpectate() == null &&
            playerStats.getCheckpoint() == null &&
            playerStats.getPracticeLocation() == null) {

            // extra condition to make sure race level timers do not stop once the race has started
            if (playerStats.inLevel() && playerStats.getLevel().isRaceLevel())
                return;

            playerStats.disableLevelStartTime();
        }
    }
}