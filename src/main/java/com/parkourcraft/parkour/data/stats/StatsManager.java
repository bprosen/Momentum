package com.parkourcraft.parkour.data.stats;

import com.parkourcraft.parkour.Parkour;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class StatsManager {

    private boolean running = false;
    private Set<PlayerStats> playerStatsList = new HashSet<>();

    public StatsManager(Plugin plugin) {
        startScheduler(plugin);
    }

    private void startScheduler(Plugin plugin) {
        // Loads unloaded PlayersStats
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
            public void run() {
                loadUnloadedStats();
            }
        }, 10L, 4L);

        // Garbage collection for offline players
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            public void run() {
                clean();
            }
        }, 0L, 10L);

        // Leader Boards
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            public void run() {
                StatsDB.loadTotalCompletions();
                StatsDB.loadLeaderboards();
            }
        });
    }

    public PlayerStats get(String UUID) {
        for (PlayerStats playerStats : playerStatsList)
            if (playerStats.getUUID().equals(UUID))
                return playerStats;

        return null;
    }

    public PlayerStats get(int playerID) {
        for (PlayerStats playerStats : playerStatsList)
            if (playerStats.getPlayerID() == playerID)
                return playerStats;

        return null;
    }

    public Set<PlayerStats> getPlayerStats() {
        return playerStatsList;
    }

    public PlayerStats getByName(String playerName) {
        for (PlayerStats playerStats : playerStatsList)
            if (playerStats.getPlayerName().equals(playerName))
                return playerStats;

        return null;
    }

    public PlayerStats getByNameIgnoreCase(String playerName) {
        for (PlayerStats playerStats : playerStatsList)
            if (playerStats.getPlayerName().equalsIgnoreCase(playerName))
                return playerStats;

        return null;
    }

    public PlayerStats get(Player player) {
        return get(player.getUniqueId().toString());
    }

    public boolean exists(String UUID) {
        return get(UUID) != null;
    }

    public void add(Player player) {
        if (!exists(player.getUniqueId().toString())) {
            PlayerStats playerStats = new PlayerStats(player);
            playerStatsList.add(playerStats);
        }
    }

    private void loadUnloadedStats() {
        if (!running) {
            for (PlayerStats playerStats : playerStatsList) {
                if (playerStats.getPlayerID() == -1) {
                    StatsDB.loadPlayerStats(playerStats);
                    Parkour.getPerkManager().syncPermissions(playerStats.getPlayer());
                }
            }
            running = false;
        }
    }

    public void addUnloadedPlayers() {
        for (Player player : Bukkit.getOnlinePlayers())
            if (!exists(player.getUniqueId().toString()))
                add(player);
    }

    public void remove(PlayerStats playerStats) {
        playerStatsList.remove(playerStats);
    }

    public void clean() {

        if (playerStatsList.isEmpty())
            return;

        Set<PlayerStats> removeList = new HashSet<>();

        for (PlayerStats playerStats : playerStatsList)
            if (!playerStats.getPlayer().isOnline())
                removeList.add(playerStats);

        for (PlayerStats playerStats : removeList)
            remove(playerStats);
    }

}
