package com.renatusnetwork.parkour.gameplay;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.data.stats.StatsManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class WorldChangeListener implements Listener {

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {

        Player player = event.getPlayer();
        StatsManager statsManager = Parkour.getStatsManager();
        PlayerStats playerStats = statsManager.get(player);

        // if entering ascendance world, add to list to start tracking
        if (player.getWorld().getName().equalsIgnoreCase(Parkour.getSettingsManager().ascendant_realm_world))
            statsManager.enteredAscendance(playerStats);
        // if they are switching to not ascendance world and were in ascendance, remove them
        else if (statsManager.isInAscendance(playerStats))
            statsManager.leftAscendance(playerStats);

        // if going to/from plot world, clear inv, armor and potions
        if (player.getWorld().getName().equalsIgnoreCase(Parkour.getSettingsManager().player_submitted_world) ||
            event.getFrom().getName().equalsIgnoreCase(Parkour.getSettingsManager().player_submitted_world)) {
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);

            // clear potion effects
            for (PotionEffect potionEffect : player.getActivePotionEffects())
                player.removePotionEffect(potionEffect.getType());
        }
    }
}
