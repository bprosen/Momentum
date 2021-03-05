package com.parkourcraft.parkour.gameplay;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.checkpoints.Checkpoint_DB;
import com.parkourcraft.parkour.data.levels.LevelObject;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.data.stats.StatsManager;
import com.parkourcraft.parkour.utils.Utils;
import com.parkourcraft.parkour.utils.dependencies.WorldGuard;
import org.bukkit.Bukkit;
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

import java.util.List;

public class LevelListener implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {

        Player player = event.getPlayer();
        // In water
        if (event.getTo().getBlock().isLiquid()) {
            String levelName = Parkour.getStatsManager().get(player).getLevel();
            if (levelName != null) {

                PlayerStats playerStats = Parkour.getStatsManager().get(player);

                if (playerStats.getCheckpoint() != null || playerStats.getPracticeLocation() != null)
                    Parkour.getCheckpointManager().teleportPlayer(player);
                else
                    LevelHandler.respawnPlayer(player, Parkour.getLevelManager().get(levelName));
            }
        }
    }

    @EventHandler
    public void onStepOnPressurePlate(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        // Start timer
        if (event.getAction().equals(Action.PHYSICAL) && block.getType().equals(Material.STONE_PLATE)) {

            String levelName = Parkour.getStatsManager().get(player).getLevel();
            if (levelName != null)
                LevelHandler.startedLevel(player);

        // Checkpoint
        } else if (event.getAction().equals(Action.PHYSICAL) && block.getType().equals(Material.GOLD_PLATE)) {

            PlayerStats playerStats = Parkour.getStatsManager().get(player);
            String levelName = Parkour.getStatsManager().get(player).getLevel();

            if (levelName != null) {
                if (playerStats.getPracticeLocation() == null) {
                    if (playerStats.getCheckpoint() != null) {

                        int blockX = playerStats.getCheckpoint().getBlockX();
                        int blockZ = playerStats.getCheckpoint().getBlockZ();

                        if (!(blockX == block.getLocation().getBlockX()) && !(blockZ == block.getLocation().getBlockZ())) {
                            playerStats.setCheckpoint(block.getLocation());
                            player.sendMessage(Utils.translate("&eYour checkpoint has been set"));
                        }
                    } else {
                        playerStats.setCheckpoint(block.getLocation());
                        player.sendMessage(Utils.translate("&eYour checkpoint has been set"));
                    }
                } else {
                    player.sendMessage(Utils.translate("&cYou cannot set a checkpoint while in practice mode"));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSignClick(PlayerInteractEvent event) {
        if ((event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.LEFT_CLICK_BLOCK))
             && event.getClickedBlock().getType().equals(Material.WALL_SIGN)) {

            Sign sign = (Sign) event.getClickedBlock().getState();
            String[] signLines = sign.getLines();

            if (ChatColor.stripColor(signLines[0]).contains(Parkour.getSettingsManager().signs_first_line)) {
                Player player = event.getPlayer();

                if (ChatColor.stripColor(signLines[1]).contains(Parkour.getSettingsManager().signs_second_line_completion)) {
                    String levelName = Parkour.getStatsManager().get(player).getLevel();

                    if (levelName != null)
                        LevelHandler.levelCompletion(player, levelName);
                } else if (ChatColor.stripColor(signLines[1]).contains(Parkour.getSettingsManager().signs_second_line_spawn)) {
                    Location lobby = Parkour.getLocationManager().getLobbyLocation();

                    if (lobby != null)
                        player.teleport(lobby);
                }
            }
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        PlayerStats playerStats = Parkour.getStatsManager().get(player);

        // this is mainly QOL for operators!
        if (player.isOp()) {
            List<String> regions = WorldGuard.getRegions(event.getTo());
            if (!regions.isEmpty()) {

                // make sure the area they are spawning in is a level
                LevelObject level = Parkour.getLevelManager().get(regions.get(0));
                if (level != null && !level.getName().equalsIgnoreCase(playerStats.getLevel()))
                    Parkour.getStatsManager().get(player).setLevel(regions.get(0));

            } else if (playerStats.getLevel() != null) {
                // save checkpoint if had one
                if (playerStats.getCheckpoint() != null) {
                    Checkpoint_DB.savePlayerAsync(player);
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