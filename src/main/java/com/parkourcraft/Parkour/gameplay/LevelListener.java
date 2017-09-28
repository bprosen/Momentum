package com.parkourcraft.Parkour.gameplay;

import com.parkourcraft.Parkour.data.LocationManager;
import com.parkourcraft.Parkour.data.StatsManager;
import com.parkourcraft.Parkour.data.stats.PlayerStats;
import com.parkourcraft.Parkour.storage.local.FileManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
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

        if (event.getTo().getBlock().isLiquid()) {
            Location playerLocation = player.getLocation();

            if (!LevelHandler.locationInIgnoreArea(playerLocation)) {
                String levelName = LevelHandler.getLocationLevelName(playerLocation);

                if (levelName != null)
                    LevelHandler.respawnPlayerToStart(player, levelName);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSignClick(PlayerInteractEvent event) {
        if ((event.getAction().equals(Action.RIGHT_CLICK_BLOCK)
                        || event.getAction().equals(Action.LEFT_CLICK_BLOCK))
                && event.getClickedBlock().getType().equals(Material.WALL_SIGN)) {
            Sign sign = (Sign) event.getClickedBlock().getState();
            String[] signLines = sign.getLines();

            FileConfiguration settings = FileManager.getFileConfig("settings");
            String firstLine = settings.getString("signs.first_line");

            if (ChatColor.stripColor(signLines[0]).contains(firstLine)) {
                Player player = event.getPlayer();
                String secondLineCompletion = settings.getString("signs.second_line.completion");
                String secondLineSpawn = settings.getString("signs.second_line.spawn");

                if (ChatColor.stripColor(signLines[1]).contains(secondLineCompletion)) {
                    String levelName = LevelHandler.getLocationLevelName(player.getLocation());
                    if (levelName != null)
                        LevelHandler.levelCompletion(player, levelName);
                } else if (ChatColor.stripColor(signLines[1]).contains(secondLineSpawn)) {
                    Location lobby = LocationManager.getLobbyLocation();

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
            String levelName = LevelHandler.getLocationLevelName(player.getLocation());

            if (levelName != null) {
                PlayerStats playerStats = StatsManager.get(event.getPlayer());

                if (playerStats != null)
                    playerStats.startedLevel();
            }
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        PlayerStats playerStats = StatsManager.get(event.getPlayer());

        if (playerStats != null)
            playerStats.disableLevelStartTime();
    }

}
