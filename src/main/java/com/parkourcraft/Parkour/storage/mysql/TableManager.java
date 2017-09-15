package com.parkourcraft.Parkour.storage.mysql;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TableManager {

    public static void setUp() {
        List<String> tableNames = getTables();

        if (!tableNames.contains("players"))
            createPlayers();

        if (!tableNames.contains("levels"))
            createLevels();

        if (!tableNames.contains("completions"))
            createCompletions();
    }

    private static void createPlayers() {
        String sqlQuery = "CREATE TABLE players(" +
                "player_id INT NOT NULL AUTO_INCREMENT, " +
                "uuid CHAR(36) NOT NULL, " +
                "player_name VARCHAR(16) NOT NULL, " +
                "PRIMARY KEY (player_id)" +
                ")";

        DatabaseManager.addUpdateQuery(sqlQuery);
    }

    private static void createLevels() {
        String sqlQuery = "CREATE TABLE levels(" +
                "level_id INT NOT NULL AUTO_INCREMENT, " +
                "level_name VARCHAR(30) NOT NULL, " +
                "PRIMARY KEY (level_id)" +
                ")";

        DatabaseManager.addUpdateQuery(sqlQuery);
    }

    private static void createCompletions() {
        String sqlQuery = "CREATE TABLE completions(" +
                "player_id INT NOT NULL, " +
                "level_id INT NOT NULL, " +
                "time_taken MEDIUMINT DEFAULT 0, " +
                "completion_date TIMESTAMP NOT NULL" +
                ")";

        DatabaseManager.addUpdateQuery(sqlQuery);
    }

    private static List<String> getTables() {
        List<String> tableNames = new ArrayList<>();
        DatabaseMetaData meta = DatabaseConnection.getMeta();

        try {
            ResultSet rs = meta.getTables(null, null, "%", null);

            while (rs.next())
                tableNames.add(rs.getString(3));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tableNames;
    }

}
