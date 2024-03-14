package com.renatusnetwork.momentum.gameplay;

import com.renatusnetwork.momentum.Momentum;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;

public class BowListener implements Listener {

    @EventHandler
    public void onItemUse(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            // if not in plot world
            if (!player.getWorld().getName().equalsIgnoreCase(Momentum.getSettingsManager().player_submitted_world))
                event.setCancelled(true);
        }
    }
}
