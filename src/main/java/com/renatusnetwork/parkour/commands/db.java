package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.events.types.EventType;
import com.renatusnetwork.parkour.data.levels.LevelType;
import com.renatusnetwork.parkour.storage.mysql.DatabaseManager;
import com.renatusnetwork.parkour.storage.mysql.DatabaseQueries;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Map;
import java.util.Set;

public class db implements CommandExecutor
{
    private Connection connection;
    private FileConfiguration settings = Parkour.getConfigManager().get("settings");


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] a)
    {
        if (a.length == 1 && a[0].equalsIgnoreCase("migrate"))
        {
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        FileConfiguration levelsConfig = null;
                        boolean cannotContinue = false;

                        try
                        {
                            File oldLevels = new File(Parkour.getPlugin().getDataFolder(), "levels.yml");
                            levelsConfig = new YamlConfiguration();
                            levelsConfig.load(oldLevels);
                        }
                        catch (IOException | InvalidConfigurationException exception)
                        {
                            exception.printStackTrace();
                            cannotContinue = true;
                        }

                        if (!cannotContinue)
                        {
                            sender.sendMessage(Utils.translate("&cBeginning migration..."));
                            Parkour.getPluginLogger().info("Opening db connection to second db");
                            open();
                            Parkour.getPluginLogger().info("Temporarily disabling foreign key checks");
                            DatabaseQueries.runQuery("SET foreign_key_checks = 0");

                            Parkour.getPluginLogger().info("Attempting migration of players table");

                            String playersQuery = "SELECT * FROM players LEFT JOIN clans ON clans.clan_id=players.clan_id";

                            PreparedStatement statement = connection.prepareStatement(playersQuery);
                            ResultSet results = statement.executeQuery();
                            int playersCount = 0;
                            while (results.next())
                            {
                                String uuid = results.getString("uuid");
                                String name = results.getString("player_name");
                                double coins = results.getDouble("coins");
                                int spectatable = results.getInt("spectatable");
                                String rankIdTemp = String.valueOf(results.getInt("rank_id")); // will need to manually fix later
                                String clanTag = results.getString("clan_tag");
                                int prestiges = results.getInt("rank_prestiges");
                                int raceWins = results.getInt("race_wins");
                                int raceLosses = results.getInt("race_losses");
                                int nightVision = results.getInt("night_vision");
                                int grinding = results.getInt("grinding");
                                String infiniteBlock = results.getString("infinite_block").toUpperCase();

                                if (infiniteBlock.isEmpty())
                                    infiniteBlock = Parkour.getSettingsManager().infinite_default_block.name();

                                int failMode = results.getInt("fail_mode");
                                int eventWins = results.getInt("event_wins");

                                DatabaseQueries.runQuery(
                                        "INSERT INTO " + DatabaseManager.PLAYERS_TABLE + " " +
                                                "(uuid, name, coins, spectatable, rank_name, clan, prestiges, race_wins, race_losses, night_vision, grinding, infinite_block, fail_mode, event_wins) VALUES " +
                                                "(?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                                        uuid, name, coins, spectatable, rankIdTemp, clanTag, prestiges, raceWins, raceLosses, nightVision, grinding, infiniteBlock, failMode, eventWins
                                );
                                playersCount++;
                            }

                            sender.sendMessage(Utils.translate("&cMigrated " + playersCount + " players"));

                            Parkour.getPluginLogger().info("Attempting migration of levels.yml to levels table");
                            Set<String> levelNames = levelsConfig.getKeys(false);
                            int levelCount = 0;

                            for (String levelName : levelNames)
                            {
                                boolean broadcastComp = levelsConfig.getBoolean(levelName + ".broadcast_completion", false);
                                int broadcast = broadcastComp ? 1 : 0;

                                boolean liquidReset = levelsConfig.getBoolean(levelName + ".liquid_reset_players", false);
                                int liquid = liquidReset ? 1 : 0;

                                int tcEnabled = levelsConfig.getBoolean(levelName + ".tc") ? 1 : 0;

                                LevelType type = LevelType.NORMAL;

                                boolean ascendance = levelsConfig.getBoolean(levelName + ".ascendance", false);

                                if (ascendance)
                                    type = LevelType.ASCENDANCE;

                                boolean dropper = levelsConfig.getBoolean(levelName + ".dropper", false);

                                if (dropper)
                                    type = LevelType.DROPPER;

                                boolean elytra = levelsConfig.getBoolean(levelName + ".elytra", false);

                                if (elytra)
                                    type = LevelType.ELYTRA;

                                boolean rankup = levelsConfig.getBoolean(levelName + ".rankup_level", false);

                                if (rankup)
                                    type = LevelType.RANKUP;

                                boolean race = levelsConfig.isConfigurationSection(levelName + ".race");

                                if (race)
                                    type = LevelType.RACE;

                                boolean isEvent = levelsConfig.isSet(levelName + ".event");

                                if (isEvent)
                                {
                                    EventType eventType = EventType.valueOf(levelsConfig.getString(levelName + ".event"));

                                    // simple switch case
                                    switch (eventType)
                                    {
                                        case ASCENT:
                                            type = LevelType.EVENT_ASCENT;
                                            break;
                                        case MAZE:
                                            type = LevelType.EVENT_MAZE;
                                            break;
                                        case PVP:
                                            type = LevelType.EVENT_PVP;
                                            break;
                                        case FALLING_ANVIL:
                                            type = LevelType.EVENT_FALLING_ANVIL;
                                            break;
                                        case RISING_WATER:
                                            type = LevelType.EVENT_RISING_WATER;
                                            break;
                                    }
                                }

                                DatabaseQueries.runQuery(
                                        "INSERT INTO " + DatabaseManager.LEVELS_TABLE + " " +
                                                "(name, type, tc, broadcast, liquid_reset) VALUES " +
                                                "(?,?,?,?,?)",
                                                levelName, type.name(), tcEnabled, broadcast, liquid
                                );

                                String title = levelsConfig.getString(levelName + ".title", null);

                                if (title != null)
                                    DatabaseQueries.runQuery("UPDATE " + DatabaseManager.LEVELS_TABLE + " SET title=? WHERE name=?", title, levelName);

                                String requiredPermission = levelsConfig.getString(levelName + ".required_permission_node", null);

                                if (requiredPermission != null)
                                    DatabaseQueries.runQuery("UPDATE " + DatabaseManager.LEVELS_TABLE + " SET required_permission=? WHERE name=?", requiredPermission, levelName);

                                int diff = levelsConfig.getInt(levelName + ".difficulty", -1);

                                if (diff > -1)
                                    DatabaseQueries.runQuery("UPDATE " + DatabaseManager.LEVELS_TABLE + " SET difficulty=? WHERE name=?", diff, levelName);

                                int respawn = levelsConfig.getInt(levelName + ".respawn_y", -1);

                                if (respawn > -1)
                                    DatabaseQueries.runQuery("UPDATE " + DatabaseManager.LEVELS_TABLE + " SET respawn_y=? WHERE name=?", respawn, levelName);

                                int max = levelsConfig.getInt(levelName + ".max_completions", -1);

                                if (max > -1)
                                    DatabaseQueries.runQuery("UPDATE " + DatabaseManager.LEVELS_TABLE + " SET max_completions=? WHERE name=?", max, levelName);

                                int price = levelsConfig.getInt(levelName + ".price", -1);

                                if (price > -1)
                                    DatabaseQueries.runQuery("UPDATE " + DatabaseManager.LEVELS_TABLE + " SET price=? WHERE name=?", price, levelName);


                                String rewardQuery = "SELECT reward FROM levels WHERE level_name='" + levelName + "'";

                                PreparedStatement rewardStatement = connection.prepareStatement(rewardQuery);
                                results = rewardStatement.executeQuery();

                                while (results.next())
                                {
                                    int reward = results.getInt("reward");
                                    if (reward > 0)
                                        DatabaseQueries.runQuery("UPDATE " + DatabaseManager.LEVELS_TABLE + " SET reward=? WHERE name=?", reward, levelName);
                                }

                                levelCount++;


                            }

                            sender.sendMessage(Utils.translate("&cMigrated " + levelCount + " levels"));

                            Parkour.getPluginLogger().info("Enabling foreign key checks");
                            DatabaseQueries.runQuery("SET foreign_key_checks = 1");
                            close();
                            Parkour.getPluginLogger().info("Closing database connection");
                        }
                    }
                    catch (SQLException exception)
                    {
                        exception.printStackTrace();
                    }
                }
            }.runTaskAsynchronously(Parkour.getPlugin());
        }
        return false;
    }

    private void open()
    {
        String dbPath = "database";

        String username = settings.getString(dbPath + ".username");
        String password = settings.getString(dbPath + ".password");

        String url = "jdbc:mysql://localhost:3306/parkour_dev_stats?autoReconnect=true&allowMultiQueries=true&useSSL=false";

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

    private void close()
    {
        try
        {
            if (!connection.isClosed())
                connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
