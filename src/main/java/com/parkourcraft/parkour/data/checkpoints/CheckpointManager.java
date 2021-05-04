package com.parkourcraft.parkour.data.checkpoints;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class CheckpointManager {

    public void teleportPlayer(Player player) {

        PlayerStats playerStats = Parkour.getStatsManager().get(player);

        if (!playerStats.inRace()) {

            Location loc = null;
            /*
             check if there is a practice location,
             if not then check if the loc is checkpoint and
             adjust the x and z to teleport to middle
             */
            if (playerStats.getPracticeLocation() != null)
                loc = playerStats.getPracticeLocation().clone();
            else if (playerStats.getCheckpoint() != null)
                loc = playerStats.getCheckpoint().clone().add(0.5, 0, 0.5);

            if (loc != null) {

                loc.setPitch(player.getLocation().getPitch());
                loc.setYaw(player.getLocation().getYaw());
                player.teleport(loc);
            } else {
                player.sendMessage(Utils.translate("&cYou do not have a saved checkpoint"));
            }
        } else {
            player.sendMessage(Utils.translate("&cYou cannot do this while in a race"));
        }
    }
}
