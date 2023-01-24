package com.renatusnetwork.parkour.data.checkpoints;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.storage.mysql.DatabaseQueries;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckpointDB {

    public static void loadCheckpoints(PlayerStats playerStats)
    {
        List<Map<String, String>> levelsResults = DatabaseQueries.getResults(
                "checkpoints",
                "*",
                "WHERE UUID='" + playerStats.getUUID() + "'"
        );

        // if they have a checkpoint, load it
        if (!levelsResults.isEmpty())
        {
            for (Map<String, String> levelResult : levelsResults)
            {
                // get player name, world name and level
                String playerName = levelResult.get("player_name");
                String worldName = levelResult.get("world");
                Level level = Parkour.getLevelManager().get(levelResult.get("level_name"));

                // x, y, z
                int x = Integer.parseInt(levelResult.get("x"));
                int y = Integer.parseInt(levelResult.get("y"));
                int z = Integer.parseInt(levelResult.get("z"));

                // add to hashmap
                if (playerName != null && worldName != null && level != null)
                    playerStats.addCheckpoint(level.getName(), new Location(Bukkit.getWorld(worldName), x, y, z));
            }
        }
    }
}
