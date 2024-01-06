package com.renatusnetwork.parkour.data.checkpoints;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.storage.mysql.DatabaseManager;
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
                DatabaseManager.LEVEL_CHECKPOINTS_TABLE,
                "*",
                "WHERE uuid=?", playerStats.getUUID()
        );

        // if they have a checkpoint, load it
        for (Map<String, String> levelResult : levelsResults)
        {
            // get player name, world name and level
            String worldName = levelResult.get("world");
            Level level = Parkour.getLevelManager().get(levelResult.get("level_name"));


            if (level != null)
            {
                // x, y, z
                int x = Integer.parseInt(levelResult.get("x"));
                int y = Integer.parseInt(levelResult.get("y"));
                int z = Integer.parseInt(levelResult.get("z"));

                // add to hashmap
                playerStats.addCheckpoint(level, new Location(Bukkit.getWorld(worldName), x, y, z));
            }
        }
    }

    public static void deleteCheckpointFromName(String playerName, String levelName)
    {
        DatabaseQueries.runAsyncQuery(
                "DELETE lc FROM " + DatabaseManager.LEVEL_CHECKPOINTS_TABLE +
                    " lc JOIN " + DatabaseManager.PLAYERS_TABLE + " p ON lc.uuid=p.uuid WHERE p.name=? AND lc.level_name=?",
                    playerName,
                    levelName
        );
    }
}
