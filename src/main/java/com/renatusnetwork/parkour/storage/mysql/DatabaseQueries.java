package com.renatusnetwork.parkour.storage.mysql;

import com.avaje.ebean.Transaction;
import com.renatusnetwork.parkour.Parkour;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.util.*;

public class DatabaseQueries
{

    public static List<Map<String, String>> getResults(String tableName, String selection, String trailingSQL, Object... parameters)
    {
        List<Map<String, String>> finalResults = new ArrayList<>();

        String query = "SELECT " + selection + " FROM " + tableName;
        if (!trailingSQL.isEmpty())
            query = query + " " + trailingSQL;

        try
        {
            PreparedStatement statement = Parkour.getDatabaseManager().getConnection().get().prepareStatement(query);

            // secure
            for (int i = 0; i < parameters.length; i++)
                statement.setObject(i + 1, parameters[i]); // who knows why it starts at 1

            ResultSet results = statement.executeQuery();

            while (results.next())
            {
                // parse results
                ResultSetMetaData meta = results.getMetaData();

                HashMap<String, String> resultMap = new HashMap<>();

                int columns = meta.getColumnCount();

                for (int i = 1; i <= columns; ++i)
                    resultMap.put(meta.getColumnName(i), results.getString(i));

                finalResults.add(resultMap);
            }
        }
        catch (SQLException exception)
        {
            Parkour.getPluginLogger().info("Error in DatabaseQueries.getResults(" + tableName + ", " + trailingSQL + ")");
            Parkour.getPluginLogger().info("Query='" + query + "'");
            exception.printStackTrace();
        }

        return finalResults;
    }

    public static Map<String, String> getResult(String tableName, String selection, String trailingSQL, Object... parameters)
    {
        // this is a use case where we are using a primary key to get a single result, just cleaner code
        List<Map<String, String>> results = getResults(tableName, selection, trailingSQL, parameters);
        Map<String, String> empty = new HashMap<>();

        if (!results.isEmpty())
            return getResults(tableName, selection, trailingSQL, parameters).get(0);
        else
            return empty;
    }

    public static ResultSet getRawResults(String query, Object... parameters)
    {
        try
        {
            PreparedStatement statement = Parkour.getDatabaseManager().getConnection().get().prepareStatement(query);

            // secure
            for (int i = 0; i < parameters.length; i++)
                statement.setObject(i + 1, parameters[i]); // who knows why it starts at 1

            return statement.executeQuery();
        }
        catch (SQLException exception)
        {
            Parkour.getPluginLogger().info("Error in DatabaseQueries.getRawResults(" + query + ")");
            Parkour.getPluginLogger().info("Query='" + query + "'");
            exception.printStackTrace();
        }

        return null;
    }

    public static boolean runQuery(String sql, Object... parameters)
    {
        try
        {
            PreparedStatement statement = Parkour.getDatabaseManager().getConnection().get().prepareStatement(sql);

            // secure
            for (int i = 0; i < parameters.length; i++)
                statement.setObject(i + 1, parameters[i]); // who knows why it starts at 1

            statement.executeUpdate();
            statement.close();
            return true;
        }
        catch (SQLException exception)
        {
            Parkour.getPluginLogger().severe("Failed to run query: " + sql);
            Parkour.getPluginLogger().severe("Params: " + Arrays.toString(parameters));
            exception.printStackTrace();
            return false;
        }
    }

    public static void runAsyncQuery(String sql, Object... parameters)
    {
        new BukkitRunnable() {
            public void run() {
                runQuery(sql, parameters);
            }
        }.runTaskAsynchronously(Parkour.getPlugin());
    }
}
