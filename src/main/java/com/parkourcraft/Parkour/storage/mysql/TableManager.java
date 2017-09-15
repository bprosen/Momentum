package com.parkourcraft.Parkour.storage.mysql;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TableManager {

    public static void setUp() {
        List<String> tableNames = getTables();

        if (!tableNames.contains("completions"))
            createCompletions();

        if (!tableNames.contains("players"))
            createPlayers();
    }

    private static void createCompletions() {
        String sqlQuery = "CREATE TABLE completions(" +
                "uuid CHAR(36) NOT NULL, " +
                "level_name VARCHAR(25) NOT NULL, " +
                "completion_time MEDIUMINT NOT NULL, " +
                "date TIMESTAMP NOT NULL" +
                ")";

        DatabaseManager.addUpdateQuery(sqlQuery);
    }

    private static void createPlayers() {
        String sqlQuery = "CREATE TABLE players(" +
                "uuid CHAR(36) NOT NULL, " +
                "player_name VARCHAR(25) NOT NULL" +
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
