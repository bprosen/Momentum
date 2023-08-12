package com.renatusnetwork.parkour.data.events.types;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.levels.LevelsYAML;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MazeEvent extends Event
{

    private List<Location> respawnLocs;
    private Location exit;

    public MazeEvent(Level level)
    {
        super(level, "Maze");

        load();
    }

    private void load()
    {
        // load cache
        respawnLocs = LevelsYAML.getMazeRespawns(getLevel().getName());
        exit = LevelsYAML.getRandomMazeEventExit(getLevel().getName());
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
