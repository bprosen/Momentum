package com.parkourcraft.parkour.storage.mysql;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Tables_DB {

    public static void configure(DatabaseManager database) {
        List<String> tableNames = get(database.get());

        if (!tableNames.contains("players"))
            createPlayers(database);

        if (!tableNames.contains("levels"))
            createLevels(database);

        if (!tableNames.contains("perks"))
            createPerks(database);

        if (!tableNames.contains("clans"))
            createClans(database);

        if (!tableNames.contains("ledger"))
            createLedger(database);

        if (!tableNames.contains("completions"))
            createCompletions(database);
    }

    private static List<String> get(DatabaseConnection connection) {
        List<String> tableNames = new ArrayList<>();
        DatabaseMetaData meta = connection.getMeta();

        try {
            ResultSet rs = meta.getTables(null, null, "%", null);

            while (rs.next())
                tableNames.add(rs.getString(3));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tableNames;
    }

    private static void createPlayers(DatabaseManager database) {
        String sqlQuery = "CREATE TABLE players(" +
                "player_id INT NOT NULL AUTO_INCREMENT, " +
                "uuid CHAR(36) NOT NULL, " +
                "player_name VARCHAR(16) NOT NULL, " +
                "spectatable BIT DEFAULT 1, " +
                "clan_id INT DEFAULT -1," +
                "PRIMARY KEY (player_id)" +
                ")";

        database.run(sqlQuery);
    }

    private static void createLevels(DatabaseManager database) {
        String sqlQuery = "CREATE TABLE levels(" +
                "level_id INT NOT NULL AUTO_INCREMENT, " +
                "level_name VARCHAR(30) NOT NULL, " +
                "reward INT DEFAULT 0, " +
                "score_modifier INT DEFAULT 1, " +
                "PRIMARY KEY (level_id)" +
                ")";

        database.run(sqlQuery);
    }

    private static void createPerks(DatabaseManager database) {
        String sqlQuery = "CREATE TABLE perks(" +
                "perk_id INT NOT NULL AUTO_INCREMENT, " +
                "perk_name VARCHAR(30) NOT NULL, " +
                "PRIMARY KEY (perk_id)" +
                ")";

        database.run(sqlQuery);
    }

    private static void createClans(DatabaseManager database) {
        String sqlQuery = "CREATE TABLE clans(" +
                "clan_id INT NOT NULL AUTO_INCREMENT, " +
                "clan_tag VARCHAR(7) NOT NULL, " +
                "owner_player_id INT NOT NULL, " +
                "PRIMARY KEY (clan_id)" +
                ")";

        database.run(sqlQuery);
    }

    private static void createLedger(DatabaseManager database) {
        String sqlQuery = "CREATE TABLE ledger(" +
                "player_id INT NOT NULL, " +
                "perk_id INT NOT NULL, " +
                "date TIMESTAMP NOT NULL" +
                ")";

        database.run(sqlQuery);
    }

    private static void createCompletions(DatabaseManager database) {
        String sqlQuery = "CREATE TABLE completions(" +
                "player_id INT NOT NULL, " +
                "level_id INT NOT NULL, " +
                "time_taken MEDIUMINT DEFAULT 0, " +
                "completion_date TIMESTAMP NOT NULL" +
                ")";

        database.run(sqlQuery);
    }

}
