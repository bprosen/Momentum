package com.parkourcraft.Parkour.storage.mysql;

import com.parkourcraft.Parkour.Parkour;
import com.parkourcraft.Parkour.data.PerkManager;
import com.parkourcraft.Parkour.data.StatsManager;
import com.parkourcraft.Parkour.data.stats.PlayerStats;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class DatabaseManager {

    private static boolean runningCaches = false;

    private static List<String> databaseQuriesCache = new ArrayList<>();
    private static List<PlayerStats> loadPlayersCache = new ArrayList<>();

    // order: runUpdateQueries, loadPlayersCache, updatePlayersCache
    public static void runCaches() {
        if (runningCaches) // makes sure it isn't already running
            return;
        runningCaches = true; // makes this method run the only one that can run

        try {

            if (databaseQuriesCache.size() > 0)
                runDatabaseQueriesCache();

            if (loadPlayersCache.size() > 0)
                runLoadPlayersCache();

        } catch (Exception exception) {
            Parkour.getPluginLogger().severe("ERROR: Occurred within DatabaseManager.runCaches()");
            Parkour.getPluginLogger().severe("ERROR:  printing StackTrace");
            exception.printStackTrace();
        }

        runningCaches = false; // allows the method to be ran again
    }

    private static void runDatabaseQueriesCache() {
        try {
            String finalQuery = "";

            List<String> cache = new ArrayList<>(databaseQuriesCache);

            for (String sql : cache)
                finalQuery = finalQuery + sql + "; ";

            runQuery(finalQuery);
            databaseQuriesCache.removeAll(cache); // removes queries that have been ran
        } catch (Exception exception) {
            Parkour.getPluginLogger().severe("ERROR: Occurred within DatabaseManager.runDatabaseQueriesCache()");
            Parkour.getPluginLogger().severe("ERROR:  printing StackTrace");
            exception.printStackTrace();
        }
    }

    private static void runLoadPlayersCache() {
        try {
            List<PlayerStats> cache = new ArrayList<>(loadPlayersCache);

            for (PlayerStats playerStats : cache) {
                if (playerStats != null
                        && playerStats.getPlayer() != null
                        && playerStats.getPlayer().isOnline()) {
                    DataQueries.loadPlayerStats(playerStats);
                    Parkour.perks.syncPermissions(playerStats.getPlayer());
                }

                loadPlayersCache.remove(playerStats);
            }
        } catch (Exception exception) {
            Parkour.getPluginLogger().severe("ERROR: Occurred within DatabaseManager.runLoadPlayersCache()");
            Parkour.getPluginLogger().severe("ERROR:  printing StackTrace");
            exception.printStackTrace();
        }
    }

    /* LoadPlayersCache
    This cache is responsible for loading information for players who have just joined
    */
    public static void addToLoadPlayersCache(PlayerStats playerStats) {
        loadPlayersCache.add(playerStats);
    }

    /* DatabaseQueriesCache
    This cache is responsible for running all queries
    */
    public static void addUpdateQuery(String sql) {
        databaseQuriesCache.add(sql);
    }

    public static void runQuery(String sql) {
        try {
            PreparedStatement statement = DatabaseConnection.get().prepareStatement(sql);
            statement.execute();
            statement.close();
        } catch (SQLException e) {
            Parkour.getPluginLogger().severe("ERROR: SQL Failed to runQuery: " + sql);
            e.printStackTrace();
        }
    }

}
