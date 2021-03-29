package com.parkourcraft.parkour.data.plots;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.storage.mysql.DatabaseQueries;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Plots_DB {

    public static List<String> getPlotCenters() {

        // get all plots from database
        List<Map<String, String>> results = DatabaseQueries.getResults(
                "plots",
                "uuid, center_x, center_z", "");

        List<String> tempList = new ArrayList<>();
        for (Map<String, String> result : results)
            tempList.add(result.get("center_x") + ":" + result.get("center_z"));

        return tempList;
    }

    public static boolean hasPlot(String UUID) {
        List<Map<String, String>> results = DatabaseQueries.getResults(
                "plots",
                "*", " WHERE uuid='" + UUID + "'");

        if (!results.isEmpty())
            return true;
        return false;
    }

    public static String getPlotCenter(String UUID) {
        if (hasPlot(UUID)) {
            List<Map<String, String>> results = DatabaseQueries.getResults(
                    "plots",
                    "center_x, center_z", " WHERE uuid='" + UUID + "'");

            for (Map<String, String> result : results)
                return result.get("center_x") + ":" + result.get("center_z");
        }
        return null;
    }

    public static void addPlot(Player player, Location loc) {
        Parkour.getDatabaseManager().run("INSERT INTO plots " +
                "(uuid, player_name, center_x, center_z)" +
                " VALUES ('" +
                player.getUniqueId().toString() + "','" +
                player.getName() + "','" +
                loc.getBlockX() + "','" +
                loc.getBlockZ() +
                "')"
        );
    }

    public static void removePlot(Player player) {
        Parkour.getDatabaseManager().add("DELETE FROM plots " +
                "WHERE uuid='" + player.getUniqueId().toString() + "'");
    }
}
