package com.parkourcraft.Parkour.data;

import com.parkourcraft.Parkour.Parkour;
import com.parkourcraft.Parkour.data.stats.PlayerStats;
import com.parkourcraft.Parkour.storage.mysql.DatabaseManager;
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

    public static List<PlayerStats> getPlayerStats() {
        return playerStatsList;
    }

    public static PlayerStats getByName(String playerName) {
        for (PlayerStats playerStats : playerStatsList)
            if (playerStats.getPlayerName().equals(playerName))
                return playerStats;

        return null;
    }

    public static PlayerStats getByNameIgnoreCase(String playerName) {
        for (PlayerStats playerStats : playerStatsList)
            if (playerStats.getPlayerName().equalsIgnoreCase(playerName))
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
            PlayerStats playerStats = new PlayerStats(player);

            playerStatsList.add(playerStats);
            DatabaseManager.addToLoadPlayersCache(playerStats);
        }
    }

    public static void remove(PlayerStats playerStats) {
        playerStatsList.remove(playerStats);
    }

    public static void clean() {
        List<PlayerStats> removeList = new ArrayList<>();

        for (PlayerStats playerStats : playerStatsList)
            if (!playerStats.getPlayer().isOnline())
                removeList.add(playerStats);

        for (PlayerStats playerStats : removeList)
            remove(playerStats);
    }

}
