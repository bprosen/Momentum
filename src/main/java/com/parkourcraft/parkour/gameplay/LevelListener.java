package com.parkourcraft.parkour.gameplay;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.checkpoints.CheckpointManager;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.utils.Utils;
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

public class LevelListener implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {

        Player player = event.getPlayer();
        // In water
        if (event.getTo().getBlock().isLiquid()) {
            String levelName = LevelHandler.getLocationLevelName(player);
            if (levelName != null) {
                if (Parkour.getStatsManager().get(player).getCheckpoint() != null)
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

            String levelName = LevelHandler.getLocationLevelName(player);
            if (levelName != null)
                LevelHandler.startedLevel(player);

        // Checkpoint
        } else if (event.getAction().equals(Action.PHYSICAL) && block.getType().equals(Material.GOLD_PLATE)) {

            PlayerStats playerStats = Parkour.getStatsManager().get(player);
            String levelName = LevelHandler.getLocationLevelName(player);

            if (levelName != null) {
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
                    String levelName = LevelHandler.getLocationLevelName(player);

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

        if (playerStats != null && playerStats.getPlayerToSpectate() == null && playerStats.getCheckpoint() == null)
            playerStats.disableLevelStartTime();
    }
}