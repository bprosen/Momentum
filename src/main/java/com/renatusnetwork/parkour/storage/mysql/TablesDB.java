package com.renatusnetwork.parkour.storage.mysql;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.infinite.gamemode.InfiniteType;
import com.renatusnetwork.parkour.data.levels.LevelType;
import com.renatusnetwork.parkour.data.modifiers.ModifierType;
import com.renatusnetwork.parkour.data.perks.PerksArmorType;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class TablesDB
{
    public static void initTables()
    {
        HashSet<String> tables = getTables(Parkour.getDatabaseManager().getConnection());

        // initialize all tables...
        if (tables.isEmpty())
        {
            createTables();
            createKeys();
        }
    }

    private static HashSet<String> getTables(DatabaseConnection connection)
    {
        HashSet<String> tableNames = new HashSet<>();
        DatabaseMetaData meta = connection.getMeta();

        try
        {
            ResultSet rs = meta.getTables(null, null, "%", null);

            while (rs.next())
                tableNames.add(rs.getString(3));
        }
        catch (SQLException exception)
        {
            exception.printStackTrace();
        }

        return tableNames;
    }

    private static void createTables()
    {
        createPlayers();
        createLevels();
        createPerks();
        createPlots();
        createLocations();
        createLevelRatings();
        createLevelCheckpoints();
        createLevelSaves();
        createLevelPurchases();
        createLevelCompletions();
        createClans();
        createRanks();
        createPlotTrustedPlayers();
        createModifiers();
        createPlayerModifiers();
        createPerksOwned();
        createPerksLevelRequirements();
        createPerksArmor();
        createLevelCompletionCommands();
        createLevelPotionEffects();
        createLevelRequiredLevels();
        createBadges();
        createBadgesOwned();
        createBadgesCommands();
        createMasteryBadgeLevels();
    }

    private static void createKeys()
    {
        
    }

    private static void createPlayers()
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.PLAYERS_TABLE + "(" +
                           "uuid CHAR(36) NOT NULL, " +
                           "name VARCHAR(16) NOT NULL, " +
                           "clan VARCHAR(10) DEFAULT NULL, " + // default not in a clan
                           "rank_name VARCHAR(10) DEFAULT NULL, " + // default set from settings
                           "prestiges TINYINT DEFAULT 0, " +
                           "coins DOUBLE DEFAULT 0, " +
                           "infinite_classic_score INT DEFAULT 0, " +
                           "infinite_speedrun_score INT DEFAULT 0, " +
                           "infinite_sprint_score INT DEFAULT 0, " +
                           "infinite_timed_score INT DEFAULT 0, " +
                           "infinite_block ENUM(" + enumQuotations(Material.values()) + ") DEFAULT NULL, " + // default set from settings
                           "infinite_type ENUM(" + enumQuotations(InfiniteType.values()) + ") DEFAULT NULL, " + // default set from settings
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
                               "race_wins >= 0 AND " +
                               "race_losses >= 0 AND " +
                               "event_wins >= 0" +
                           ")" +
                       ")";

        DatabaseQueries.runQuery(query);

        String foreignKeyQuery = "ALTER TABLE " + DatabaseManager.PLAYERS_TABLE + " ADD CONSTRAINT " + DatabaseManager.PLAYERS_TABLE + "_fk " +
                                 "FOREIGN KEY(clan) REFERENCES " + DatabaseManager.CLANS_TABLE + "(tag) " +
                                 "ON UPDATE CASCADE " +
                                 "ON DELETE SET NULL, " +
                                 "FOREIGN KEY(rank_name) REFERENCES " + DatabaseManager.RANKS_TABLE + "(name) " +
                                 "ON UPDATE CASCADE " +
                                 "ON DELETE SET NULL";

        DatabaseQueries.runQuery(foreignKeyQuery);
    }

    private static void createLevels()
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
                            "type ENUM(" + enumQuotations(LevelType.values()) + ") DEFAULT NULL, " + // default set from settings
                            "difficulty TINYINT DEFAULT NULL, " +
                            // switches
                            "cooldown BIT DEFAULT 0, " +
                            "broadcast BIT DEFAULT 0, " +
                            "liquid_reset BIT DEFAULT 0, " +
                            "new BIT DEFAULT 0, " +
                            "has_mastery BIT DEFAULT 0, " +
                            // keys
                            "PRIMARY KEY(name), " +
                            // indexes
                            "INDEX type_index(type), " +
                            // constraints
                            "CONSTRAINT non_negative CHECK (" +
                                    "reward >= 0 AND " +
                                    "price >= 0" +
                            ")" +
                        ")";

        DatabaseQueries.runQuery(query);

        String foreignKeyQuery = "ALTER TABLE " + DatabaseManager.LEVELS_TABLE + " ADD CONSTRAINT " + DatabaseManager.LEVELS_TABLE + "_fk " +
                                 "FOREIGN KEY(required_rank) REFERENCES " + DatabaseManager.RANKS_TABLE + "(name)" +
                                 "ON UPDATE CASCADE " +
                                 "ON DELETE SET NULL";

        DatabaseQueries.runQuery(foreignKeyQuery);
    }

    private static void createPerks()
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.PERKS_TABLE + "(" +
                            "name VARCHAR(20) NOT NULL, " +
                            "title VARCHAR(30) DEFAULT NULL, " + // this needs to be long to allow for storage of colors
                            // settings
                            "price INT DEFAULT NULL, " +
                            "required_permission VARCHAR(20) DEFAULT NULL, " +
                            "infinite_block ENUM(" + enumQuotations(Material.values()) + ") DEFAULT NULL, " +
                            // keys
                            "PRIMARY KEY(name), " +
                            // constraints
                            "CONSTRAINT non_negative CHECK (" +
                                "price >= 0" +
                            ")" +
                        ")";

        DatabaseQueries.runQuery(query);
    }

    private static void createPlots()
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.PLOTS_TABLE + "(" +
                            "id INT NOT NULL AUTO_INCREMENT, " +
                            "owner_uuid CHAR(36) NOT NULL, " +
                            "center_x INT NOT NULL, " +
                            "center_z INT NOT NULL, " +
                            "submitted BIT DEFAULT 0, " +
                            // keys
                            "PRIMARY KEY(plot_id)" +
                        ")";

        DatabaseQueries.runQuery(query);

        String foreignKeyQuery = "ALTER TABLE " + DatabaseManager.PLOTS_TABLE + " ADD CONSTRAINT " + DatabaseManager.PLOTS_TABLE + "_fk " +
                                 "FOREIGN KEY(owner_uuid) REFERENCES " + DatabaseManager.PLAYERS_TABLE + "(uuid) " +
                                 "ON DELETE CASCADE, " +
                                 "ON UPDATE CASCADE"; // we want to delete their plot if the player is deleted from the db

        DatabaseQueries.runQuery(foreignKeyQuery);
    }

    private static void createLocations()
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.LOCATIONS_TABLE + "(" +
                            "name VARCHAR(30) NOT NULL, " +
                            "world VARCHAR(30) NOT NULL, " +
                            "x DOUBLE NOT NULL, " +
                            "y DOUBLE NOT NULL, " +
                            "z DOUBLE NOT NULL, " +
                            "yaw FLOAT NOT NULL, " +
                            "pitch FLOAT NOT NULL, " +
                            "PRIMARY KEY(name)" +
                        ")";

        DatabaseQueries.runQuery(query);
    }

    private static void createLevelRatings()
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.LEVEL_RATINGS_TABLE + "(" +
                            "uuid CHAR(36) NOT NULL, " +
                            "level_name VARCHAR(20) NOT NULL, " +
                            "rating TINYINT DEFAULT NULL, " +
                            // keys
                            "PRIMARY KEY(uuid, level_name), " +
                            // indexes
                            "INDEX uuid_index(uuid), " +
                            // constraints
                            "CONSTRAINT rating_in_bounds CHECK (" +
                                "ratings >= 0 AND rating <= 5" +
                            ")" +
                        ")";

        DatabaseQueries.runQuery(query);

        String foreignKeyQuery = "ALTER TABLE " + DatabaseManager.LEVEL_RATINGS_TABLE + " ADD CONSTRAINT " + DatabaseManager.LEVEL_RATINGS_TABLE + "_fk " +
                                 "FOREIGN KEY(uuid) REFERENCES " + DatabaseManager.PLAYERS_TABLE + "(uuid) " +
                                 "ON UPDATE CASCADE " +
                                 "ON DELETE CASCADE, " +
                                 "FOREIGN KEY(level_name) REFERENCES " + DatabaseManager.LEVELS_TABLE + "(name) " +
                                 "ON UPDATE CASCADE " +
                                 "ON DELETE CASCADE";

        DatabaseQueries.runQuery(foreignKeyQuery);
    }

    private static void createLevelCheckpoints()
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
                            // indexes
                            "INDEX uuid_index(uuid)" +
                        ")";

        DatabaseQueries.runQuery(query);

        String foreignKeyQuery = "ALTER TABLE " + DatabaseManager.LEVEL_CHECKPOINTS_TABLE + " ADD CONSTRAINT " + DatabaseManager.LEVEL_CHECKPOINTS_TABLE + "_fk " +
                                 "FOREIGN KEY(uuid) REFERENCES " + DatabaseManager.PLAYERS_TABLE + "(uuid) " +
                                 "ON UPDATE CASCADE " +
                                 "ON DELETE CASCADE, " +
                                 "FOREIGN KEY(level_name) REFERENCES " + DatabaseManager.LEVELS_TABLE + "(name) " +
                                 "ON UPDATE CASCADE " +
                                 "ON DELETE CASCADE";

        DatabaseQueries.runQuery(foreignKeyQuery);
    }

    private static void createLevelSaves()
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
                            // indexes
                            "INDEX uuid_index(uuid)" +
                        ")";

        DatabaseQueries.runQuery(query);

        String foreignKeyQuery = "ALTER TABLE " + DatabaseManager.LEVEL_SAVES_TABLE + " ADD CONSTRAINT " + DatabaseManager.LEVEL_SAVES_TABLE + "_fk " +
                                 "FOREIGN KEY(uuid) REFERENCES " + DatabaseManager.PLAYERS_TABLE + "(uuid) " +
                                 "ON UPDATE CASCADE " +
                                 "ON DELETE CASCADE, " +
                                 "FOREIGN KEY(level_name) REFERENCES " + DatabaseManager.LEVELS_TABLE + "(name) " +
                                 "ON UPDATE CASCADE " +
                                 "ON DELETE CASCADE";

        DatabaseQueries.runQuery(foreignKeyQuery);
    }

    private static void createLevelPurchases()
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.LEVEL_PURCHASES_TABLE + "(" +
                            "uuid CHAR(36) NOT NULL, " +
                            "level_name VARCHAR(20) NOT NULL, " +
                            // keys
                            "PRIMARY KEY(uuid, level_name), " +
                            // indexes
                            "INDEX uuid_index(uuid)" +
                        ")";

        DatabaseQueries.runQuery(query);

        String foreignKeyQuery = "ALTER TABLE " + DatabaseManager.LEVEL_PURCHASES_TABLE + " ADD CONSTRAINT " + DatabaseManager.LEVEL_PURCHASES_TABLE + "_fk " +
                                 "FOREIGN KEY(uuid) REFERENCES " + DatabaseManager.PLAYERS_TABLE + "(uuid) " +
                                 "ON UPDATE CASCADE " +
                                 "ON DELETE CASCADE, " +
                                 "FOREIGN KEY(level_name) REFERENCES " + DatabaseManager.LEVELS_TABLE + "(name) " +
                                 "ON UPDATE CASCADE " +
                                 "ON DELETE CASCADE";

        DatabaseQueries.runQuery(foreignKeyQuery);
    }

    private static void createClans()
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
                            // constraints
                            "CONSTRAINT non_negative CHECK (" +
                                "level >= 1 AND " +
                                "xp >= 0 AND " +
                                "total_xp >= 0 AND " +
                                "max_level >= 5 AND " +
                                "max_members >= 5" +
                            ")" +
                        ")";

        DatabaseQueries.runQuery(query);

        String foreignKeyQuery = "ALTER TABLE " + DatabaseManager.CLANS_TABLE + " ADD CONSTRAINT " + DatabaseManager.CLANS_TABLE + "_fk " +
                                 "FOREIGN KEY(owner_uuid) REFERENCES " + DatabaseManager.PLAYERS_TABLE + "(uuid)" +
                                 "ON UPDATE CASCADE " +
                                 "ON DELETE CASCADE";

        DatabaseQueries.runQuery(foreignKeyQuery);
    }

    private static void createRanks()
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.RANKS_TABLE + "(" +
                            "name VARCHAR(10) NOT NULL, " +
                            "title VARCHAR(20) DEFAULT NULL, " + // allow space for color codes
                            "rankup_level VARCHAR(20) DEFAULT NULL, " +
                            "next_rank VARCHAR(10) DEFAULT NULL, " +
                            // keys
                            "PRIMARY KEY(name)" +
                        ")";

        DatabaseQueries.runQuery(query);

        String foreignKeyQuery = "ALTER TABLE " + DatabaseManager.RANKS_TABLE + " ADD CONSTRAINT " + DatabaseManager.RANKS_TABLE + "_fk " +
                                 "FOREIGN KEY(rankup_level) REFERENCES " + DatabaseManager.LEVELS_TABLE + "(name) " +
                                 "ON UPDATE CASCADE " +
                                 "ON DELETE SET NULL, " +
                                 "FOREIGN KEY(next_rank) REFERENCES " + DatabaseManager.RANKS_TABLE + "(name)";

        DatabaseQueries.runQuery(foreignKeyQuery);
    }

    private static void createLevelCompletions()
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.LEVEL_COMPLETIONS_TABLE + "(" +
                            "uuid CHAR(36) NOT NULL, " +
                            "level_name VARCHAR(20) NOT NULL, " +
                            "completion_date TIMESTAMP NOT NULL, " +
                            "time_taken MEDIUMINT DEFAULT 0, " +
                            "mastery BIT DEFAULT 0, " +
                            "record BIT DEFAULT 0, " +
                            // keys
                            "PRIMARY KEY(uuid, level_name, completion_date), " +
                            // indexes
                            "INDEX uuid_index(uuid), " + // gets the completions for that user fast
                            "INDEX level_index(level_name), " + // gets the completions for that level fast
                            "UNIQUE INDEX record_index(uuid, record), " + // get a users records fast, where uuid=player uuid and record=1
                            // constraints
                            "CONSTRAINT non_negative CHECK (" +
                                "time_taken >= 0" +
                            ")" +
                        ")";

        DatabaseQueries.runQuery(query);

        String foreignKeyQuery = "ALTER TABLE " + DatabaseManager.LEVEL_COMPLETIONS_TABLE + " ADD CONSTRAINT " + DatabaseManager.LEVEL_COMPLETIONS_TABLE + "_fk " +
                                 "FOREIGN KEY(uuid) REFERENCES " + DatabaseManager.PLAYERS_TABLE + "(uuid) " +
                                 "ON UPDATE CASCADE " +
                                 "ON DELETE CASCADE, " +
                                 "FOREIGN KEY(level_name) REFERENCES " + DatabaseManager.LEVELS_TABLE + "(name) " +
                                 "ON UPDATE CASCADE " +
                                 "ON DELETE CASCADE";

        DatabaseQueries.runQuery(foreignKeyQuery);
    }

    private static void createModifiers()
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.MODIFIERS_TABLE + "(" +
                            "name VARCHAR(20) NOT NULL, " +
                            "type ENUM(" + enumQuotations(ModifierType.values()) + ") DEFAULT NULL, " +
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

        DatabaseQueries.runQuery(query);
    }

    private static void createPlotTrustedPlayers()
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.PLOTS_TRUSTED_PLAYERS_TABLE + "(" +
                            "plot_id INT NOT NULL, " +
                            "trusted_uuid CHAR(36) NOT NULL, " +
                            // keys
                            "PRIMARY KEY(plot_id, trusted_uuid), " +
                            // indexes
                            "INDEX id_index(plot_id)" +
                        ")";

        DatabaseQueries.runQuery(query);

        String foreignKeyQuery = "ALTER TABLE " + DatabaseManager.PLOTS_TRUSTED_PLAYERS_TABLE + " ADD CONSTRAINT " + DatabaseManager.PLOTS_TRUSTED_PLAYERS_TABLE + "_fk " +
                                 "FOREIGN KEY(plot_id) REFERENCES " + DatabaseManager.PLOTS_TABLE + "(id) " +
                                 "ON UPDATE CASCADE " +
                                 "ON DELETE CASCADE, " +
                                 "FOREIGN KEY(trusted_uuid) REFERENCES " + DatabaseManager.PLAYERS_TABLE + "(uuid) " +
                                 "ON UPDATE CASCADE " +
                                 "ON DELETE CASCADE";

        DatabaseQueries.runQuery(foreignKeyQuery);
    }

    private static void createPlayerModifiers()
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.PLAYER_MODIFIERS_TABLE + "(" +
                            "uuid CHAR(36) NOT NULL, " +
                            "modifier_name VARCHAR(20) NOT NULL, " +
                            // keys
                            "PRIMARY KEY(uuid, modifier_name), " +
                            // indexes
                            "INDEX uuid_index(uuid)" +
                        ")";

        DatabaseQueries.runQuery(query);

        String foreignKeyQuery = "ALTER TABLE " + DatabaseManager.PLAYER_MODIFIERS_TABLE + " ADD CONSTRAINT " + DatabaseManager.PLAYER_MODIFIERS_TABLE + "_fk " +
                                 "FOREIGN KEY(uuid) REFERENCES " + DatabaseManager.PLAYERS_TABLE + "(uuid) " +
                                 "ON UPDATE CASCADE " +
                                 "ON DELETE CASCADE, " +
                                 "FOREIGN KEY(modifier_name) REFERENCES " + DatabaseManager.MODIFIERS_TABLE + "(name) " +
                                 "ON UPDATE CASCADE " +
                                 "ON DELETE CASCADE";

        DatabaseQueries.runQuery(foreignKeyQuery);
    }

    private static void createPerksOwned()
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.PERKS_OWNED_TABLE + "(" +
                            "uuid CHAR(36) NOT NULL, " +
                            "perk_name VARCHAR(20) NOT NULL, " +
                            "date_received TIMESTAMP NOT NULL, " +
                            // keys
                            "PRIMARY KEY(uuid, perk_name), " +
                            // indexes
                            "INDEX uuid_index(uuid)" +
                        ")";

        DatabaseQueries.runQuery(query);

        String foreignKeyQuery = "ALTER TABLE " + DatabaseManager.PERKS_OWNED_TABLE + " ADD CONSTRAINT " + DatabaseManager.PERKS_OWNED_TABLE + "_fk " +
                                 "FOREIGN KEY(uuid) REFERENCES " + DatabaseManager.PLAYERS_TABLE + "(uuid) " +
                                 "ON UPDATE CASCADE " +
                                 "ON DELETE CASCADE, " +
                                 "FOREIGN KEY(perk_name) REFERENCES " + DatabaseManager.PERKS_TABLE + "(name) " +
                                 "ON UPDATE CASCADE " +
                                 "ON DELETE CASCADE";

        DatabaseQueries.runQuery(foreignKeyQuery);
    }

    private static void createPerksLevelRequirements()
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.PERKS_LEVEL_REQUIREMENTS_TABLE + "(" +
                            "perk_name VARCHAR(20) NOT NULL, " +
                            "level_name VARCHAR(20) NOT NULL, " +
                            // keys
                            "PRIMARY KEY(perk_name, level_name), " +
                            // indexes
                            "INDEX perk_name_index(perk_name)" +
                        ")";

        DatabaseQueries.runQuery(query);

        String foreignKeyQuery = "ALTER TABLE " + DatabaseManager.PERKS_LEVEL_REQUIREMENTS_TABLE + " ADD CONSTRAINT " + DatabaseManager.PERKS_LEVEL_REQUIREMENTS_TABLE + "_fk " +
                                 "FOREIGN KEY(perk_name) REFERENCES " + DatabaseManager.PERKS_TABLE + "(name) " +
                                 "ON UPDATE CASCADE " +
                                 "ON DELETE CASCADE, " +
                                 "FOREIGN KEY(level_name) REFERENCES " + DatabaseManager.LEVELS_TABLE + "(name) " +
                                 "ON UPDATE CASCADE " +
                                 "ON DELETE CASCADE";

        DatabaseQueries.runQuery(foreignKeyQuery);
    }

    private static void createPerksArmor()
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.PERKS_ARMOR_TABLE + "(" +
                            "perk_name VARCHAR(20) NOT NULL, " +
                            "armor_piece ENUM(" + enumQuotations(PerksArmorType.values()) + ") NOT NULL, " + // choices are... HELMET, CHESTPLATE, LEGGINGS, BOOTS
                            "material ENUM(" + enumQuotations(Material.values()) + ") NOT NULL, " +
                            "type TINYINT DEFAULT 0, " +
                            "title VARCHAR(30) DEFAULT NULL, " + // allow for extra length due to color codes
                            "glow BIT DEFAULT 0, " +
                            // keys
                            "PRIMARY KEY(perk_name, armor_piece), " +
                            // indexes
                            "INDEX perk_name_index(perk_name)" +
                        ")";

        DatabaseQueries.runQuery(query);

        String foreignKeyQuery = "ALTER TABLE " + DatabaseManager.PERKS_ARMOR_TABLE + " ADD CONSTRAINT " + DatabaseManager.PERKS_ARMOR_TABLE + "_fk " +
                                 "FOREIGN KEY(perk_name) REFERENCES " + DatabaseManager.PERKS_TABLE + "(name) " +
                                 "ON UPDATE CASCADE " +
                                 "ON DELETE CASCADE";

        DatabaseQueries.runQuery(foreignKeyQuery);
    }

    private static void createLevelCompletionCommands()
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.LEVEL_COMPLETIONS_COMMANDS_TABLE + "(" +
                            "level_name VARCHAR(20) NOT NULL, " +
                            "command VARCHAR(100) NOT NULL, " + // commands can get quite long
                            // keys
                            "PRIMARY KEY(level_name, command), " +
                            // indexes
                            "INDEX level_name_index(level_name)" +
                        ")";

        DatabaseQueries.runQuery(query);

        String foreignKeyQuery = "ALTER TABLE " + DatabaseManager.LEVEL_COMPLETIONS_COMMANDS_TABLE + " ADD CONSTRAINT " + DatabaseManager.LEVEL_COMPLETIONS_COMMANDS_TABLE + "_fk " +
                                 "FOREIGN KEY(level_name) REFERENCES " + DatabaseManager.LEVELS_TABLE + "(name) " +
                                 "ON UPDATE CASCADE " +
                                 "ON DELETE CASCADE";

        DatabaseQueries.runQuery(foreignKeyQuery);
    }

    private static void createLevelPotionEffects()
    {
        /*
            SPIGOT IS A PAIN AND DOES NOT DO AN ENUM FOR POTIONEFFECTTYPE, HAVE TO DUPLICATE CODE
         */
        String finalString = "";

        for (PotionEffectType type : PotionEffectType.values())
            finalString += "'" + type.getName().toUpperCase() + "',";

        finalString = finalString.substring(0, finalString.length() - 1);

        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.LEVEL_POTION_EFFECTS_TABLE + "(" +
                            "level_name VARCHAR(20) NOT NULL, " +
                            "type ENUM(" + finalString + ") NOT NULL, " +
                            "amplifier TINYINT UNSIGNED DEFAULT 0, " + // potion effects dont go past 255, so TINYINT UNSIGNED is perfect
                            "duration MEDIUMINT DEFAULT 0, " +
                            "PRIMARY KEY(level_name, type), " +
                            // indexes
                            "INDEX level_name_index(level_name), " +
                            // constraints
                            "CONSTRAINT non_negative CHECK (" +
                                "amplifier >= 0 AND " +
                                "duration >= 0" +
                            ")" +
                        ")";

        DatabaseQueries.runQuery(query);

        String foreignKeyQuery = "ALTER TABLE " + DatabaseManager.LEVEL_POTION_EFFECTS_TABLE + " ADD CONSTRAINT " + DatabaseManager.LEVEL_POTION_EFFECTS_TABLE + "_fk " +
                                 "FOREIGN KEY(level_name) REFERENCES " + DatabaseManager.LEVELS_TABLE + "(name) " +
                                 "ON UPDATE CASCADE " +
                                 "ON DELETE CASCADE";

        DatabaseQueries.runQuery(foreignKeyQuery);
    }

    private static void createLevelRequiredLevels()
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.LEVEL_REQUIRED_LEVELS_TABLE + "(" +
                            "level_name VARCHAR(20) NOT NULL, " +
                            "required_level_name VARCHAR(20) NOT NULL, " +
                            // keys
                            "PRIMARY KEY(level_name, required_level_name), " +
                            // indexes
                            "INDEX level_name_index(level_name)" +
                        ")";

        DatabaseQueries.runQuery(query);

        String foreignKeyQuery = "ALTER TABLE " + DatabaseManager.LEVEL_REQUIRED_LEVELS_TABLE + " ADD CONSTRAINT " + DatabaseManager.LEVEL_REQUIRED_LEVELS_TABLE + "_fk " +
                                 "FOREIGN KEY(level_name) REFERENCES " + DatabaseManager.LEVELS_TABLE + "(name) " +
                                 "ON UPDATE CASCADE " +
                                 "ON DELETE CASCADE, " +
                                 "FOREIGN KEY(required_level_name) REFERENCES " + DatabaseManager.LEVELS_TABLE + "(name) " +
                                 "ON UPDATE CASCADE " +
                                 "ON DELETE CASCADE";

        DatabaseQueries.runQuery(foreignKeyQuery);
    }

    private static void createBadges()
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.BADGES_TABLE + "(" +
                            "name VARCHAR(20) NOT NULL, " +
                            "title VARCHAR(30) DEFAULT NULL, " + // allow for extra space for color codes
                            "required_permission VARCHAR(20) DEFAULT NULL, " +
                            "PRIMARY KEY(name)" +
                        ")";

        DatabaseQueries.runQuery(query);
    }

    private static void createBadgesOwned()
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.BADGES_OWNED_TABLE + "(" +
                            "uuid CHAR(36) NOT NULL, " +
                            "badge_name VARCHAR(20) NOT NULL, " +
                            // keys
                            "PRIMARY KEY(uuid, badge_name), " +
                            // indexes
                            "INDEX uuid_index(uuid)" +
                        ")";

        DatabaseQueries.runQuery(query);

        String foreignKeyQuery = "ALTER TABLE " + DatabaseManager.BADGES_OWNED_TABLE + " ADD CONSTRAINT " + DatabaseManager.BADGES_OWNED_TABLE + "_fk " +
                                 "FOREIGN KEY(uuid) REFERENCES " + DatabaseManager.PLAYERS_TABLE + "(uuid) " +
                                 "ON UPDATE CASCADE " +
                                 "ON DELETE CASCADE, " +
                                 "FOREIGN KEY(badge_name) REFERENCES " + DatabaseManager.BADGES_TABLE + "(name) " +
                                 "ON UPDATE CASCADE " +
                                 "ON DELETE CASCADE";

        DatabaseQueries.runQuery(foreignKeyQuery);
    }

    private static void createBadgesCommands()
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

        DatabaseQueries.runQuery(query);

        String foreignKeyQuery = "ALTER TABLE " + DatabaseManager.BADGES_COMMANDS_TABLE + " ADD CONSTRAINT " + DatabaseManager.BADGES_COMMANDS_TABLE + "_fk " +
                                 "FOREIGN KEY(badge_name) REFERENCES " + DatabaseManager.BADGES_TABLE + "(name) " +
                                 "ON UPDATE CASCADE " +
                                 "ON DELETE CASCADE";

        DatabaseQueries.runQuery(foreignKeyQuery);
    }

    private static void createMasteryBadgeLevels()
    {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.MASTERY_BADGE_LEVELS_TABLE + "(" +
                            "badge_name VARCHAR(20) NOT NULL, " +
                            "level_name VARCHAR(20) NOT NULL, " +
                            // keys
                            "PRIMARY KEY(badge_name, level_name), " +
                            // indexes
                            "INDEX badge_name_index(badge_name)" +
                        ")";

        DatabaseQueries.runQuery(query);

        String foreignKeyQuery = "ALTER TABLE " + DatabaseManager.MASTERY_BADGE_LEVELS_TABLE + " ADD CONSTRAINT " + DatabaseManager.MASTERY_BADGE_LEVELS_TABLE + "_fk " +
                                 "FOREIGN KEY(level_name) REFERENCES " + DatabaseManager.LEVELS_TABLE + "(name) " +
                                 "ON UPDATE CASCADE " +
                                 "ON DELETE CASCADE";

        DatabaseQueries.runQuery(foreignKeyQuery);
    }

    private static String enumQuotations(Enum<?>[] array)
    {
        String finalString = "";

        for (Enum<?> enumerator : array)
            finalString += "'" + enumerator.name() + "',";

        return finalString.substring(0, finalString.length() - 1);
    }
}