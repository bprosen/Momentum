package com.renatusnetwork.momentum.gameplay;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class PracticeHandler {

    public static void shutdown() {
        for (Map.Entry<String, PlayerStats> entry : Momentum.getStatsManager().getPlayerStats().entrySet()) {

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