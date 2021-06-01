package com.parkourcraft.parkour.gameplay;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.entity.Player;

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

        if (message)
            player.sendMessage(Utils.translate("&2You have disabled practice mode"));
    }
}