package com.renatusnetwork.momentum.data.saves;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.levels.Level;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.storage.mysql.DatabaseManager;
import com.renatusnetwork.momentum.storage.mysql.DatabaseQueries;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.List;
import java.util.Map;

public class SavesDB
{
    public static void loadSaves(PlayerStats playerStats)
    {
        List<Map<String, String>> levelsResults = DatabaseQueries.getResults(
                 DatabaseManager.LEVEL_SAVES_TABLE,
                "*",
                "WHERE uuid=?", playerStats.getUUID()
        );

        for (Map<String, String> levelResult : levelsResults)
        {
            String worldName = levelResult.get("world");
            Level level = Momentum.getLevelManager().get(levelResult.get("level_name"));

            // x, y, z
            float x = Float.parseFloat(levelResult.get("x"));
            float y = Float.parseFloat(levelResult.get("y"));
            float z = Float.parseFloat(levelResult.get("z"));

            float yaw = Float.parseFloat(levelResult.get("yaw"));
            float pitch = Float.parseFloat(levelResult.get("pitch"));

            // add to hashmap
            playerStats.addSave(level, new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch));
        }
    }

    public static void addSave(String uuid, String levelName, Location location)
    {
        // add to async queue
        DatabaseQueries.runAsyncQuery("INSERT INTO " + DatabaseManager.LEVEL_SAVES_TABLE + " " +
                "(uuid, level_name, world, x, y, z, yaw, pitch)" +
                " VALUES (?,?,?,?,?,?,?,?)",
                uuid,
                levelName,
                location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch()
        );
    }

    public static void removeSave(String uuid, String levelName)
    {
        DatabaseQueries.runAsyncQuery("DELETE FROM " + DatabaseManager.LEVEL_SAVES_TABLE + " WHERE uuid=? AND level_name=?", uuid, levelName);
    }

    public static void removeSaveFromName(String playerName, String levelName)
    {
        DatabaseQueries.runAsyncQuery(
                "DELETE FROM " + DatabaseManager.LEVEL_SAVES_TABLE + " ls " +
                    "JOIN " + DatabaseManager.PLAYERS_TABLE + " p ON p.uuid=ls.uuid WHERE p.name=? AND ls.level_name=?",
                    playerName, levelName);
    }

    public static void updateSave(String uuid, String levelName, Location newLocation)
    {
        DatabaseQueries.runAsyncQuery(
                "UPDATE " + DatabaseManager.LEVEL_SAVES_TABLE +
                     " SET world=?, " +
                     "x=?, y=?, z=?, " +
                     "yaw=?, pitch=? " +
                     "WHERE uuid=? AND level_name=?",
                newLocation.getWorld().getName(),
                newLocation.getX(),
                newLocation.getY(),
                newLocation.getZ(),
                newLocation.getYaw(),
                newLocation.getPitch(),
                uuid, levelName
        );
    }
}
