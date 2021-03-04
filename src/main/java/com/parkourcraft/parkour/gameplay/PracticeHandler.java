package com.parkourcraft.parkour.gameplay;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.entity.Player;

public class PracticeHandler {

    public static void shutdown() {
        for (PlayerStats playerStats : Parkour.getStatsManager().getPlayerStats()) {

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