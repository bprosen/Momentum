package com.parkourcraft.Parkour.storage.mysql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.parkourcraft.Parkour.Parkour;
import org.bukkit.configuration.file.FileConfiguration;

import com.parkourcraft.Parkour.storage.local.FileManager;

public class DatabaseConnection {

    private static Connection connection;

    public static void open() {
        FileConfiguration settings = FileManager.getFileConfig("settings");
        String dbPath = "database";

        String username = settings.getString(dbPath + ".username");
        String password = settings.getString(dbPath + ".password");

        String host = settings.getString(dbPath + ".host");
        String database = settings.getString(dbPath + ".database");
        String port = settings.getString(dbPath + ".port");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true"  + "&allowMultiQueries=true";

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

        Parkour.getPluginLogger().info("Successfully connected to mySQL database: " + host);
    }

    public static void close() {
        try {
            if (!connection.isClosed())
                connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection get() {
        return connection;
    }

    public static DatabaseMetaData getMeta() {
        DatabaseMetaData meta = null;
        try {
            meta = get().getMetaData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return meta;
    }

}
