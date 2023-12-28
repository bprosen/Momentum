package com.renatusnetwork.parkour.data.levels;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.SettingsManager;
import com.renatusnetwork.parkour.data.locations.LocationManager;
import com.renatusnetwork.parkour.storage.mysql.DatabaseManager;
import com.renatusnetwork.parkour.storage.mysql.DatabaseQueries;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Location;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class LevelsDB {

    public static HashMap<String, Level> getLevels()
    {
        List<Map<String, String>> results = DatabaseQueries.getResults(DatabaseManager.LEVELS_TABLE, "*", "");
        HashMap<String, Level> levels = new HashMap<>();

        for (Map<String, String> result : results)
        {
            String levelName = result.get("name");

            // set core details
            Level level = new Level(levelName);
            level.setReward(Integer.parseInt(result.get("reward")));
            level.setPrice(Integer.parseInt(result.get("price")));
            level.setTitle(Utils.translate(result.get("title")));

            // settings
            level.setRequiredPermission(result.get("required_permission"));
            level.setRequiredRank(result.get("required_rank"));
            level.setRespawnY(Integer.parseInt(result.get("respawn_y")));
            level.setMaxCompletions(Integer.getInteger("max_completions"));
            level.setLevelType(LevelType.valueOf(result.get("type").toUpperCase()));

            // switches
            level.setCooldown(Integer.parseInt(result.get("cooldown")) == 1);
            level.setBroadcast(Integer.parseInt(result.get("broadcast")) == 1);
            level.setLiquidResetPlayer(Integer.parseInt(result.get("liquid_reset")) == 1);
            level.setNew(Integer.parseInt(result.get("new")) == 1);
            level.setHasMastery(Integer.parseInt(result.get("has_mastery")) == 1);

            // spawns
            LocationManager locationManager = Parkour.getLocationManager();

            Location spawnLoc = locationManager.get(SettingsManager.LEVEL_SPAWN_FORMAT.replace("%level%", levelName));
            Location completionLoc = locationManager.get(SettingsManager.LEVEL_COMPLETION_FORMAT.replace("%level%", levelName));

            // add spawn loc
            if (spawnLoc != null)
                level.setStartLocation(spawnLoc);
            else
                level.setStartLocation(locationManager.getLobbyLocation());

            // add completion loc
            if (completionLoc != null)
                level.setCompletionLocation(completionLoc);
            else
                level.setCompletionLocation(locationManager.getLobbyLocation());

            // TODO: implement the rest of the spawns... like races/events

            // 4 seperate queries... not the greatest but it keeps our code clean and due to our indexes it does still happen quite fast
            level.setCommands(getCompletionCommands(levelName));
            level.setRequiredLevels(getRequiredLevels(levelName));
            level.setRatings(getLevelRatings(levelName));
            level.setPotionEffects(getPotionEffects(levelName));

            levels.put(levelName, level);
        }

        return levels;
    }

    public static List<String> getRequiredLevels(String levelName)
    {
        List<Map<String, String>> results = DatabaseQueries.getResults(
            DatabaseManager.LEVEL_REQUIRED_LEVELS_TABLE + " lrl",
             "*",
            "WHERE level_name=?", levelName
        );

        List<String> requiredLevels = new ArrayList<>();

        for (Map<String, String> result : results)
            requiredLevels.add(result.get("required_level_name"));

        return requiredLevels;
    }

    public static List<String> getCompletionCommands(String levelName)
    {
        List<Map<String, String>> results = DatabaseQueries.getResults(
                DatabaseManager.LEVEL_COMPLETIONS_COMMANDS_TABLE,
                "*",
                "WHERE level_name=?", levelName
        );

        List<String> commands = new ArrayList<>();

        for (Map<String, String> result : results)
            commands.add(result.get("command"));

        return commands;
    }

    public static List<PotionEffect> getPotionEffects(String levelName)
    {
        List<Map<String, String>> results = DatabaseQueries.getResults(
                DatabaseManager.LEVEL_POTION_EFFECTS_TABLE,
                "*",
               "WHERE level_name=?", levelName
        );

        List<PotionEffect> potionEffects = new ArrayList<>();

        for (Map<String, String> result : results)
        {
            PotionEffectType type = PotionEffectType.getByName(result.get("type"));
            int duration = Integer.parseInt(result.get("duration"));
            int amplifier = Integer.parseInt(result.get("amplifier"));

            potionEffects.add(new PotionEffect(type, duration, amplifier, true, true));
        }

        return potionEffects;
    }

    public static HashMap<String, Integer> getLevelRatings(String levelName)
    {
        List<Map<String, String>> results = DatabaseQueries.getResults(
                DatabaseManager.LEVEL_RATINGS_TABLE + " lr",
                "*",
                "JOIN " + DatabaseManager.PLAYERS_TABLE + " p ON p.uuid=lr.uuid WHERE level_name=?", levelName
        );

        HashMap<String, Integer> ratings = new HashMap<>();

        for (Map<String, String> result : results)
            ratings.put(result.get("p.name"), Integer.parseInt(result.get("rating")));

        return ratings;
    }

    public static void insertLevel(String levelName)
    {
        DatabaseQueries.runAsyncQuery("INSERT INTO " + DatabaseManager.LEVELS_TABLE + "(name) VALUES (?)", levelName);
    }

    public static void updateName(String levelName, String newLevelName)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.LEVELS_TABLE + " SET name=? WHERE name=?", newLevelName, levelName);
    }

    public static long getGlobalCompletions()
    {
        List<Map<String, String>> globalResults = DatabaseQueries.getResults(DatabaseManager.LEVEL_COMPLETIONS_TABLE,
                "COUNT(*) AS total_completions", "");

        return Long.parseLong(globalResults.get(0).get("total_completions"));
    }

    public static long getCompletionsBetweenDates(String levelName, String start, String end)
    {
        List<Map<String, String>> globalResults = DatabaseQueries.getResults(DatabaseManager.LEVEL_COMPLETIONS_TABLE,
                "COUNT(*) AS total_completions",
                " WHERE name=? AND completion_date BETWEEN ? AND ?", levelName, start, end);

        return Long.parseLong(globalResults.get(0).get("total_completions"));
    }

    public static void updateLiquidReset(String levelName)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.LEVELS_TABLE + " SET liquid_reset=NOT liquid_reset WHERE name=?", levelName);
    }

    public static void updateReward(String levelName, int reward)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.LEVELS_TABLE + " SET reward=? WHERE name=?", reward, levelName);
    }

    public static void updatePrice(String levelName, int price)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.LEVELS_TABLE + " SET price=? WHERE name=?", price, levelName);
    }

    public static void updateTitle(String levelName, String title)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.LEVELS_TABLE + " SET title=? WHERE name=?", title, levelName);
    }

    public static void updateRespawnY(String levelName, int respawnY)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.LEVELS_TABLE + " SET respawn_y=? WHERE name=?", respawnY, levelName);
    }
    public static void updateMaxCompletions(String levelName, int maxCompletions)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.LEVELS_TABLE + " SET max_completions=? WHERE name=?", maxCompletions, levelName);
    }

    public static void updateBroadcast(String levelName)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.LEVELS_TABLE + " SET broadcast=NOT broadcast WHERE name=?", levelName);
    }

    public static void updateNew(String levelName)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.LEVELS_TABLE + " SET new=NOT new WHERE name=?", levelName);
    }

    public static void updateCooldown(String levelName)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.LEVELS_TABLE + " SET cooldown=NOT cooldown WHERE name=?", levelName);
    }

    public static void updateDifficulty(String levelName, int difficulty)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.LEVELS_TABLE + " SET difficulty=? WHERE name=?", difficulty, levelName);
    }

    public static void setLevelType(String levelName, LevelType levelType)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.LEVELS_TABLE + " SET type=? WHERE name=?", levelType.name(), levelName);
    }

    public static void insertLevelRequired(String levelName, String requiredLevelName)
    {
        DatabaseQueries.runAsyncQuery(
                "INSERT INTO " + DatabaseManager.LEVEL_REQUIRED_LEVELS_TABLE + " (level_name, required_level_name) VALUES (?,?)",
                levelName, requiredLevelName
        );
    }

    public static void removeLevelRequired(String levelName, String requiredLevelName)
    {
        DatabaseQueries.runAsyncQuery(
                "DELETE FROM " + DatabaseManager.LEVEL_REQUIRED_LEVELS_TABLE + " WHERE level_name=? AND required_level_name=?",
                levelName, requiredLevelName
        );
    }

    public static void removeLevel(String levelName)
    {
        Connection connection = Parkour.getDatabaseManager().getConnection().get();

        try
        {
            connection.setAutoCommit(false);

            DatabaseQueries.runAsyncQuery("DELETE FROM " + DatabaseManager.LEVELS_TABLE + " WHERE name='" + levelName + "'");

            // this is just for extra clean up since they are not foreign key relationships
            DatabaseQueries.runAsyncQuery("DELETE FROM " + DatabaseManager.LOCATIONS_TABLE + " WHERE name='" + levelName + "-spawn'");
            DatabaseQueries.runAsyncQuery("DELETE FROM " + DatabaseManager.LOCATIONS_TABLE + " WHERE name='" + levelName + "-completion'");

            // commit
            connection.commit();

            // fix auto commit
            connection.setAutoCommit(true);
        }
        catch (SQLException exception)
        {
            try
            {
                connection.rollback();
                Parkour.getPluginLogger().info("Transaction failed on LevelsDB.removeLevel(), rolling back");
                exception.printStackTrace();
            }
            catch (SQLException rollbackException)
            {
                Parkour.getPluginLogger().info("Failure rolling back transaction on LevelsDB.removeLevel()");
                rollbackException.printStackTrace();
            }
        }
    }
}
