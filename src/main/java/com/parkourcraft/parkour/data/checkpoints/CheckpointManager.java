package com.parkourcraft.parkour.data.checkpoints;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class CheckpointManager {

    public void teleportPlayer(Player player) {

        PlayerStats playerStats = Parkour.getStatsManager().get(player);

        Location loc;
        if (playerStats.getPracticeLocation() != null)
            loc = playerStats.getPracticeLocation().clone();
        else
            loc = playerStats.getCheckpoint().clone();

        if (loc != null) {
            // check if there is a practice location,
            // if not then the loc is checkpoint and
            // adjust the x and z to teleport to middle
            if (playerStats.getPracticeLocation() == null)
                loc.add(0.5, 0, 0.5);

            loc.setPitch(player.getLocation().getPitch());
            loc.setYaw(player.getLocation().getYaw());
            player.teleport(loc);
        }
    }
}
