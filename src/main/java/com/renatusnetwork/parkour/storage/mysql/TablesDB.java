package com.renatusnetwork.parkour.storage.mysql;

public class TablesDB
{
    public static void initTables(DatabaseManager database)
    {
        // initialize all tables...
        createPlayers(database);
        createLevels(database);
        createPerks(database);
        createPlots(database);
        createLocations(database);
        createLevelRatings(database);
        createLevelCheckpoints(database);
        createLevelRecords(database);
        createLevelSaves(database);
        createLevelPurchases(database);
        createLevelSpawns(database);
        createLevelCompletions(database);
        createClans(database);
        createRanks(database);
        createPlotTrustedPlayers(database);
        createModifiers(database);
        createPlayerModifiers(database);
        createPerksOwned(database);
        createPerksLevelRequirements(database);
        createPerksArmor(database);
        createLevelCompletionCommands(database);
        createLevelPotionEffects(database);
        createLevelRequiredLevels(database);
        createBadges(database);
        createBadgesOwned(database);
        createBadgesCommands(database);
        createMasteryBadgeLevels(database);
    }

    private static void createPlayers(DatabaseManager databaseManager)
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.PLAYERS_TABLE + "(" +
                           "uuid CHAR(36) NOT NULL, " +
                           "name VARCHAR(16) NOT NULL, " +
                           "clan VARCHAR(10) DEFAULT NULL, " + // default not in a clan
                           "rank VARCHAR(10) DEFAULT NULL, " + // default set from settings
                           "prestiges TINYINT DEFAULT 0, " +
                           "coins DOUBLE DEFAULT 0, " +
                           "infinite_classic_score INT DEFAULT 0, " +
                           "infinite_speedrun_score INT DEFAULT 0, " +
                           "infinite_sprint_score INT DEFAULT 0, " +
                           "infinite_timed_score INT DEFAULT 0, " +
                           "infinite_block VARCHAR(30) DEFAULT NULL, " + // default set from settings
                           "infinite_type VARCHAR(10) DEFAULT NULL, " + // default set from settings
                           "race_wins SMALLINT DEFAULT 0, " +
                           "race_losses SMALLINT DEFAULT 0, " +
                           "event_wins MEDIUMINT DEFAULT 0, " +
                           // mode switches
                           "attempting_mastery BIT DEFAULT 0, " +
                           "attempting_rankup BIT DEFAULT 0, " +
                           "night_vision BIT DEFAULT 0, " +
                           "grinding BIT DEFAULT 0, " +
                           "spectatable BIT DEFAULT 1, " +
                           "fail_mode BIT DEFAULT 1, " +
                           // keys
                           "PRIMARY KEY(uuid), " +
                           "FOREIGN KEY(clan) REFERENCES " + DatabaseManager.CLANS_TABLE + "(tag) " +
                                "ON UPDATE CASCADE " +
                                "ON DELETE SET NULL, " +
                           "FOREIGN KEY(rank) REFERENCES " + DatabaseManager.RANKS_TABLE + "(name) " +
                                "ON UPDATE CASCADE " +
                                "ON DELETE SET NULL, " +
                           // indexes
                           "UNIQUE INDEX name_index(name), " +
                           // constraints
                           "CONSTRAINT non_negative CHECK (" +
                               "prestiges >= 0 AND " +
                               "coins >= 0.0 AND " +
                               "infinite_classic_score >= 0 AND " +
                               "infinite_speedrun_score >= 0 AND " +
                               "infinite_sprint_score >= 0 AND " +
                               "infinite_timed_score >= 0 AND " +
                               "level_completions >= 0 AND " +
                               "race_wins >= 0 AND " +
                               "race_losses >= 0 AND " +
                               "event_wins >= 0 AND" +
                           ")" +
                       ")";

        databaseManager.runQuery(query);
    }

    private static void createLevels(DatabaseManager databaseManager)
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.LEVELS_TABLE + "(" +
                            // basic info
                            "name VARCHAR(20) NOT NULL, " +
                            "reward INT DEFAULT 0, " +
                            "price INT DEFAULT 0, " +
                            "title VARCHAR(30) DEFAULT NULL, " + // this needs to be long to allow for storage of colors
                            // settings
                            "required_permission VARCHAR(20) DEFAULT NULL, " +
                            "required_rank VARCHAR(10) DEFAULT NULL, " +
                            "respawn_y SMALLINT DEFAULT NULL, " +
                            "max_completions SMALLINT DEFAULT NULL, " +
                            "type VARCHAR(30) DEFAULT NULL, " + // default set from settings
                            "difficulty TINYINT DEFAULT NULL, " +
                            // switches
                            "cooldown BIT DEFAULT 0, " +
                            "broadcast BIT DEFAULT 0, " +
                            "liquid_reset BIT DEFAULT 0, " +
                            "new BIT DEFAULT 0, " +
                            "has_mastery BIT DEFAULT 0, " +
                            // keys
                            "PRIMARY KEY(name), " +
                            "FOREIGN KEY(start_location) REFERENCES " + DatabaseManager.LOCATIONS_TABLE + "(name) " +
                                "ON UPDATE CASCADE " +
                                "ON DELETE SET NULL, " +
                            "FOREIGN KEY(completion_locations) REFERENCES " + DatabaseManager.LOCATIONS_TABLE + "(name) " +
                                "ON UPDATE CASCADE " +
                                "ON DELETE SET NULL, " +
                            "FOREIGN KEY(required_rank) REFERENCES " + DatabaseManager.RANKS_TABLE + "(name)" +
                                "ON UPDATE CASCADE " +
                                "ON DELETE SET NULL, " +
                            // indexes
                            "INDEX type_index(type), " +
                            // constraints
                            "CONSTRAINT non_negative CHECK (" +
                                    "reward >= 0 AND " +
                                    "price >= 0" +
                            ")" +
                        ")";

        databaseManager.runQuery(query);
    }

    private static void createPerks(DatabaseManager databaseManager)
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.PERKS_TABLE + "(" +
                            "name VARCHAR(20) NOT NULL, " +
                            "title VARCHAR(30) DEFAULT NULL, " + // this needs to be long to allow for storage of colors
                            // settings
                            "price INT DEFAULT 0, " +
                            "required_permission VARCHAR(20) DEFAULT NULL, " +
                            // keys
                            "PRIMARY KEY(name), " +
                            // constraints
                            "CONSTRAINT non_negative CHECK (" +
                                "price >= 0" +
                            ")" +
                        ")";

        databaseManager.runQuery(query);
    }

    private static void createPlots(DatabaseManager databaseManager)
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.PLOTS_TABLE + "(" +
                            "id INT NOT NULL AUTO_INCREMENT, " +
                            "owner_uuid CHAR(36) NOT NULL, " +
                            "center_x INT NOT NULL, " +
                            "center_z INT NOT NULL, " +
                            "submitted BIT DEFAULT 0, " +
                            // keys
                            "PRIMARY KEY(plot_id), " +
                            "FOREIGN KEY(owner_uuid) REFERENCES " + DatabaseManager.PLAYERS_TABLE + "(uuid) " +
                                "ON DELETE CASCADE" + // we want to delete their plot if the player is deleted from the db
                        ")";

        databaseManager.runQuery(query);
    }

    private static void createLocations(DatabaseManager databaseManager)
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.LOCATIONS_TABLE + "(" +
                            "name VARCHAR(30) NOT NULL, " +
                            "world VARCHAR(30) NOT NULL, " +
                            "x DOUBLE NOT NULL, " +
                            "y DOUBLE NOT NULL, " +
                            "z DOUBLE NOT NULL, " +
                            "yaw DOUBLE NOT NULL, " +
                            "pitch DOUBLE NOT NULL, " +
                            "PRIMARY KEY(name)" +
                        ")";

        databaseManager.runQuery(query);
    }

    private static void createLevelRatings(DatabaseManager databaseManager)
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.LEVEL_RATINGS_TABLE + "(" +
                            "uuid CHAR(36) NOT NULL, " +
                            "level_name VARCHAR(20) NOT NULL, " +
                            "rating TINYINT DEFAULT NULL, " +
                            // keys
                            "PRIMARY KEY(uuid, level_name), " +
                            "FOREIGN KEY(uuid) REFERENCES " + DatabaseManager.PLAYERS_TABLE + "(uuid) " +
                                "ON UPDATE CASCADE " +
                                "ON DELETE CASCADE, " +
                            "FOREIGN KEY(level_name) REFERENCES " + DatabaseManager.LEVELS_TABLE + "(name) " +
                                "ON UPDATE CASCADE " +
                                "ON DELETE CASCADE, " +
                            // indexes
                            "INDEX uuid_index(uuid), " +
                            // constraints
                            "CONSTRAINT rating_in_bounds CHECK (" +
                                "ratings >= 0 AND rating <= 5" +
                            ")" +
                        ")";

        databaseManager.runQuery(query);
    }

    private static void createLevelCheckpoints(DatabaseManager databaseManager)
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.LEVEL_CHECKPOINTS_TABLE + "(" +
                            "uuid CHAR(36) NOT NULL, " +
                            "level_name VARCHAR(20) NOT NULL, " +
                            "world VARCHAR(30) NOT NULL, " +
                            "x DOUBLE NOT NULL, " +
                            "y DOUBLE NOT NULL, " +
                            "z DOUBLE NOT NULL, " +
                            // keys
                            "PRIMARY KEY(uuid, level_name), " +
                            "FOREIGN KEY(uuid) REFERENCES " + DatabaseManager.PLAYERS_TABLE + "(uuid) " +
                                "ON UPDATE CASCADE " +
                                "ON DELETE CASCADE, " +
                            "FOREIGN KEY(level_name) REFERENCES " + DatabaseManager.LEVELS_TABLE + "(name) " +
                                "ON UPDATE CASCADE " +
                                "ON DELETE CASCADE, " +
                            // indexes
                            "INDEX uuid_index(uuid)" +
                        ")";

        databaseManager.runQuery(query);
    }

    private static void createLevelRecords(DatabaseManager databaseManager)
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.LEVEL_RECORDS_TABLE + "(" +
                            "level_name VARCHAR(20) NOT NULL, " +
                            "uuid CHAR(36) NOT NULL, " +
                            // keys
                            "PRIMARY KEY(level_name), " +
                            "FOREIGN KEY(level_name) REFERENCES " + DatabaseManager.LEVELS_TABLE + "(name) " +
                                "ON UPDATE CASCADE " +
                                "ON DELETE CASCADE, " +
                            "FOREIGN KEY(uuid) REFERENCES " + DatabaseManager.PLAYERS_TABLE + "(uuid) " +
                                "ON UPDATE CASCADE " +
                                "ON DELETE CASCADE, " +
                            // indexes
                            "INDEX uuid_index(uuid)" +
                        ")";

        databaseManager.runQuery(query);
    }

    private static void createLevelSaves(DatabaseManager databaseManager)
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.LEVEL_SAVES_TABLE + "(" +
                            "uuid CHAR(36) NOT NULL, " +
                            "level_name VARCHAR(20) NOT NULL, " +
                            "world VARCHAR(30) NOT NULL, " +
                            "x DOUBLE NOT NULL, " +
                            "y DOUBLE NOT NULL, " +
                            "z DOUBLE NOT NULL, " +
                            "yaw DOUBLE NOT NULL, " +
                            "pitch DOUBLE NOT NULL, " +
                            // keys
                            "PRIMARY KEY(uuid, level_name), " +
                            "FOREIGN KEY(uuid) REFERENCES " + DatabaseManager.PLAYERS_TABLE + "(uuid) " +
                                "ON UPDATE CASCADE " +
                                "ON DELETE CASCADE, " +
                            "FOREIGN KEY(level_name) REFERENCES " + DatabaseManager.LEVELS_TABLE + "(name) " +
                                "ON UPDATE CASCADE " +
                                "ON DELETE CASCADE, " +
                            // indexes
                            "INDEX uuid_index(uuid)" +
                        ")";

        databaseManager.runQuery(query);
    }

    private static void createLevelPurchases(DatabaseManager databaseManager)
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.LEVEL_PURCHASES_TABLE + "(" +
                            "uuid CHAR(36) NOT NULL, " +
                            "level_name VARCHAR(20) NOT NULL, " +
                            // keys
                            "PRIMARY KEY(uuid, level_name), " +
                            "FOREIGN KEY(uuid) REFERENCES " + DatabaseManager.PLAYERS_TABLE + "(uuid) " +
                                "ON UPDATE CASCADE " +
                                "ON DELETE CASCADE, " +
                            "FOREIGN KEY(level_name) REFERENCES " + DatabaseManager.LEVELS_TABLE + "(name) " +
                                "ON UPDATE CASCADE " +
                                "ON DELETE CASCADE, " +
                            // indexes
                            "INDEX uuid_index(uuid)" +
                        ")";

        databaseManager.runQuery(query);
    }

    private static void createLevelSpawns(DatabaseManager databaseManager)
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.LEVEL_SPAWNS_TABLE + "(" +
                            "level_name VARCHAR(20) NOT NULL, " +
                            "location_name VARCHAR(30) NOT NULL, " +
                            // keys
                            "PRIMARY KEY(level_name, location_name), " +
                            // indexes
                            "INDEX level_index(level_name)" +
                        ")";

        databaseManager.runQuery(query);
    }

    private static void createClans(DatabaseManager databaseManager)
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.CLANS_TABLE + "(" +
                            "tag VARCHAR(10) NOT NULL, " +
                            "owner_uuid CHAR(36) NOT NULL, " +
                            "level SMALLINT NOT NULL DEFAULT 1, " +
                            "xp INT NOT NULL DEFAULT 0, " +
                            "total_xp BIGINT NOT NULL DEFAULT 0, " +
                            "max_level SMALLINT NOT NULL DEFAULT 5, " +
                            "max_members SMALLINT NOT NULL DEFAULT 5, " +
                            // keys
                            "PRIMARY KEY(tag), " +
                            "FOREIGN KEY(owner_uuid) REFERENCES " + DatabaseManager.PLAYERS_TABLE + "(uuid)" +
                                "ON UPDATE CASCADE " +
                                "ON DELETE CASCADE, " +
                            // constraints
                            "CONSTRAINT non_negative CHECK (" +
                                "level >= 1 AND " +
                                "xp >= 0 AND " +
                                "total_xp >= 0 AND " +
                                "max_level >= 5 AND " +
                                "max_members >= 5" +
                            ")" +
                        ")";

        databaseManager.runQuery(query);
    }

    private static void createRanks(DatabaseManager databaseManager)
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.RANKS_TABLE + "(" +
                            "name VARCHAR(10) NOT NULL, " +
                            "title VARCHAR(20) DEFAULT NULL, " + // allow space for color codes
                            "rankup_level VARCHAR(20) DEFAULT NULL, " +
                            "next_rank VARCHAR(10) DEFAULT NULL, " +
                            // keys
                            "PRIMARY KEY(name), " +
                            "FOREIGN KEY(rankup_level) REFERENCES " + DatabaseManager.LEVELS_TABLE + "(name) " +
                                "ON UPDATE CASCADE " +
                                "ON DELETE SET NULL, " +
                            "FOREIGN KEY(next_rank) REFERENCES " + DatabaseManager.RANKS_TABLE + "(name)" +
                        ")";

        databaseManager.runQuery(query);
    }

    private static void createLevelCompletions(DatabaseManager databaseManager)
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.LEVEL_COMPLETIONS_TABLE + "(" +
                            "uuid CHAR(36) NOT NULL, " +
                            "level_name VARCHAR(20) NOT NULL, " +
                            "time_taken MEDIUMINT DEFAULT 0, " +
                            "completion_date TIMESTAMP NOT NULL, " +
                            // keys
                            "PRIMARY KEY(uuid, level_name), " +
                            "FOREIGN KEY(uuid) REFERENCES " + DatabaseManager.PLAYERS_TABLE + "(uuid) " +
                                "ON UPDATE CASCADE " +
                                "ON DELETE CASCADE, " +
                            // indexes
                            "INDEX uuid_index(uuid), " +
                            // constraints
                            "CONSTRAINT non_negative CHECK (" +
                                "time_taken >= 0" +
                            ")" +
                        ")";

        databaseManager.runQuery(query);
    }

    private static void createModifiers(DatabaseManager databaseManager)
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.MODIFIERS_TABLE + "(" +
                            "name VARCHAR(20) NOT NULL, " +
                            "type VARCHAR(20) DEFAULT NULL, " +
                            "title VARCHAR(30) DEFAULT NULL, " + // add room for color codes
                            "multiplier FLOAT DEFAULT NULL, " +
                            "discount FLOAT DEFAULT NULL, " +
                            "bonus INT DEFAULT NULL, " +
                            // keys
                            "PRIMARY KEY(name), " +
                            // constraints
                            "CONSTRAINT non_negative CHECK (" +
                                "multiplier >= 0.0 AND " +
                                "discount >= 0.0 AND " +
                                "bonus >= 0" +
                            ")" +
                        ")";

        databaseManager.runQuery(query);
    }

    private static void createPlotTrustedPlayers(DatabaseManager databaseManager)
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.PLOTS_TRUSTED_PLAYERS_TABLE + "(" +
                            "plot_id INT NOT NULL, " +
                            "trusted_uuid CHAR(36) NOT NULL, " +
                            // keys
                            "PRIMARY KEY(plot_id, trusted_uuid), " +
                            "FOREIGN KEY(plot_id) REFERENCES " + DatabaseManager.PLOTS_TABLE + "(id) " +
                                "ON UPDATE CASCADE " +
                                "ON DELETE CASCADE, " +
                            "FOREIGN KEY(trusted_uuid) REFERENCES " + DatabaseManager.PLAYERS_TABLE + "(uuid) " +
                                "ON UPDATE CASCADE " +
                                "ON DELETE CASCADE, " +
                            // indexes
                            "INDEX id_index(plot_id)" +
                        ")";

        databaseManager.runQuery(query);
    }

    private static void createPlayerModifiers(DatabaseManager databaseManager)
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.PLAYER_MODIFIERS_TABLE + "(" +
                            "uuid CHAR(36) NOT NULL, " +
                            "modifier_name VARCHAR(20) NOT NULL, " +
                            // keys
                            "PRIMARY KEY(uuid, modifier_name), " +
                            "FOREIGN KEY(uuid) REFERENCES " + DatabaseManager.PLAYERS_TABLE + "(uuid) " +
                                "ON UPDATE CASCADE " +
                                "ON DELETE CASCADE, " +
                            "FOREIGN KEY(modifier_name) REFERENCES " + DatabaseManager.MODIFIERS_TABLE + "(name) " +
                                "ON UPDATE CASCADE " +
                                "ON DELETE CASCADE, " +
                            // indexes
                            "INDEX uuid_index(uuid)" +
                        ")";

        databaseManager.runQuery(query);
    }

    private static void createPerksOwned(DatabaseManager databaseManager)
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.PERKS_OWNED_TABLE + "(" +
                            "uuid CHAR(36) NOT NULL, " +
                            "perk_name VARCHAR(20) NOT NULL, " +
                            "date_received TIMESTAMP NOT NULL, " +
                            // keys
                            "PRIMARY KEY(uuid, perk_name), " +
                            "FOREIGN KEY(uuid) REFERENCES " + DatabaseManager.PLAYERS_TABLE + "(uuid) " +
                                "ON UPDATE CASCADE " +
                                "ON DELETE CASCADE, " +
                            "FOREIGN KEY(perk_name) REFERENCES " + DatabaseManager.PERKS_TABLE + "(name) " +
                                "ON UPDATE CASCADE " +
                                "ON DELETE CASCADE, " +
                            // indexes
                            "INDEX uuid_index(uuid)" +
                        ")";

        databaseManager.runQuery(query);
    }

    private static void createPerksLevelRequirements(DatabaseManager databaseManager)
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.PERKS_LEVEL_REQUIREMENTS_TABLE + "(" +
                            "perk_name VARCHAR(20) NOT NULL, " +
                            "level_name VARCHAR(20) NOT NULL, " +
                            // keys
                            "PRIMARY KEY(perk_name, level_name), " +
                            "FOREIGN KEY(perk_name) REFERENCES " + DatabaseManager.PERKS_TABLE + "(name) " +
                                "ON UPDATE CASCADE " +
                                "ON DELETE CASCADE, " +
                            "FOREIGN KEY(level_name) REFERENCES " + DatabaseManager.LEVELS_TABLE + "(name) " +
                                "ON UPDATE CASCADE " +
                                "ON DELETE CASCADE, " +
                            // indexes
                            "INDEX perk_name_index(perk_name)" +
                        ")";

        databaseManager.runQuery(query);
    }

    private static void createPerksArmor(DatabaseManager databaseManager)
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.PERKS_ARMOR_TABLE + "(" +
                            "perk_name VARCHAR(20) NOT NULL, " +
                            "armor_piece VARCHAR(10) NOT NULL, " + // choices are... HELMET, CHESTPLATE, LEGGINGS, BOOTS... so max = 10
                            "material VARCHAR(30) NOT NULL, " +
                            "type TINYINT DEFAULT 0, " +
                            "title VARCHAR(30) DEFAULT NULL, " + // allow for extra length due to color codes
                            "glow BIT DEFAULT 0, " +
                            // keys
                            "PRIMARY KEY(perk_name, armor_piece), " +
                            "FOREIGN KEY(perk_name) REFERENCES " + DatabaseManager.PERKS_TABLE + "(name) " +
                                "ON UPDATE CASCADE " +
                                "ON DELETE CASCADE, " +
                            // indexes
                            "INDEX perk_name_index(perk_name)" +
                        ")";

        databaseManager.runQuery(query);
    }

    private static void createLevelCompletionCommands(DatabaseManager databaseManager)
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.LEVEL_COMPLETIONS_COMMANDS_TABLE + "(" +
                            "level_name VARCHAR(20) NOT NULL, " +
                            "command VARCHAR(100) NOT NULL, " + // commands can get quite long
                            // keys
                            "PRIMARY KEY(level_name, command), " +
                            "FOREIGN KEY(level_name) REFERENCES " + DatabaseManager.LEVELS_TABLE + "(name) " +
                                "ON UPDATE CASCADE " +
                                "ON DELETE CASCADE, " +
                            // indexes
                            "INDEX level_name_index(level_name)" +
                        ")";

        databaseManager.runQuery(query);
    }

    private static void createLevelPotionEffects(DatabaseManager databaseManager)
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.LEVEL_POTION_EFFECTS_TABLE + "(" +
                            "level_name VARCHAR(20) NOT NULL, " +
                            "type VARCHAR(20) NOT NULL, " + // longest potion effect is DAMAGE_RESISTANCE (17), so will just say 20 max
                            "amplifier TINYINT UNSIGNED DEFAULT 0, " + // potion effects dont go past 255, so TINYINT UNSIGNED is perfect
                            "duration MEDIUMINT DEFAULT 0, " +
                            "PRIMARY KEY(level_name, type), " +
                            "FOREIGN KEY(level_name) REFERENCES " + DatabaseManager.LEVELS_TABLE + "(name) " +
                                "ON UPDATE CASCADE " +
                                "ON DELETE CASCADE, " +
                            // indexes
                            "INDEX level_name_index(level_name), " +
                            // constraints
                            "CONSTRAINT non_negative CHECK (" +
                                "amplifier >= 0 AND " +
                                "duration >= 0" +
                            ")" +
                        ")";

        databaseManager.runQuery(query);
    }

    private static void createLevelRequiredLevels(DatabaseManager databaseManager)
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.LEVEL_REQUIRED_LEVELS_TABLE + "(" +
                            "level_name VARCHAR(20) NOT NULL, " +
                            "required_level_name VARCHAR(20) NOT NULL, " +
                            // keys
                            "PRIMARY KEY(level_name, required_level_name), " +
                            "FOREIGN KEY(level_name) REFERENCES " + DatabaseManager.LEVELS_TABLE + "(name) " +
                                "ON UPDATE CASCADE " +
                                "ON DELETE CASCADE, " +
                            "FOREIGN KEY(required_level_name) REFERENCES " + DatabaseManager.LEVELS_TABLE + "(name) " +
                                "ON UPDATE CASCADE " +
                                "ON DELETE CASCADE, " +
                            // indexes
                            "INDEX level_name_index(level_name)" +
                        ")";

        databaseManager.runQuery(query);
    }

    private static void createBadges(DatabaseManager databaseManager)
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.BADGES_TABLE + "(" +
                            "name VARCHAR(20) NOT NULL, " +
                            "title VARCHAR(30) DEFAULT NULL, " + // allow for extra space for color codes
                            "required_permission VARCHAR(20) DEFAULT NULL, " +
                            "PRIMARY KEY(name)" +
                        ")";

        databaseManager.runQuery(query);
    }

    private static void createBadgesOwned(DatabaseManager databaseManager)
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.BADGES_OWNED_TABLE + "(" +
                            "uuid CHAR(36) NOT NULL, " +
                            "badge_name VARCHAR(20) NOT NULL, " +
                            // keys
                            "PRIMARY KEY(uuid, badge_name), " +
                            "FOREIGN KEY(uuid) REFERENCES " + DatabaseManager.PLAYERS_TABLE + "(uuid) " +
                                "ON UPDATE CASCADE " +
                                "ON DELETE CASCADE, " +
                            "FOREIGN KEY(badge_name) REFERENCES " + DatabaseManager.BADGES_TABLE + "(name) " +
                                "ON UPDATE CASCADE " +
                                "ON DELETE CASCADE, " +
                            // indexes
                            "INDEX uuid_index(uuid)" +
                        ")";

        databaseManager.runQuery(query);
    }

    private static void createBadgesCommands(DatabaseManager databaseManager)
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.BADGES_COMMANDS_TABLE + "(" +
                            "badge_name VARCHAR(20) NOT NULL, " +
                            "command VARCHAR(100) NOT NULL, " + // commands can get quite long
                            // keys
                            "PRIMARY KEY(badge_name, command), " +
                            "FOREIGN KEY(badge_name) REFERENCES " + DatabaseManager.BADGES_TABLE + "(name) " +
                                "ON UPDATE CASCADE " +
                                "ON DELETE CASCADE, " +
                            // indexes
                            "INDEX badge_name_index(badge_name)" +
                        ")";

        databaseManager.runQuery(query);
    }

    private static void createMasteryBadgeLevels(DatabaseManager databaseManager)
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.MASTERY_BADGE_LEVELS_TABLE + "(" +
                            "badge_name VARCHAR(20) NOT NULL, " +
                            "level_name VARCHAR(20) NOT NULL, " +
                            // keys
                            "PRIMARY KEY(badge_name, level_name), " +
                            "FOREIGN KEY(level_name) REFERENCES " + DatabaseManager.LEVELS_TABLE + "(name) " +
                                "ON UPDATE CASCADE " +
                                "ON DELETE CASCADE, " +
                            // indexes
                            "INDEX badge_name_index(badge_name)" +
                        ")";

        databaseManager.runQuery(query);
    }

    /*
    private static void createPlayers(DatabaseManager database) {
        String sqlQuery = "CREATE TABLE players(" +
                "player_id INT NOT NULL AUTO_INCREMENT, " +
                "uuid CHAR(36) NOT NULL, " +
                "player_name VARCHAR(16) NOT NULL, " +
                "coins DOUBLE DEFAULT 0, " +
                "spectatable BIT DEFAULT 1, " +
                "clan_id INT DEFAULT -1, " +
                "rank_id INT DEFAULT 1, " +
                "rank_prestiges TINYINT default 0, " +
                "infinite_classic_score INT default 0, " +
                "infinite_speedrun_score INT default 0, " +
                "infinite_sprint_score INT default 0, " +
                "infinite_timed_score INT default 0, " +
                "level_completions INT default 0, " +
                "race_wins SMALLINT default 0, " +
                "race_losses SMALLINT default 0, " +
                "night_vision BIT DEFAULT 0, " +
                "grinding BIT DEFAULT 0, " +
                "records SMALLINT default 0, " +
                "event_wins MEDIUMINT default 0, " +
                "infinite_block VARCHAR(30) DEFAULT '' NOT NULL, " +
                "fail_mode BIT DEFAULT 1, " +
                "infinite_type VARCHAR(10) DEFAULT 'classic' NOT NULL, " +
                "attempting_rankup BIT DEFAULT 0, " +
                "PRIMARY KEY (player_id)" +
                ")";

        database.runQuery(sqlQuery);
    }

    private static void createModifiers(DatabaseManager database)
    {
        String sqlQuery = "CREATE TABLE modifiers(" +
                "uuid CHAR(36) NOT NULL, " +
                "player_name VARCHAR(16) NOT NULL," +
                "modifier_name VARCHAR(20) NOT NULL)";

        database.runQuery(sqlQuery);
    }

    private static void createLevels(DatabaseManager database) {
        String sqlQuery = "CREATE TABLE levels(" +
                "level_id INT NOT NULL AUTO_INCREMENT, " +
                "level_name VARCHAR(30) NOT NULL, " +
                "reward INT DEFAULT 0, " +
                "score_modifier INT DEFAULT 1, " +
                "PRIMARY KEY (level_id)" +
                ")";

        database.runQuery(sqlQuery);
    }

    private static void createPerks(DatabaseManager database) {
        String sqlQuery = "CREATE TABLE perks(" +
                "perk_id INT NOT NULL AUTO_INCREMENT, " +
                "perk_name VARCHAR(30) NOT NULL, " +
                "PRIMARY KEY (perk_id)" +
                ")";

        database.runQuery(sqlQuery);
    }

    private static void createClans(DatabaseManager database) {
        String sqlQuery = "CREATE TABLE clans(" +
                "clan_id INT NOT NULL AUTO_INCREMENT, " +
                "clan_tag VARCHAR(7) NOT NULL, " +
                "clan_level SMALLINT NOT NULL, " +
                "clan_xp INT NOT NULL, " +
                "total_gained_xp BIGINT NOT NULL, " +
                "owner_player_id INT NOT NULL, " +
                "max_level SMALLINT NOT NULL DEFAULT 5, " +
                "max_members SMALLINT NOT NULL DEFAULT 5, " +
                "PRIMARY KEY (clan_id)" +
                ")";

        database.runQuery(sqlQuery);
    }

    private static void createLedger(DatabaseManager database) {
        String sqlQuery = "CREATE TABLE ledger(" +
                "player_id INT NOT NULL, " +
                "perk_id INT NOT NULL, " +
                "date TIMESTAMP NOT NULL" +
                ")";

        database.runQuery(sqlQuery);
    }

    private static void createCompletions(DatabaseManager database) {
        String sqlQuery = "CREATE TABLE completions(" +
                "player_id INT NOT NULL, " +
                "level_id INT NOT NULL, " +
                "time_taken MEDIUMINT DEFAULT 0, " +
                "completion_date TIMESTAMP NOT NULL" +
                ")";

        database.runQuery(sqlQuery);
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
        database.runQuery(sqlQuery);
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

        database.runQuery(sqlQuery);
    }

    private static void createRatings(DatabaseManager database) {
        String sqlQuery = "CREATE TABLE ratings(" +
                "uuid CHAR(36) NOT NULL, " +
                "player_name VARCHAR(16) NOT NULL, " +
                "level_id INT NOT NULL, " +
                "rating TINYINT NOT NULL" +
                ")";

        database.runQuery(sqlQuery);
    }

    private static void createPurchasedLevels(DatabaseManager database)
    {
        String sqlQuery = "CREATE TABLE bought_levels(" +
                "uuid CHAR(36) NOT NUll, " +
                "player_name VARCHAR(16) NOT NULL, " +
                "level_name VARCHAR(30) NOT NULL)";

        database.runQuery(sqlQuery);
    }

    private static void createSaves(DatabaseManager database)
    {
        String sqlQuery = "CREATE TABLE saves(" +
                "uuid CHAR(36) NOT NULL, " +
                "player_name VARCHAR(16) NOT NULL, " +
                "level_name VARCHAR(30) NOT NULL, " +
                "world VARCHAR(15) NOT NULL, " +
                "x FLOAT NOT NULL, " +
                "y FLOAT NOT NULL, " +
                "z FLOAT NOT NULL, " +
                "yaw FLOAT NOT NULL, " +
                "pitch FLOAT NOT NULL" +
                ")";

        database.runQuery(sqlQuery);
    }*/
}
