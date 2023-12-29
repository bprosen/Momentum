package com.renatusnetwork.parkour.data.locations;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.ranks.Rank;
import com.renatusnetwork.parkour.storage.mysql.DatabaseManager;
import com.renatusnetwork.parkour.storage.mysql.DatabaseQueries;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationsDB
{
    public static HashMap<String, Location> loadLocations()
    {
        HashMap<String, Location> tempMap = new HashMap<>();
        List<Map<String, String>> results = DatabaseQueries.getResults(DatabaseManager.LOCATIONS_TABLE, "*", "");

        for (Map<String, String> result : results)
        {
            String locationName = result.get("name");
            tempMap.put(locationName, parseLocationFromResult(result));
        }
        return tempMap;
    }

    public static Location loadLocation(String locationName)
    {
        Map<String, String> result = DatabaseQueries.getResult(
                         DatabaseManager.LOCATIONS_TABLE,
                "*",
               "WHERE name=?",
                         locationName);

        return parseLocationFromResult(result);
    }

    public static Location parseLocationFromResult(Map<String, String> result)
    {
        World world = Bukkit.getWorld(result.get("world"));
        double x = Double.parseDouble(result.get("x"));
        double y = Double.parseDouble(result.get("y"));
        double z = Double.parseDouble(result.get("z"));
        float yaw = Float.parseFloat(result.get("yaw"));
        float pitch = Float.parseFloat(result.get("pitch"));

        return new Location(world, x, y, z, yaw, pitch);
    }

    public static void insertLocation(String locationName, Location location)
    {
        DatabaseQueries.runAsyncQuery(
                "INSERT INTO " + DatabaseManager.LOCATIONS_TABLE +
                "(name, world, x, y, z, yaw, pitch) VALUES" +
                "('" + locationName + "','" +
                       location.getWorld().getName() + "'," +
                       location.getX() + "," +
                       location.getY() + "," +
                       location.getZ() + "," +
                       location.getYaw() + "," +
                       location.getPitch() +
                ")");
    }

    public static void updateLocation(String locationName, Location location)
    {
        DatabaseQueries.runAsyncQuery(
                "UPDATE " + DatabaseManager.LOCATIONS_TABLE + " " +
                    "SET world='" + location.getWorld().getName() + "', " +
                    "x=" + location.getX() + ", " +
                    "y=" + location.getY() + ", " +
                    "z=" + location.getZ() + ", " +
                    "yaw=" + location.getYaw() + ", " +
                    "pitch=" + location.getPitch() + " " +
                    "WHERE name='" + locationName+ "'"
        );
    }

    public static void updateLocationName(String oldName, String newName)
    {
        DatabaseQueries.runAsyncQuery(
                "UPDATE " + DatabaseManager.LOCATIONS_TABLE + " " +
                    "SET name=? WHERE name=?",
                    newName, oldName
        );
    }

    public static void removeLocation(String locationName)
    {
        DatabaseQueries.runAsyncQuery("DELETE FROM " + DatabaseManager.LOCATIONS_TABLE + " WHERE name=?", locationName);
    }
}
