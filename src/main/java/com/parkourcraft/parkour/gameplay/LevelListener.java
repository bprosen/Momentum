package com.parkourcraft.parkour.gameplay;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.utils.dependencies.WorldGuardUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.HumanEntity;
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

        if (event.getTo().getBlock().isLiquid()) {
            Location playerLocation = player.getLocation();

            if (!LevelHandler.locationInIgnoreArea(playerLocation)) {
                String levelName = LevelHandler.getLocationLevelName(player);

                if (levelName != null)
                    LevelHandler.respawnPlayerToStart(player, levelName);
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
    public void onStepOnPressurePlate(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.PHYSICAL)
                && event.getClickedBlock().getType().equals(Material.STONE_PLATE)) {
            Player player = event.getPlayer();

            if (!LevelHandler.locationInIgnoreArea(player.getLocation())) {
                String levelName = LevelHandler.getLocationLevelName(player);

                if (levelName != null)
                    LevelHandler.startedLevel(player);
            }
        }
    }

    @EventHandler
    public void onWalkOnPressurePlate(PlayerMoveEvent event) {
        if (event.getTo().getBlock().getRelative(BlockFace.UP)
            .getLocation().add(0, 1, 0).getBlock().getType() == Material.STONE_PLATE) {
            Player player = event.getPlayer();

            if (!LevelHandler.locationInIgnoreArea(player.getLocation())) {
                String levelName = LevelHandler.getLocationLevelName(player);

                if (levelName != null)
                    LevelHandler.startedLevel(player);
            }
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        PlayerStats playerStats = Parkour.getStatsManager().get(player);

        if (playerStats != null && playerStats.getPlayerToSpectate() == null)
            playerStats.disableLevelStartTime();
    }
}