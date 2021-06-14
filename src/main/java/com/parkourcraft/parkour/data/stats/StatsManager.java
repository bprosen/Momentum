package com.parkourcraft.parkour.data.stats;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.levels.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class StatsManager {

    private boolean running = false;
    private HashMap<String, PlayerStats> playerStatsList = new HashMap<>();
    private LinkedHashMap<String, Integer> globalPersonalCompletionsLB = new LinkedHashMap<>
            (Parkour.getSettingsManager().max_global_personal_completions_leaderboard_size);

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
                Parkour.getLevelManager().loadGlobalLevelCompletionsLB(); // we MUST load this after leaderboards
            }
        });
    }

    public PlayerStats get(String UUID) {
        for (PlayerStats playerStats : playerStatsList.values())
            if (playerStats.getUUID().equals(UUID))
                return playerStats;

        return null;
    }

    public PlayerStats get(int playerID) {
        for (PlayerStats playerStats : playerStatsList.values())
            if (playerStats.getPlayerID() == playerID)
                return playerStats;

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
            for (PlayerStats playerStats : playerStatsList.values()) {
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

    public LinkedHashMap<String, Integer> getGlobalPersonalCompletionsLB() {
        return globalPersonalCompletionsLB;
    }

    public void clean() {

        if (playerStatsList.isEmpty())
            return;

        Set<PlayerStats> removeList = new HashSet<>();

        for (PlayerStats playerStats : playerStatsList.values())
            if (!playerStats.getPlayer().isOnline())
                removeList.add(playerStats);

        for (PlayerStats playerStats : removeList)
            remove(playerStats);
    }
}