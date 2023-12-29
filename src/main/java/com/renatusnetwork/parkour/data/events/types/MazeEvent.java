package com.renatusnetwork.parkour.data.events.types;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.levels.Level;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MazeEvent extends Event
{

    private List<Location> respawnLocs;
    private Location exit;

    public MazeEvent(Level level)
    {
        super(level, "Maze");

        // load cache
        this.respawnLocs = Parkour.getLocationManager().getMazeLocations(level.getName(), true, false);
        List<Location> exitLocations = Parkour.getLocationManager().getMazeLocations(level.getName(), false, true);

        this.exit = exitLocations.get(ThreadLocalRandom.current().nextInt(exitLocations.size()));
        exit.getBlock().setType(Material.IRON_PLATE);
    }

    public void respawn(Player player)
    {
        player.teleport(respawnLocs.get(ThreadLocalRandom.current().nextInt(respawnLocs.size())));
    }

    @Override
    public void end()
    {
        exit.getBlock().setType(Material.AIR); // remove the plate
    }
}
