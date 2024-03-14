package com.renatusnetwork.momentum.storage.mysql;

import com.renatusnetwork.momentum.Momentum;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseManager {

    private DatabaseConnection connection;

    public DatabaseManager(Plugin plugin) {
        connection = new DatabaseConnection();
        TablesDB.configure(this);
        startScheduler(plugin);
    }

    public void close() {
        connection.close();
    }

    private void startScheduler(Plugin plugin) {

        // run async random queue every 10 minutes to keep connection alive if nobody is online and no database activity
        new BukkitRunnable() {
            public void run() {
                try {
                    PreparedStatement statement = connection.get().prepareStatement(
                            "SELECT * FROM checkpoints WHERE UUID='s'");
                    statement.execute();
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskTimerAsynchronously(plugin, 20 * 60 * 10, 20 * 60 * 10);
    }

    public DatabaseConnection get() {
        return connection;
    }

    public void runQuery(String sql, Object... parameters) {
        try {
            PreparedStatement statement = connection.get().prepareStatement(sql);

            // secure
            for (int i = 0; i < parameters.length; i++)
                statement.setObject(i + 1, parameters[i]); // who knows why it starts at 1

            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            Momentum.getPluginLogger().severe("ERROR: SQL Failed to run query: " + sql);
            e.printStackTrace();
        }
    }

    public void runAsyncQuery(String sql, Object... parameters) {
        new BukkitRunnable() {
            public void run() {
                runQuery(sql, parameters);
            }
        }.runTaskAsynchronously(Momentum.getPlugin());
    }
}
