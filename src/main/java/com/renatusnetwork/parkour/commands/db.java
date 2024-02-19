package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.events.types.EventType;
import com.renatusnetwork.parkour.data.levels.LevelType;
import com.renatusnetwork.parkour.data.perks.PerksArmorType;
import com.renatusnetwork.parkour.storage.mysql.DatabaseManager;
import com.renatusnetwork.parkour.storage.mysql.DatabaseQueries;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.List;
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
                        FileConfiguration perksConfig = null;
                        FileConfiguration locationsConfig = null;
                        boolean cannotContinue = false;

                        try
                        {
                            File oldLevels = new File(Parkour.getPlugin().getDataFolder(), "levels.yml");
                            levelsConfig = new YamlConfiguration();
                            levelsConfig.load(oldLevels);

                            File oldPerks = new File(Parkour.getPlugin().getDataFolder(), "perks.yml");
                            perksConfig = new YamlConfiguration();
                            perksConfig.load(oldPerks);

                            File locationPerks = new File(Parkour.getPlugin().getDataFolder(), "locations.yml");
                            locationsConfig = new YamlConfiguration();
                            locationsConfig.load(locationPerks);
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

                            /*
                                Migration handling players table
                             */
                            Parkour.getPluginLogger().info("Attempting migration of players table");

                            String playersQuery = "SELECT * FROM players LEFT JOIN clans ON clans.clan_id=players.clan_id";

                            PreparedStatement statement = connection.prepareStatement(playersQuery);
                            ResultSet results = statement.executeQuery();
                            int playersCount = 0;
                            while (results.next())
                            {
                                String uuid = results.getString("uuid");
                                String name = results.getString("player_name");
                                int coins = (int) results.getDouble("coins");
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

                            /*
                                Tables handling level migration here
                             */
                            Parkour.getPluginLogger().info("Attempting migration of levels.yml to levels table");
                            Set<String> levelNames = levelsConfig.getKeys(false);
                            int levelCount = 0;
                            long creationBase = System.currentTimeMillis();

                            for (String levelName : levelNames)
                            {

                                String levelIDQuery = "SELECT level_id FROM levels WHERE level_name='" + levelName + "'";

                                PreparedStatement statement2 = connection.prepareStatement(levelIDQuery);
                                ResultSet results2 = statement2.executeQuery();
                                int levelID = -1;

                                while (results2.next())
                                {
                                    levelID = results2.getInt("level_id");
                                    break;
                                }

                                long newBase = creationBase;
                                if (levelID > -1)
                                    newBase += levelID;
                                else
                                    newBase = System.currentTimeMillis();

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
                                                "(name, creation_date, type, tc, broadcast, liquid_reset) VALUES " +
                                                "(?,?,?,?,?,?)",
                                                levelName, newBase, type.name(), tcEnabled, broadcast, liquid
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

                                if (levelsConfig.isSet(levelName + ".required_levels"))
                                {
                                    List<String> requiredLevels = levelsConfig.getStringList(levelName + ".required_levels");
                                    for (String requiredLevel : requiredLevels)
                                        DatabaseQueries.runQuery(
                                                "INSERT INTO " + DatabaseManager.LEVEL_REQUIRED_LEVELS_TABLE + "(level_name, required_level_name) VALUES (?,?)",
                                                    levelName, requiredLevel
                                        );
                                }

                                if (levelsConfig.isSet(levelName + ".commands"))
                                {
                                    List<String> commands = levelsConfig.getStringList(levelName + ".commands");
                                    for (String command : commands)
                                        DatabaseQueries.runQuery(
                                                "INSERT INTO " + DatabaseManager.LEVEL_COMPLETION_COMMANDS_TABLE + "(level_name, command) VALUES (?,?)",
                                                levelName, command
                                        );
                                }

                                if (levelsConfig.isConfigurationSection(levelName + ".potion_effects"))
                                {
                                    for (int i = 1;; i++)
                                    {
                                        if (levelsConfig.isConfigurationSection(levelName + ".potion_effects." + i))
                                        {
                                            String potionType = levelsConfig.getString(levelName + ".potion_effects." + i + ".type").toUpperCase();
                                            int amplifier = levelsConfig.getInt(levelName + ".potion_effects." + i + ".amplifier");
                                            int duration = levelsConfig.getInt(levelName + ".potion_effects." + i + ".duration");

                                            DatabaseQueries.runQuery(
                                                    "INSERT INTO " + DatabaseManager.LEVEL_POTION_EFFECTS_TABLE + " (level_name, type, amplifier, duration) VALUES (?,?,?,?)",
                                                    levelName, potionType, amplifier, duration
                                            );
                                        }
                                        else break;
                                    }
                                }
                                levelCount++;
                            }

                            String ratingsQuery = "SELECT * FROM ratings r JOIN levels l ON l.level_id=r.level_id";

                            PreparedStatement ratingsStatement = connection.prepareStatement(ratingsQuery);
                            results = ratingsStatement.executeQuery();
                            while (results.next())
                            {
                                DatabaseQueries.runQuery(
                                        "INSERT INTO " + DatabaseManager.LEVEL_RATINGS_TABLE + " (uuid, level_name, rating) VALUES (?,?,?)",
                                        results.getString("uuid"),
                                        results.getString("level_name"),
                                        results.getInt("rating")
                                );
                            }

                            String checkpointsQuery = "SELECT * FROM checkpoints";

                            PreparedStatement checkpointStatement = connection.prepareStatement(checkpointsQuery);
                            results = checkpointStatement.executeQuery();
                            while (results.next())
                            {
                                DatabaseQueries.runQuery(
                                        "INSERT INTO " + DatabaseManager.LEVEL_CHECKPOINTS_TABLE + " (uuid, level_name, world, x, y, z) VALUES (?,?,?,?,?,?)",
                                        results.getString("uuid"),
                                        results.getString("level_name"),
                                        results.getString("world"),
                                        results.getInt("x"),
                                        results.getInt("y"),
                                        results.getInt("z")
                                );
                            }

                            String purchasesTable = "SELECT * FROM bought_levels";

                            PreparedStatement purchasesStatement = connection.prepareStatement(purchasesTable);
                            results = purchasesStatement.executeQuery();
                            while (results.next())
                            {
                                DatabaseQueries.runQuery(
                                        "INSERT INTO " + DatabaseManager.LEVEL_PURCHASES_TABLE + " (uuid, level_name) VALUES (?,?)",
                                        results.getString("uuid"),
                                        results.getString("level_name")
                                );
                            }

                            String savesTable = "SELECT * FROM saves";

                            PreparedStatement savesStatement = connection.prepareStatement(savesTable);
                            results = savesStatement.executeQuery();
                            while (results.next())
                            {
                                DatabaseQueries.runQuery(
                                        "INSERT INTO " + DatabaseManager.LEVEL_SAVES_TABLE + " (uuid, level_name, world, x, y, z, yaw, pitch) VALUES (?,?,?,?,?,?,?,?)",
                                        results.getString("uuid"),
                                        results.getString("level_name"),
                                        results.getString("world"),
                                        results.getDouble("x"),
                                        results.getDouble("y"),
                                        results.getDouble("z"),
                                        results.getDouble("yaw"),
                                        results.getDouble("pitch")
                                );
                            }


                            sender.sendMessage(Utils.translate("&cMigrated " + levelCount + " levels"));

                            /*
                                Migration handling level completions
                             */
                            Parkour.getPluginLogger().info("Attempting migration of completions table");
                            String completionsQuery = "SELECT *, (UNIX_TIMESTAMP(completion_date) * 1000) AS date FROM completions c JOIN players p ON p.player_id=c.player_id JOIN levels l ON l.level_id=c.level_id";

                            int completionCounter = 0;

                            PreparedStatement rewardStatement = connection.prepareStatement(completionsQuery);
                            results = rewardStatement.executeQuery();


                            int duplicates = 0;
                            while (results.next())
                            {
                                String uuid = results.getString("uuid");
                                long date = results.getLong("date");
                                long oldDate = date;

                                int timeTaken = results.getInt("time_taken");

                                if (timeTaken > 0)
                                {
                                    while (!DatabaseQueries.runQuery(
                                            "INSERT INTO " + DatabaseManager.LEVEL_COMPLETIONS_TABLE + " (uuid, level_name, completion_date, time_taken) VALUES (?,?,?,?)",
                                            results.getString("uuid"),
                                            results.getString("level_name"),
                                            date,
                                            results.getInt("time_taken")
                                    ))
                                        date++;
                                }
                                else
                                    while (!DatabaseQueries.runQuery(
                                            "INSERT INTO " + DatabaseManager.LEVEL_COMPLETIONS_TABLE + " (uuid, level_name, completion_date) VALUES (?,?,?)",
                                            results.getString("uuid"),
                                            results.getString("level_name"),
                                            date
                                    ))
                                        date++;

                                if (oldDate != date)
                                {
                                    Parkour.getPluginLogger().info("Duplicate completion from forced found for " + uuid + ", changing date from " + oldDate + " -> " + date);
                                    duplicates++;
                                }

                                completionCounter++;
                            }

                            sender.sendMessage(Utils.translate("&cMigrated " + completionCounter + " completions (" + duplicates + " duplicates fixed)"));

                            /*
                                Clans migration system
                             */
                            Parkour.getPluginLogger().info("Attempting migration of clans table");

                            String clansTable = "SELECT * FROM clans c JOIN players p ON p.player_id=c.owner_player_id";
                            int clanCounter = 0;
                            PreparedStatement clansStatement = connection.prepareStatement(clansTable);
                            results = clansStatement.executeQuery();
                            while (results.next())
                            {
                                DatabaseQueries.runQuery(
                                        "INSERT INTO " + DatabaseManager.CLANS_TABLE + " (tag, owner_uuid, xp, level, total_xp) VALUES (?,?,?,?,?)",
                                        results.getString("clan_tag"),
                                        results.getString("uuid"),
                                        results.getInt("clan_xp"),
                                        results.getInt("clan_level"),
                                        results.getInt("total_gained_xp")
                                );
                                clanCounter++;
                            }
                            sender.sendMessage(Utils.translate("&cMigrated " + clanCounter + " clans"));

                            /*
                                Plots migration system
                             */

                            Parkour.getPluginLogger().info("Attempting migration of plots table");
                            String plotsTable = "SELECT * FROM plots";
                            int plotsCounter = 0;
                            PreparedStatement plotsStatement = connection.prepareStatement(plotsTable);
                            results = plotsStatement.executeQuery();

                            while (results.next())
                            {
                                int plotID = results.getInt("plot_id");

                                DatabaseQueries.runQuery(
                                        "INSERT INTO " + DatabaseManager.PLOTS_TABLE + " (id, owner_uuid, center_x, center_z, submitted) VALUES (?,?,?,?,?)",
                                        plotID,
                                        results.getString("uuid"),
                                        results.getInt("center_x"),
                                        results.getInt("center_z"),
                                        Boolean.parseBoolean(results.getString("submitted")) ? 1 : 0
                                );

                                String trusteds = results.getString("trusted_players");

                                if (trusteds.contains(":"))
                                {
                                    String[] trustedPlayers = trusteds.split(":");
                                    for (String trustedPlayerName : trustedPlayers)
                                    {
                                        String playerUUID = "SELECT uuid FROM players WHERE player_name='" + trustedPlayerName + "'";

                                        PreparedStatement playerNameStatement = connection.prepareStatement(playerUUID);
                                        ResultSet trustedResults = playerNameStatement.executeQuery();

                                        if (trustedResults.next())
                                            DatabaseQueries.runQuery(
                                                    "INSERT INTO " + DatabaseManager.PLOTS_TRUSTED_PLAYERS_TABLE + " (plot_id, trusted_uuid) VALUES (?,?)",
                                                    plotID, trustedResults.getString("uuid")
                                            );

                                    }
                                }
                                plotsCounter++;
                            }
                            sender.sendMessage(Utils.translate("&cMigrated " + plotsCounter + " plots"));

                            /*
                                Perks migration system
                             */
                            Parkour.getPluginLogger().info("Attempting migration of perks table");

                            Set<String> perks = perksConfig.getKeys(false);
                            int perksCounter = 0;
                            for (String perkName : perks)
                            {

                                DatabaseQueries.runQuery("INSERT INTO " + DatabaseManager.PERKS_TABLE + " (name) VALUES (?)", perkName);

                                String title = perksConfig.getString(perkName + ".title");
                                if (title != null)
                                    DatabaseQueries.runQuery("UPDATE " + DatabaseManager.PERKS_TABLE + " SET title=? WHERE name=?", title, perkName);

                                int price = perksConfig.getInt(perkName + ".price", -1);
                                if (price > -1)
                                    DatabaseQueries.runQuery("UPDATE " +  DatabaseManager.PERKS_TABLE + " SET price=? WHERE name=?", price, perkName);

                                String requiredPermission = perksConfig.getString(perkName + ".required_permissions");
                                if (requiredPermission != null)
                                    DatabaseQueries.runQuery("UPDATE " + DatabaseManager.PERKS_TABLE + " SET required_permission=? WHERE name=?", requiredPermission, perkName);

                                String infiniteBlockString = perksConfig.getString(perkName + ".infinite_block");
                                if (infiniteBlockString != null)
                                    DatabaseQueries.runQuery("UPDATE " + DatabaseManager.PERKS_TABLE + " SET infinite_block=? WHERE name=?", infiniteBlockString.toUpperCase(), perkName);

                                if (perksConfig.isConfigurationSection(perkName + ".requirements"))
                                {
                                    List<String> levelRequirements = perksConfig.getStringList(perkName + ".requirements");
                                    for (String levelName : levelRequirements)
                                    {
                                        DatabaseQueries.runQuery(
                                                "INSERT INTO " + DatabaseManager.PERKS_LEVEL_REQUIREMENTS_TABLE + " (perk_name, level_name) VALUES (?,?)",
                                                perkName, levelName
                                        );
                                    }
                                }


                                if (perksConfig.isConfigurationSection(perkName + ".items"))
                                {
                                    String[] types = {"head", "chest", "leg", "feet"};

                                    for (String type : types)
                                    {
                                        if (perksConfig.isConfigurationSection(perkName + ".items." + type))
                                        {
                                            PerksArmorType newType = null;
                                            switch (type)
                                            {
                                                case "head":
                                                    newType = PerksArmorType.HELMET;
                                                    break;
                                                case "chest":
                                                    newType = PerksArmorType.CHESTPLATE;
                                                    break;
                                                case "leg":
                                                    newType = PerksArmorType.LEGGINGS;
                                                    break;
                                                case "feet":
                                                    newType = PerksArmorType.BOOTS;
                                                    break;
                                            }

                                            Material itemMaterial = Material.matchMaterial(perksConfig.getString(perkName + ".items." + type + ".material"));
                                            DatabaseQueries.runQuery("INSERT INTO " + DatabaseManager.PERKS_ARMOR_TABLE + "(perk_name,armor_piece,material) VALUES (?,?,?)",
                                                    perkName, newType.name(), itemMaterial.name());

                                            int itemType = perksConfig.getInt(perkName +".items." + type + ".type", 0);

                                            if (itemType > 0)
                                                DatabaseQueries.runQuery("UPDATE " + DatabaseManager.PERKS_ARMOR_TABLE + " SET type=? WHERE perk_name=?", itemType, perkName);

                                            String itemTitle = perksConfig.getString(perkName + ".items." + type + ".title");

                                            if (itemTitle != null)
                                                DatabaseQueries.runQuery("UPDATE " + DatabaseManager.PERKS_ARMOR_TABLE + " SET title=? WHERE perk_name=?", title, perkName);

                                            boolean glow = perksConfig.getBoolean(perkName + ".items." + type +".glow", false);

                                            if (glow)
                                                DatabaseQueries.runQuery("UPDATE " + DatabaseManager.PERKS_ARMOR_TABLE + " SET glow=1 WHERE perk_name=?", perkName);
                                        }
                                    }
                                }
                                perksCounter++;
                            }
                            sender.sendMessage(Utils.translate("&cMigrated " + perksCounter + " perks"));

                            String perksTable = "SELECT uuid,p.perk_name AS perk_name FROM ledger l JOIN perks p ON p.perk_id=l.perk_id JOIN players pl ON pl.player_id=l.player_id";
                            PreparedStatement perksStatement = connection.prepareStatement(perksTable);
                            results = perksStatement.executeQuery();

                            while (results.next())
                            {
                                DatabaseQueries.runQuery("INSERT INTO " + DatabaseManager.PERKS_BOUGHT_TABLE + " (uuid, perk_name) VALUES(?,?)",
                                        results.getString("uuid"), results.getString("perk_name"));
                            }

                            /*
                                Perks migration system
                             */
                            Parkour.getPluginLogger().info("Attempting migration of locations");
                            Set<String> locationNames = locationsConfig.getKeys(false);
                            int locationCounter = 0;

                            for (String locationName : locationNames)
                            {
                                String world = locationsConfig.getString(locationName + ".world");
                                double x = locationsConfig.getDouble(locationName + ".x");
                                double y = locationsConfig.getDouble(locationName + ".y");
                                double z = locationsConfig.getDouble(locationName + ".z");
                                float yaw = (float) locationsConfig.getDouble(locationName + ".yaw");
                                float pitch = (float) locationsConfig.getDouble(locationName + ".pitch");

                                DatabaseQueries.runQuery("INSERT INTO " + DatabaseManager.LOCATIONS_TABLE + " (name,world,x,y,z,yaw,pitch) VALUES (?,?,?,?,?,?,?)",
                                        locationName, world, x, y, z, yaw, pitch);
                                locationCounter++;
                            }
                            sender.sendMessage(Utils.translate("&cMigrated " + locationCounter + " locations"));

                            sender.sendMessage(Utils.translate("&cCompleted migration"));
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
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
}
