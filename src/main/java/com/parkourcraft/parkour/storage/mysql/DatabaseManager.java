package com.parkourcraft.parkour.storage.mysql;

import com.parkourcraft.parkour.Parkour;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class DatabaseManager {

    private DatabaseConnection connection;

    private boolean running = false;

    private List<String> cache = new ArrayList<>();

    public DatabaseManager(Plugin plugin) {
        connection = new DatabaseConnection();
        Tables_DB.configure(this);
        startScheduler(plugin);
    }

    public void close() {
        runCaches();
        connection.close();
    }

    private void startScheduler(Plugin plugin) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
            public void run() {
                runCaches();
            }
        }, 0L, 2L);
    }

    public DatabaseConnection get() {
        return connection;
    }

    private void runCaches() {
        if (running) // makes sure it isn't already running
            return;

        running = true; // ensures this method run the only one that can run

        try {

            if (cache.size() > 0)
                runCache();

        } catch (Exception exception) {
            Parkour.getPluginLogger().severe("ERROR: Occurred within DatabaseManager.runCaches()");
            Parkour.getPluginLogger().severe("ERROR:  printing StackTrace");
            exception.printStackTrace();
        }

        running = false; // allows the method to be ran again
    }

    private void runCache() {
        try {
            String finalQuery = "";

            List<String> tempCache = new ArrayList<>(cache);

            for (String sql : tempCache)
                finalQuery = finalQuery + sql + "; ";

            run(finalQuery);
            cache.removeAll(tempCache); // removes queries that have been ran
        } catch (Exception exception) {
            Parkour.getPluginLogger().severe("ERROR: Occurred within DatabaseManager.runCache()");
            Parkour.getPluginLogger().severe("ERROR:  printing StackTrace");
            exception.printStackTrace();
        }
    }

    public void add(String sql) {
        cache.add(sql);
    }

    public void run(String sql) {
        try {
            PreparedStatement statement = connection.get().prepareStatement(sql);
            statement.execute();
            statement.close();
        } catch (SQLException e) {
            Parkour.getPluginLogger().severe("ERROR: SQL Failed to run query: " + sql);
            e.printStackTrace();
        }
    }

}
