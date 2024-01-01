package com.renatusnetwork.parkour.storage.mysql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.renatusnetwork.parkour.Parkour;
import org.bukkit.configuration.file.FileConfiguration;

public class DatabaseConnection {

    private Connection connection;

    public DatabaseConnection() {
        open();
    }

    private void open()
    {
        FileConfiguration settings = Parkour.getConfigManager().get("settings");
        String dbPath = "database";

        String username = settings.getString(dbPath + ".username");
        String password = settings.getString(dbPath + ".password");

        String host = settings.getString(dbPath + ".host");
        String database = settings.getString(dbPath + ".database");
        String port = settings.getString(dbPath + ".port");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true&allowMultiQueries=true&useSSL=false";

        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Parkour.getPluginLogger().info("Successfully opened the connection to the database");
    }

    public void close() {
        try {
            if (!connection.isClosed())
                connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection get() {
        return connection;
    }

    public DatabaseMetaData getMeta() {
        DatabaseMetaData meta = null;

        try {
            meta = get().getMetaData();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return meta;
    }

}
