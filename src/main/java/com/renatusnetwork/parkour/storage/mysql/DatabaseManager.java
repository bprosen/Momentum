package com.renatusnetwork.parkour.storage.mysql;

import com.renatusnetwork.parkour.Parkour;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class DatabaseManager {

    private DatabaseConnection connection;

    // constants for all table names
    public static final String PLAYERS_TABLE = "players";
    public static final String LEVELS_TABLE = "levels";
    public static final String PERKS_TABLE = "perks";
    public static final String PLOTS_TABLE = "plots";
    public static final String LOCATIONS_TABLE = "locations";
    public static final String LEVEL_RATINGS_TABLE = "level_ratings";
    public static final String LEVEL_CHECKPOINTS_TABLE = "level_checkpoints";
    public static final String LEVEL_SAVES_TABLE = "level_saves";
    public static final String LEVEL_PURCHASES_TABLE = "level_purchases";
    public static final String LEVEL_COMPLETIONS_TABLE = "level_completions";
    public static final String CLANS_TABLE = "clans";
    public static final String RANKS_TABLE = "ranks";
    public static final String PLOTS_TRUSTED_PLAYERS_TABLE = "plot_trusted_players";
    public static final String MODIFIERS_TABLE = "modifiers";
    public static final String PLAYER_MODIFIERS_TABLE = "player_modifiers";
    public static final String PERKS_OWNED_TABLE = "perks_owned";
    public static final String PERKS_LEVEL_REQUIREMENTS_TABLE = "perks_level_requirements";
    public static final String PERKS_ARMOR_TABLE = "perks_armor";
    public static final String LEVEL_COMPLETIONS_COMMANDS_TABLE = "level_completions_commands";
    public static final String LEVEL_POTION_EFFECTS_TABLE = "level_potion_effects";
    public static final String LEVEL_REQUIRED_LEVELS_TABLE = "level_required_levels";
    public static final String BADGES_TABLE = "badges";
    public static final String BADGES_OWNED_TABLE = "badges_owned";
    public static final String BADGES_COMMANDS_TABLE = "badges_commands";
    public static final String MASTERY_BADGE_LEVELS_TABLE = "mastery_badge_levels";

    public DatabaseManager(Plugin plugin)
    {
        connection = new DatabaseConnection();
        TablesDB.initTables();
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
                            "SELECT * FROM " + DatabaseManager.PLAYERS_TABLE + " WHERE UUID='s'");
                    statement.execute();
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskTimerAsynchronously(plugin, 20 * 60 * 10, 20 * 60 * 10);
    }

    public DatabaseConnection getConnection() {
        return connection;
    }
}
