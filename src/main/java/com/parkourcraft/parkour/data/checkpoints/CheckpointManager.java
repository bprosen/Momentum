package com.parkourcraft.parkour.data.checkpoints;

import com.parkourcraft.parkour.Parkour;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class CheckpointManager {

    public void teleportPlayer(Player player) {

        Location loc = Parkour.getStatsManager().get(player).getCheckpoint().clone();

        if (loc != null) {
            loc.add(0.5, 0, 0.5);
            loc.setPitch(player.getLocation().getPitch());
            loc.setYaw(player.getLocation().getYaw());
            player.teleport(loc);
        }
    }
}
