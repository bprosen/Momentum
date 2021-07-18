package com.parkourcraft.parkour.gameplay;

import com.parkourcraft.parkour.Parkour;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;

public class RedstoneListener implements Listener {

    @EventHandler
    public void onRedstone(BlockRedstoneEvent event) {

        Block block = event.getBlock();

        if (block.getLocation().getWorld().getName().equalsIgnoreCase(Parkour.getSettingsManager().player_submitted_world))
            block.setType(Material.AIR);
    }
}
