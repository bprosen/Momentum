package com.parkourcraft.parkour.data.stats;

import com.parkourcraft.parkour.Parkour;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class StatsManager {

    private boolean running = false;
    private HashMap<String, PlayerStats> playerStatsList = new HashMap<>();

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
        for (Map.Entry<String, PlayerStats> entry : playerStatsList.entrySet()) {
            PlayerStats playerStats = entry.getValue();

            if (playerStats.getUUID().equals(UUID))
                return playerStats;
        }

        return null;
    }

    public PlayerStats get(int playerID) {
        for (Map.Entry<String, PlayerStats> entry : playerStatsList.entrySet())
            if (entry.getValue().getPlayerID() == playerID)
                return entry.getValue();

        return null;
    }

    public HashMap<String, PlayerStats> getPlayerStats() {
        return playerStatsList;
    }

    public PlayerStats getByNameIgnoreCase(String playerName) {
        return playerStatsList.get(playerName);
    }

    public PlayerStats get(Player player) {
        return playerStatsList.get(player.getName());
    }

    public boolean exists(String playerName) {
        return getByNameIgnoreCase(playerName) != null;
    }

    public void add(Player player) {
        if (!exists(player.getUniqueId().toString())) {
            PlayerStats playerStats = new PlayerStats(player);
            playerStatsList.put(player.getName(), playerStats);
        }
    }

    private void loadUnloadedStats() {
        if (!running) {
            for (Map.Entry<String, PlayerStats> entry : playerStatsList.entrySet()) {
                if (entry.getValue().getPlayerID() == -1) {
                    StatsDB.loadPlayerStats(entry.getValue());
                    Parkour.getPerkManager().syncPermissions(entry.getValue().getPlayer());
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

        for (Map.Entry<String, PlayerStats> entry : playerStatsList.entrySet())
            if (!entry.getValue().getPlayer().isOnline())
                removeList.add(entry.getValue());

        for (PlayerStats playerStats : removeList)
            remove(playerStats);
    }
}