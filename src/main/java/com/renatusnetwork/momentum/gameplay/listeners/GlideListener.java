package com.renatusnetwork.momentum.gameplay.listeners;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;

public class GlideListener implements Listener {

    @EventHandler
    public void onGlide(EntityToggleGlideEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            PlayerStats playerStats = Momentum.getStatsManager().get(player);

            // null check
            if (playerStats != null) {

                // if the level they are in is not an elytra level or their world is not the plot world, cancel it
                if (event.isGliding() &&
                   (!player.getWorld().getName().equalsIgnoreCase(Momentum.getSettingsManager().player_submitted_world) &&
                   !(playerStats.inLevel() && playerStats.getLevel().isElytra()))) {
                    player.setGliding(false);
                    event.setCancelled(true);
                }
            }
        }
    }
}
