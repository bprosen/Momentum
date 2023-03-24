package com.renatusnetwork.parkour.storage.mysql;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TablesDB {

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

        if (!tableNames.contains("checkpoints"))
            createCheckpoints(database);

        if (!tableNames.contains("plots"))
            createPlots(database);

        if (!tableNames.contains("ratings"))
            createRatings(database);

        if (!tableNames.contains("bought_levels"))
            createPurchasedLevels(database);

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
                "coins DOUBLE DEFAULT 0, " +
                "spectatable BIT DEFAULT 1, " +
                "clan_id INT DEFAULT -1, " +
                "rank_id INT DEFAULT 1, " +
                "rankup_stage BIT default 0, " +
                "rank_prestiges TINYINT default 0, " +
                "infinitepk_score INT default 0, " +
                "level_completions INT default 0, " +
                "race_wins SMALLINT default 0, " +
                "race_losses SMALLINT default 0, " +
                "night_vision BIT DEFAULT 0, " +
                "grinding BIT DEFAULT 0, " +
                "records SMALLINT default 0, " +
                "event_wins MEDIUMINT default 0, " +
                "infinite_block VARCHAR(30) DEFAULT '' NOT NULL, " +
                "fail_mode BIT DEFAULT 1, " +
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
                "clan_level SMALLINT NOT NULL, " +
                "clan_xp INT NOT NULL, " +
                "total_gained_xp BIGINT NOT NULL, " +
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

    private static void createPlots(DatabaseManager database) {
        String sqlQuery = "CREATE TABLE plots(" +
                "plot_id INT NOT NULL AUTO_INCREMENT, " +
                "uuid char(36) NOT NULL, " +
                "player_name VARCHAR(16) NOT NULL, " +
                "trusted_players TEXT NOT NULL, " +
                "center_x INT NOT NULL, " +
                "center_z INT NOT NULL,  " +
                "submitted VARCHAR(5) NOT NULL, " +
                "PRIMARY KEY (plot_id)" +
                ")";
        database.run(sqlQuery);
    }

    private static void createCheckpoints(DatabaseManager database) {
        String sqlQuery = "CREATE TABLE checkpoints(" +
                "uuid CHAR(36) NOT NULL, " +
                "player_name VARCHAR(16) NOT NULL, " +
                "level_name VARCHAR(30) NOT NULL, " +
                "world VARCHAR(15) NOT NULL, " +
                "x INT NOT NULL, " +
                "y INT NOT NULL, " +
                "z INT NOT NULL" +
                ")";

        database.run(sqlQuery);
    }

    private static void createRatings(DatabaseManager database) {
        String sqlQuery = "CREATE TABLE ratings(" +
                "uuid CHAR(36) NOT NULL, " +
                "player_name VARCHAR(16) NOT NULL, " +
                "level_id INT NOT NULL, " +
                "rating TINYINT NOT NULL" +
                ")";

        database.run(sqlQuery);
    }

    private static void createPurchasedLevels(DatabaseManager database)
    {
        String sqlQuery = "CREATE TABLE bought_levels(" +
                "uuid CHAR(36) NOT NUll, " +
                "player_name VARCHAR(16) NOT NULL, " +
                "level_name VARCHAR(30) NOT NULL)";

        database.run(sqlQuery);
    }
}
