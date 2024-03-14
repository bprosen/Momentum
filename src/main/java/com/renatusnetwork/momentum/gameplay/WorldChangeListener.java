package com.renatusnetwork.momentum.gameplay;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.data.stats.StatsManager;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class WorldChangeListener implements Listener {

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {

        Player player = event.getPlayer();
        StatsManager statsManager = Momentum.getStatsManager();
        PlayerStats playerStats = statsManager.get(player);

        // avoid all this code if spectating someone
        if (!playerStats.isSpectating()) {
            // if entering ascendance world, add to list to start tracking
            if (player.getWorld().getName().equalsIgnoreCase(Momentum.getSettingsManager().ascendant_realm_world))
                statsManager.enteredAscendance(playerStats);
                // if they are switching to not ascendance world and were in ascendance, remove them
            else if (statsManager.isInAscendance(playerStats))
                statsManager.leftAscendance(playerStats);

            // if going to/from plot world, clear inv, armor and potions
            if (player.getWorld().getName().equalsIgnoreCase(Momentum.getSettingsManager().player_submitted_world) ||
                    event.getFrom().getName().equalsIgnoreCase(Momentum.getSettingsManager().player_submitted_world)) {
                player.getInventory().clear();
                player.getInventory().setArmorContents(null);

                // clear potion effects
                playerStats.clearPotionEffects();

                if (event.getFrom().getName().equalsIgnoreCase(Momentum.getSettingsManager().player_submitted_world))
                    // reset hotbar
                    Utils.setHotbar(player);
            }
        }
    }
}
