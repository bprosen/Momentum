package com.parkourcraft.Parkour.listeners;


import com.parkourcraft.Parkour.utils.dependencies.WorldGuardUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

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

}
