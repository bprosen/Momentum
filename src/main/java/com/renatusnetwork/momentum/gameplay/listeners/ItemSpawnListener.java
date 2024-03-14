package com.renatusnetwork.momentum.gameplay.listeners;

import com.renatusnetwork.momentum.Momentum;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;

public class ItemSpawnListener implements Listener {

    @EventHandler
    public void onItemDrop(ItemSpawnEvent event) {

        if (Momentum.getEventManager().isEventRunning() &&
            Momentum.getEventManager().isFallingAnvilEvent() &&
            (event.getEntity() != null &&
            event.getEntity().getItemStack() != null &&
            event.getEntity().getItemStack().getType() == Material.ANVIL))

            event.setCancelled(true);
    }
}
