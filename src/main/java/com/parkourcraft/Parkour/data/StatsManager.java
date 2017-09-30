package com.parkourcraft.Parkour.data;

import com.parkourcraft.Parkour.data.stats.PlayerStats;
import com.parkourcraft.Parkour.storage.mysql.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class StatsManager {

    private static List<PlayerStats> playerStatsList = new ArrayList<>();

    public static PlayerStats get(String UUID) {
        for (PlayerStats playerStats : playerStatsList)
            if (playerStats.getUUID().equals(UUID))
                return playerStats;

        return null;
    }

    public static PlayerStats getByName(String playerName) {
        for (PlayerStats playerStats : playerStatsList)
            if (playerStats.getPlayerName().equals(playerName))
                return playerStats;

        return null;
    }

    public static PlayerStats get(Player player) {
        return get(player.getUniqueId().toString());
    }

    public static boolean exists(String UUID) {
        if (get(UUID) != null)
            return true;

        return false;
    }

    public static void add(Player player) {
        if (!exists(player.getUniqueId().toString())) {
            PlayerStats playerStats = new PlayerStats(
                    player.getUniqueId().toString(),
                    player.getName()
            );

            playerStatsList.add(playerStats);
            DatabaseManager.addToLoadPlayersCache(playerStats);
        }
    }

    public static void remove(String UUID) {
        for (Iterator<PlayerStats> iterator = playerStatsList.iterator(); iterator.hasNext();)
            if (iterator.next().getUUID().equals(UUID))
                iterator.remove();
    }

    public static void cleanPlayerStats() {
        List<String> removeList = new ArrayList<>();

        for (PlayerStats playerStats : playerStatsList)
            if (Bukkit.getPlayer(UUID.fromString(playerStats.getUUID())) == null)
                removeList.add(playerStats.getUUID());

        for (String UUID : removeList)
            remove(UUID);
    }

}
