package com.renatusnetwork.momentum.storage.mysql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.renatusnetwork.momentum.Momentum;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;

public class DatabaseConnection {

    private HikariDataSource dataSource;

    public DatabaseConnection() {
        open();
    }

    private void open()
    {
        FileConfiguration settings = Momentum.getConfigManager().get("config");
        String dbPath = "database";

        String username = settings.getString(dbPath + ".username");
        String password = settings.getString(dbPath + ".password");

        String host = settings.getString(dbPath + ".host");
        String database = settings.getString(dbPath + ".database");
        String port = settings.getString(dbPath + ".port");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false";

        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);

        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
        hikariConfig.addDataSourceProperty("useLocalSessionState", "true");
        hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
        hikariConfig.addDataSourceProperty("cacheResultSetMetadata", "true");
        hikariConfig.addDataSourceProperty("cacheServerConfiguration", "true");
        hikariConfig.addDataSourceProperty("maintainTimeStats", "true");
        hikariConfig.setMaximumPoolSize(10);

        dataSource = new HikariDataSource(hikariConfig);

        Momentum.getPluginLogger().info("Successfully opened the HikariCP connection pool to the database");
    }

    public void close()
    {
        if (dataSource != null && !dataSource.isClosed())
            dataSource.close();
    }

    public Connection get() throws SQLException
    {
        return dataSource.getConnection();
    }
}
