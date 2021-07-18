package com.parkourcraft.parkour.gameplay;

import com.parkourcraft.parkour.Parkour;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

public class EntitySpawnListener implements Listener {

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        // ZERO entity overloading
        if (event.getLocation().getWorld().getName().equalsIgnoreCase(Parkour.getSettingsManager().player_submitted_world))
            event.setCancelled(true);
    }
}
