package com.renatusnetwork.parkour.gameplay.handlers;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class PracticeHandler {

    public static void shutdown() {
        for (Map.Entry<String, PlayerStats> entry : Parkour.getStatsManager().getPlayerStats().entrySet()) {

            PlayerStats playerStats = entry.getValue();
            if (playerStats.isLoaded() && playerStats.getPlayer().isOnline() && playerStats.inPracticeMode())
                resetPlayer(playerStats, false);
        }
    }

    public static void resetPlayer(PlayerStats playerStats, boolean message) {

        Player player = playerStats.getPlayer();

        player.teleport(playerStats.getPracticeLocation());
        resetDataOnly(playerStats);

        if (message)
            player.sendMessage(Utils.translate("&2You have disabled practice mode"));
    }

    public static void resetDataOnly(PlayerStats playerStats)
    {
        if (playerStats.inPracticeMode())
        {
            Player player = playerStats.getPlayer();
            ItemStack item = Utils.getPracPlateIfExists(player.getInventory());

            if (item != null)
                player.getInventory().remove(item);

            playerStats.resetPracticeMode();
        }
    }
}