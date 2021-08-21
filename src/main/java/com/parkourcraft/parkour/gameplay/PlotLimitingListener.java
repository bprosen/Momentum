package com.parkourcraft.parkour.gameplay;

import com.parkourcraft.parkour.Parkour;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PotionSplashEvent;

public class PlotLimitingListener implements Listener {

    /*
        these are all listeners meant for the plot world that disable entities and multiple types of redstone
     */

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        // ZERO entity overloading
        if (event.getLocation().getWorld().getName().equalsIgnoreCase(Parkour.getSettingsManager().player_submitted_world))
            event.setCancelled(true);
    }

    @EventHandler
    public void onDispenserDrop(BlockDispenseEvent event) {
        // ZERO dispenser overloading
        if (event.getBlock().getLocation().getWorld().getName().equalsIgnoreCase(Parkour.getSettingsManager().player_submitted_world))
            event.setCancelled(true);
    }

    @EventHandler
    public void onRedstone(BlockRedstoneEvent event) {
        // ZERO redstone overloading
        if (event.getBlock().getLocation().getWorld().getName().equalsIgnoreCase(Parkour.getSettingsManager().player_submitted_world))
            event.setNewCurrent(0);
    }
}
