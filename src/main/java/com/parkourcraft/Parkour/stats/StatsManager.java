package com.parkourcraft.Parkour.stats;


import com.parkourcraft.Parkour.stats.objects.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class StatsManager {

    private static Map<String, PlayerStats> playerStatsMap = new HashMap<>();

    public static void addPlayer(String UUID, String playerName) {
        if (!playerStatsMap.containsKey(UUID))
            playerStatsMap.put(UUID, new PlayerStats(UUID, playerName));
    }

    public static PlayerStats getPlayerStatsByPlayerName(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null)
            return null;
        return getPlayerStats(player);
    }

    public static PlayerStats getPlayerStats(String UUID) {
        return playerStatsMap.get(UUID);
    }

    public static PlayerStats getPlayerStats(Player player) {
        return playerStatsMap.get(player.getUniqueId().toString());
    }

    public static void updateOnlinePlayersInDatabase() {
        Iterator<Map.Entry<String, PlayerStats>> iterator = playerStatsMap.entrySet().iterator();

        while(iterator.hasNext()) {
            Map.Entry<String, PlayerStats> entry = iterator.next();

            entry.getValue().updateIntoDatabase();

            if (Bukkit.getPlayer(UUID.fromString(entry.getValue().getUUID())) == null)
                iterator.remove();
        }
    }

    public static void updatePlayerInDatabase(String UUID) {
        if (playerStatsMap.get(UUID) != null)
            playerStatsMap.get(UUID).updateIntoDatabase();
    }

}
