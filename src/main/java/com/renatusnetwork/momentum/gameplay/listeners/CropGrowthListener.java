<<<<<<<< HEAD:src/main/java/com/renatusnetwork/momentum/gameplay/listeners/CropGrowthListener.java
package com.renatusnetwork.momentum.gameplay.listeners;
========
package com.renatusnetwork.momentum.gameplay;
>>>>>>>> master:src/main/java/com/renatusnetwork/momentum/gameplay/CropGrowthListener.java

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;

public class CropGrowthListener implements Listener {

    // growth listener for stuff like cocoa beans
    @EventHandler
    public void onGrowth(BlockGrowEvent event) {
        if (event.getBlock().getType() == Material.COCOA) {
            event.setCancelled(true);
        }
    }
}
