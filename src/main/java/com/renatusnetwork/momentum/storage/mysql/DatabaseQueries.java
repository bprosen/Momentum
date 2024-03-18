package com.renatusnetwork.momentum.storage.mysql;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.utils.TimeUtils;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.Bukkit;
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

        try (Connection connection = Momentum.getDatabaseManager().getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(query);

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
            Momentum.getPluginLogger().info("Error in DatabaseQueries.getResults(" + tableName + ", " + trailingSQL + ")");
            Momentum.getPluginLogger().info("Query='" + query + "'");
            exception.printStackTrace();
        }

        return finalResults;
    }

    public static Map<String, String> getResult(String tableName, String selection, String trailingSQL, Object... parameters)
    {
        // this is a use case where we are using a primary key to get a single result, just cleaner code
        List<Map<String, String>> results = getResults(tableName, selection, trailingSQL, parameters);
        Map<String, String> empty = new HashMap<>();

        return !results.isEmpty() ? getResults(tableName, selection, trailingSQL, parameters).get(0) : empty;
    }

    public static boolean runQuery(String sql, Object... parameters)
    {
        try (Connection connection = Momentum.getDatabaseManager().getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);

            // secure
            for (int i = 0; i < parameters.length; i++)
                statement.setObject(i + 1, parameters[i]); // who knows why it starts at 1

            statement.executeUpdate();
            return true;
        }
        catch (SQLException exception)
        {
            Momentum.getPluginLogger().severe("Failed to run query: " + sql);
            Momentum.getPluginLogger().severe("Params: " + Arrays.toString(parameters));
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
        }.runTaskAsynchronously(Momentum.getPlugin());
    }
}
