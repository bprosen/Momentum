package com.renatusnetwork.parkour.gameplay;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;

public class GlideListener implements Listener {

    @EventHandler
    public void onGlide(EntityToggleGlideEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            PlayerStats playerStats = Parkour.getStatsManager().get(player);

            // null check
            if (playerStats != null) {

                // if the level they are in is not an elytra level or their world is not the plot world, cancel it
                if (event.isGliding() &&
                   (!player.getWorld().getName().equalsIgnoreCase(Parkour.getSettingsManager().player_submitted_world) &&
                   !(playerStats.inLevel() && playerStats.getLevel().isElytraLevel()))) {
                    player.setGliding(false);
                    event.setCancelled(true);
                }
            }
        }
    }
}
