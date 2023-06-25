package com.renatusnetwork.parkour.gameplay;

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
            if (playerStats.isLoaded() && playerStats.getPlayer().isOnline() && playerStats.getPracticeLocation() != null) {
                playerStats.getPlayer().teleport(playerStats.getPracticeLocation());
                playerStats.resetPracticeMode();
            }
        }
    }

    public static void resetPlayer(Player player, boolean message) {

        PlayerStats playerStats = Parkour.getStatsManager().get(player);

        player.teleport(playerStats.getPracticeLocation());
        playerStats.resetPracticeMode();

        ItemStack item = Utils.getPracPlateIfExists(player.getInventory());

        if (item != null)
            player.getInventory().remove(item);

        if (message)
            player.sendMessage(Utils.translate("&2You have disabled practice mode"));
    }
}