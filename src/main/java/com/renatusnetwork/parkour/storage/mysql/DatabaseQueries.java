package com.renatusnetwork.parkour.storage.mysql;

import com.renatusnetwork.parkour.Parkour;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseQueries {

    // Data parsing
    private static Map<String, String> resultSetToMap(ResultSet resultSet) {
        try {
            ResultSetMetaData md = resultSet.getMetaData();
            Map<String, String> results = new HashMap<>();

            int columns = md.getColumnCount();
            for (int i = 1; i <= columns; ++i)
                results.put(md.getColumnName(i), resultSet.getString(i));

            return results;
        } catch (SQLException exception) {
            Parkour.getPluginLogger().severe("ERROR: Occurred within DatabaseQueries.resultSetTomap()");
            Parkour.getPluginLogger().severe("ERROR:   printing StackTrace=");
            exception.printStackTrace();
        }

        return null;
    }

    public static List<Map<String, String>> getResults(String tableName, String selection, String trailingSQL, Object... parameters) {
        List<Map<String, String>> finalResults = new ArrayList<>();

        String query = "SELECT " + selection + " FROM " + tableName;
        if (!trailingSQL.isEmpty())
            query = query + " " + trailingSQL;

        try {
            PreparedStatement statement = Parkour.getDatabaseManager().get().get().prepareStatement(query);

            // secure
            for (int i = 0; i < parameters.length; i++)
                statement.setObject(i + 1, parameters[i]); // who knows why it starts at 1

            ResultSet results = statement.executeQuery();

            while (results.next())
                finalResults.add(resultSetToMap(results));
        } catch (SQLException exception) {
            Parkour.getPluginLogger().severe(
                    "ERROR: Occurred within DatabaseQueries.getResults(" + tableName + ", " + trailingSQL + ")"
            );
            Parkour.getPluginLogger().severe("ERROR:  query='" + query + "'");
            Parkour.getPluginLogger().severe("ERROR:   exception=" + exception.getLocalizedMessage());
        }

        return finalResults;
    }

    public static ResultSet getRawResults(String query)
    {
        try {
            PreparedStatement statement = Parkour.getDatabaseManager().get().get().prepareStatement(query);
            return statement.executeQuery();

        } catch (SQLException exception) {
            Parkour.getPluginLogger().severe(
                    "ERROR: Occurred within DatabaseQueries.getRawResults(" + query + ")"
            );
            Parkour.getPluginLogger().severe("ERROR:  query='" + query + "'");
            Parkour.getPluginLogger().severe("ERROR:   exception=" + exception.getLocalizedMessage());
        }

        return null;
    }
}
