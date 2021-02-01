package com.parkourcraft.parkour.data.checkpoints;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.storage.mysql.DatabaseQueries;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Checkpoint_DB {

    public static void loadPlayer(UUID uuid) {

        List<Map<String, String>> levelsResults = DatabaseQueries.getResults(
                "checkpoints",
                "*",
                "WHERE UUID='" + uuid.toString() + "'"
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
            Parkour.getCheckpointManager().addPlayer(playerName, new Location(Bukkit.getWorld(worldName), x, y, z));
            Parkour.getDatabaseManager().add("DELETE FROM checkpoints WHERE UUID='" + uuid.toString() + "'");
        }
    }


    public static void savePlayer(Player player) {

        Location loc = Parkour.getCheckpointManager().get(player);

        Parkour.getDatabaseManager().run("INSERT INTO checkpoints " +
                "(uuid, player_name, world, x, y, z)" +
                " VALUES ('" +
                player.getUniqueId().toString() + "','" +
                player.getName() + "','" +
                loc.getWorld().getName() + "','" +
                loc.getBlockX() + "','" +
                loc.getBlockY() + "','" +
                loc.getBlockZ() +
                "')"
        );
    }
    public static void savePlayerAsync(Player player) {

        Location loc = Parkour.getCheckpointManager().get(player);

        Parkour.getDatabaseManager().add("INSERT INTO checkpoints " +
                "(uuid, player_name, world, x, y, z)" +
                " VALUES ('" +
                player.getUniqueId().toString() + "','" +
                player.getName() + "','" +
                loc.getWorld().getName() + "','" +
                loc.getBlockX() + "','" +
                loc.getBlockY() + "','" +
                loc.getBlockZ() +
                "')"
        );

        Parkour.getCheckpointManager().removePlayer(player);
    }

    public static void saveAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (Parkour.getCheckpointManager().contains(player))
                savePlayer(player);
        }
    }

    public static boolean hasCheckpoint(UUID uuid) {

        List<Map<String, String>> levelsResults = DatabaseQueries.getResults(
                "checkpoints",
                "*",
                "WHERE UUID='" + uuid.toString() + "'"
        );

        if (!levelsResults.isEmpty())
            return true;
        return false;
    }
}
