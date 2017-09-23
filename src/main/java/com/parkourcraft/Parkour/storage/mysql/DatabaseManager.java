package com.parkourcraft.Parkour.storage.mysql;

import com.parkourcraft.Parkour.Parkour;
import com.parkourcraft.Parkour.data.StatsManager;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class DatabaseManager {

    private static boolean runningCaches = false;

    private static List<String> databaseQuriesCache = new ArrayList<>();
    private static Map<String, String> loadPlayersCache = new HashMap<>();
    private static List<String> updatePlayersCache = new ArrayList<>();

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

            if (updatePlayersCache.size() > 0)
                runUpdatePlayersCache();

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

            List<String> cache = databaseQuriesCache;

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
            Map<String, String> cache = loadPlayersCache;

            for (String UUID : cache.keySet()) {
                StatsManager.addPlayer(UUID, cache.get(UUID));
                loadPlayersCache.remove(UUID);
            }
        } catch (Exception exception) {
            Parkour.getPluginLogger().severe("ERROR: Occurred within DatabaseManager.runLoadPlayersCache()");
            Parkour.getPluginLogger().severe("ERROR:  printing StackTrace");
            exception.printStackTrace();
        }
    }

    private static void runUpdatePlayersCache() {
        try {
            List<String> cache = updatePlayersCache;

            for (String UUID : cache)
                StatsManager.updatePlayerInDatabase(UUID);

            updatePlayersCache.removeAll(cache); // removes all players that were updates
        } catch (Exception exception) {
            Parkour.getPluginLogger().severe("ERROR: Occurred within DatabaseManager.runUpdatePlayersCache()");
            Parkour.getPluginLogger().severe("ERROR:  printing StackTrace");
            exception.printStackTrace();
        }
    }

    /* LoadPlayersCache
    This cache is responsible for loading information for players who have just joined
    */
    public static void addToLoadPlayersCache(String UUID, String playerName) {
        loadPlayersCache.put(UUID, playerName);
    }

    /* UpdatePlayersCache
    This cache is responsible for updating a player's information in the database
    */
    public static void addToUpdatePlayersCache(String UUID) {
        if (!updatePlayersCache.contains(UUID))
            updatePlayersCache.add(UUID);
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
