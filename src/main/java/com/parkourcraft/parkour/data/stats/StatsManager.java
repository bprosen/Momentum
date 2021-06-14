package com.parkourcraft.parkour.data.stats;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.levels.Level;
import com.parkourcraft.parkour.storage.mysql.DatabaseQueries;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
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
        new BukkitRunnable() {
            public void run() {
                loadUnloadedStats();
            }
        }.runTaskTimerAsynchronously(plugin, 10L, 4L);

        // Garbage collection for offline players
        new BukkitRunnable() {
            public void run() {
                clean();
            }
        }.runTaskTimer(plugin, 0L, 10L);

        // Leader Boards
        new BukkitRunnable() {
            public void run() {
                StatsDB.loadTotalCompletions();
                StatsDB.loadLeaderboards();
                Parkour.getLevelManager().loadGlobalLevelCompletionsLB(); // we MUST load this after leaderboards
                loadGlobalPersonalCompletionsLB();
            }
        }.runTaskAsynchronously(plugin);

        // run personal lb load every 3 mins in async
        new BukkitRunnable() {
            @Override
            public void run() {
                loadGlobalPersonalCompletionsLB();
            }
        }.runTaskTimerAsynchronously(plugin, 20 * 180, 20 * 180);
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

    public void loadGlobalPersonalCompletionsLB() {
        try {
            globalPersonalCompletionsLB.clear();

            // find the highest top 10 completion stat
            List<Map<String, String>> playerCompletions = DatabaseQueries.getResults("players", "player_name, level_completions",
                    " ORDER BY level_completions DESC LIMIT " + Parkour.getSettingsManager().max_global_personal_completions_leaderboard_size);

                for (Map<String, String> playerCompletionStat : playerCompletions)
                    // add playername to completion in map
                    globalPersonalCompletionsLB.put(playerCompletionStat.get("player_name"),
                                                    Integer.parseInt(playerCompletionStat.get("level_completions")));
        } catch (Exception e) {
            e.printStackTrace();
        }
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