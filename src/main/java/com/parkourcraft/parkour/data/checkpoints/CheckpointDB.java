package com.parkourcraft.parkour.data.checkpoints;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.storage.mysql.DatabaseQueries;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CheckpointDB {

    public static void loadPlayer(UUID uuid, String levelName) {

        List<Map<String, String>> levelsResults = DatabaseQueries.getResults(
                "checkpoints",
                "*",
                "WHERE UUID='" + uuid.toString() + "' " +
                "AND LEVEL_NAME='" + levelName + "'"
        );

        String playerName = null;
        String worldName = null;
        int x = 0;
        int y = 0;
        int z = 0;

        for (Map<String, String> levelResult : levelsResults) {
            playerName = levelResult.get("player_name");
            x = Integer.parseInt(levelResult.get("x"));
            y = Integer.parseInt(levelResult.get("y"));
            z = Integer.parseInt(levelResult.get("z"));
            worldName = levelResult.get("world");
        }

        if (playerName != null && worldName != null) {
            Parkour.getStatsManager().getByNameIgnoreCase(playerName).setCheckpoint(new Location(Bukkit.getWorld(worldName), x, y, z));
            Parkour.getDatabaseManager().add("DELETE FROM checkpoints WHERE UUID='" + uuid.toString() +
                                             "' AND LEVEL_NAME='" + levelName + "'");
        }
    }


    public static void savePlayer(Player player) {

        PlayerStats stats = Parkour.getStatsManager().get(player);

        if (stats.inLevel()) {
            Location loc = stats.getCheckpoint();

            Parkour.getDatabaseManager().run("INSERT INTO checkpoints " +
                    "(uuid, player_name, level_name, world, x, y, z)" +
                    " VALUES ('" +
                    player.getUniqueId().toString() + "','" +
                    player.getName() + "','" +
                    stats.getLevel().getName() + "','" +
                    loc.getWorld().getName() + "','" +
                    loc.getBlockX() + "','" +
                    loc.getBlockY() + "','" +
                    loc.getBlockZ() +
                    "')"
            );
        }
    }

    public static void savePlayerAsync(Player player) {

        PlayerStats stats = Parkour.getStatsManager().get(player);

        if (stats.inLevel()) {
            Location loc = stats.getCheckpoint();

            Parkour.getDatabaseManager().add("INSERT INTO checkpoints " +
                    "(uuid, player_name, level_name, world, x, y, z)" +
                    " VALUES ('" +
                    player.getUniqueId().toString() + "','" +
                    player.getName() + "','" +
                    stats.getLevel().getName() + "','" +
                    loc.getWorld().getName() + "','" +
                    loc.getBlockX() + "','" +
                    loc.getBlockY() + "','" +
                    loc.getBlockZ() +
                    "')"
            );
        }
    }

    public static void shutdown() {
        for (PlayerStats playerStats : Parkour.getStatsManager().getPlayerStats().values()) {
            if (playerStats.isLoaded() && playerStats.getPlayer().isOnline() && playerStats.getCheckpoint() != null)
                savePlayer(playerStats.getPlayer());
        }
    }

    public static boolean hasCheckpoint(UUID uuid, String levelName) {

        List<Map<String, String>> levelsResults = DatabaseQueries.getResults(
                "checkpoints",
                "*",
                "WHERE UUID='" + uuid.toString() + "' " +
                         "AND LEVEL_NAME='" + levelName + "'"
        );

        if (!levelsResults.isEmpty())
            return true;
        return false;
    }
}
