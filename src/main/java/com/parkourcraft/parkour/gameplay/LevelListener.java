package com.parkourcraft.parkour.gameplay;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.checkpoints.CheckpointDB;
import com.parkourcraft.parkour.data.events.EventManager;
import com.parkourcraft.parkour.data.events.EventType;
import com.parkourcraft.parkour.data.infinite.InfinitePK;
import com.parkourcraft.parkour.data.levels.Level;
import com.parkourcraft.parkour.data.races.Race;
import com.parkourcraft.parkour.data.races.RaceManager;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.utils.Utils;
import com.parkourcraft.parkour.utils.dependencies.WorldGuard;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
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
                } else {
                    Level level = playerStats.getLevel();

                    if (level != null && level.doesLiquidResetPlayer()) {

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
        PlayerStats playerStats = Parkour.getStatsManager().get(player);

        // Start timer
        if (event.getAction().equals(Action.PHYSICAL) && block.getType().equals(Material.STONE_PLATE)) {
            if (playerStats.getLevel() != null && playerStats.getPracticeLocation() == null && playerStats.getPlayerToSpectate() == null)
                LevelHandler.startedLevel(player);

        // Checkpoint
        } else if (event.getAction().equals(Action.PHYSICAL) && block.getType().equals(Material.GOLD_PLATE)) {

            if (playerStats.getLevel() != null) {
                if (playerStats.getPracticeLocation() == null) {
                    if (playerStats.getPlayerToSpectate() == null) {
                        if (playerStats.getCheckpoint() != null) {

                            int blockX = playerStats.getCheckpoint().getBlockX();
                            int blockZ = playerStats.getCheckpoint().getBlockZ();

                            if (!(blockX == block.getLocation().getBlockX()) && !(blockZ == block.getLocation().getBlockZ()))
                                setCheckpoint(player, playerStats, block.getLocation());
                        } else {
                            setCheckpoint(player, playerStats, block.getLocation());
                        }
                    }
                }
            }
        } else if (event.getAction().equals(Action.PHYSICAL) && block.getType().equals(Material.IRON_PLATE)) {
            // end if in race

            if (playerStats.inRace())
                Parkour.getRaceManager().endRace(player);
            else if (playerStats.isInInfinitePK()) {

                block.setType(Material.AIR);

                // prevent double clicking
                InfinitePK infinitePK = Parkour.getInfinitePKManager().get(player.getName());

                if (infinitePK.getPressutePlateLoc().getBlockX() == block.getLocation().getBlockX() &&
                    infinitePK.getPressutePlateLoc().getBlockZ() == block.getLocation().getBlockZ())

                Parkour.getInfinitePKManager().doNextJump(player, false);
            }
        }
    }

    private void setCheckpoint(Player player, PlayerStats playerStats, Location location) {
        playerStats.setCheckpoint(location);

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

                Level level = Parkour.getStatsManager().get(player).getLevel();

                if (level != null)
                    LevelHandler.levelCompletion(player, level.getName());

            } else if (ChatColor.stripColor(signLines[1]).contains(Parkour.getSettingsManager().signs_second_line_spawn)) {
                Location lobby = Parkour.getLocationManager().getLobbyLocation();

                if (lobby != null) {
                    PlayerStats playerStats = Parkour.getStatsManager().get(player);

                    // toggle off elytra armor
                    Parkour.getStatsManager().toggleOffElytra(playerStats);

                    if (playerStats.getCheckpoint() != null) {
                        CheckpointDB.savePlayerAsync(player);
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
        if (playerStats != null && player.hasPermission("pc-parkour.staff")) {
            List<String> regions = WorldGuard.getRegions(event.getTo());
            if (!regions.isEmpty()) {

                // make sure the area they are spawning in is a level
                Level level = Parkour.getLevelManager().get(regions.get(0));

                if (level != null)
                    playerStats.setLevel(level);

            } else if (playerStats.getLevel() != null) {
                // save checkpoint if had one
                if (playerStats.getCheckpoint() != null) {
                    CheckpointDB.savePlayerAsync(player);
                    playerStats.resetCheckpoint();
                }
                playerStats.resetLevel();
            }
        }

        if (playerStats != null &&
            playerStats.getPlayerToSpectate() == null &&
            playerStats.getCheckpoint() == null &&
            playerStats.getPracticeLocation() == null) {
            playerStats.disableLevelStartTime();
        }
    }
}