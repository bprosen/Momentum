package com.renatusnetwork.parkour.data.saves;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.storage.mysql.DatabaseQueries;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.List;
import java.util.Map;

public class SavesDB
{
    public static void loadSaves(PlayerStats playerStats)
    {
        List<Map<String, String>> levelsResults = DatabaseQueries.getResults(
                "saves",
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
                float x = Float.parseFloat(levelResult.get("x"));
                float y = Float.parseFloat(levelResult.get("y"));
                float z = Float.parseFloat(levelResult.get("z"));

                float yaw = Float.parseFloat(levelResult.get("yaw"));
                float pitch = Float.parseFloat(levelResult.get("pitch"));

                // add to hashmap
                if (playerName != null && worldName != null && level != null)
                    playerStats.addSave(level.getName(), new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch));
            }
        }
    }

    public static void addSave(PlayerStats playerStats, String levelName, Location location)
    {
        // add to async queue
        DatabaseQueries.runAsyncQuery("INSERT INTO saves " +
                "(uuid, player_name, level_name, world, x, y, z, yaw, pitch)" +
                " VALUES ('" +
                playerStats.getUUID() + "','" +
                playerStats.getPlayerName() + "','" +
                levelName + "','" +
                location.getWorld().getName() + "','" +
                location.getX() + "','" +
                location.getY() + "','" +
                location.getZ() + "','" +
                location.getYaw() + "','" +
                location.getPitch() +
                "')"
        );
    }

    public static void removeSave(PlayerStats playerStats, String levelName)
    {
        DatabaseQueries.runAsyncQuery("DELETE FROM saves WHERE uuid='" + playerStats.getUUID() + "' AND level_name='" + levelName + "'");
    }
}
