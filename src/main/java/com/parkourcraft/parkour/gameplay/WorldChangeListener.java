package com.parkourcraft.parkour.gameplay;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.data.stats.StatsManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

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
    }
}
