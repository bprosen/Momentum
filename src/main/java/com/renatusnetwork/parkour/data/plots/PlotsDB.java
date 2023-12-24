package com.renatusnetwork.parkour.data.plots;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.storage.mysql.DatabaseManager;
import com.renatusnetwork.parkour.storage.mysql.DatabaseQueries;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class PlotsDB {

    public static List<String> getPlotCenters()
    {

        // get all plots from database
        List<Map<String, String>> results = DatabaseQueries.getResults(
                DatabaseManager.PLOTS_TABLE,
                "owner_uuid, center_x, center_z", "");

        List<String> tempList = new ArrayList<>();
        for (Map<String, String> result : results)
            tempList.add(result.get("center_x") + ":" + result.get("center_z"));

        return tempList;
    }

    public static boolean hasPlot(String UUID)
    {
        List<Map<String, String>> results = DatabaseQueries.getResults(
                DatabaseManager.PLOTS_TABLE,
                "*", " WHERE owner_uuid='" + UUID + "'");

        return !results.isEmpty();
    }

    public static List<String> getTrustedUUIDs(String UUID) {
        List<String> trustedUUIDs = new ArrayList<>();

        if (hasPlot(UUID)) {
            List<Map<String, String>> results = DatabaseQueries.getResults(
                    DatabaseManager.PLOTS_TRUSTED_PLAYERS_TABLE + " tp",
                    "trusted_uuid",
                    "JOIN " + DatabaseManager.PLOTS_TABLE + "p ON tp.plot_id=p.id WHERE owner_uuid='" + UUID + "'");

            for (Map<String, String> result : results)
                trustedUUIDs.add(result.get("trusted_uuid"));
        }
        return trustedUUIDs;
    }

    public static void addTrustedPlayer(int plotID, Player trustedPlayer)
    {
        Parkour.getDatabaseManager().runAsyncQuery(
                "INSERT INTO " + DatabaseManager.PLOTS_TRUSTED_PLAYERS_TABLE + " (plot_id, trusted_uuid) " +
                    "VALUES (" + plotID + ",'" + trustedPlayer.getUniqueId().toString() + "')");
    }

    public static void removeTrustedPlayer(int plotID, Player trustedPlayer)
    {
        Parkour.getDatabaseManager().runAsyncQuery(
                "DELETE FROM " + DatabaseManager.PLOTS_TRUSTED_PLAYERS_TABLE + " WHERE plot_id=" + plotID + " AND trusted_uuid='" + trustedPlayer.getUniqueId().toString() + "'");
    }

    public static int getPlotID(Player player)
    {
        List<Map<String, String>> results = DatabaseQueries.getResults(DatabaseManager.PLOTS_TABLE, "id", "WHERE owner_uuid='" + player.getUniqueId().toString() + "'");

        return results.isEmpty() ? -1 : Integer.parseInt(results.get(0).get("id"));
    }

    public static void addPlot(Player player, Location loc) {
        Parkour.getDatabaseManager().runQuery("INSERT INTO " + DatabaseManager.PLOTS_TABLE +
                " (owner_uuid, center_x, center_z)" +
                " VALUES ('" +
                player.getUniqueId().toString() + "','" +
                loc.getBlockX() + "','" +
                loc.getBlockZ() + "')"
        );
    }

    public static void removePlot(String UUID) {
        Parkour.getDatabaseManager().runAsyncQuery("DELETE FROM " + DatabaseManager.PLOTS_TABLE + " WHERE owner_uuid='" + UUID + "'");
    }


    public static HashMap<String, Plot> loadPlots()
    {

        // get all plots from database, join players table -> plots
        List<Map<String, String>> results = DatabaseQueries.getResults(
                DatabaseManager.PLAYERS_TABLE + " p",
                "id, name, center_x, center_z, submitted, owner_uuid",
               "JOIN " + DatabaseManager.PLOTS_TABLE + " pl ON p.uuid=pl.owner_uuid");


        HashMap<String, Plot> tempMap = new HashMap<>();

        // this is technically n more queries than doing one big join, but it is much more readable code plus this only runs once on boot
        for (Map<String, String> result : results)
        {
            int plotID = Integer.parseInt(result.get("id"));
            String ownerUUID = result.get("owner_uuid");
            String ownerName = result.get("name");
            double centerX = Double.parseDouble(result.get("center_x"));
            double centerZ = Double.parseDouble(result.get("center_z"));
            boolean submitted = Integer.parseInt(result.get("submitted")) == 1;

            // loc from database, 0.5 for center of block
            Location loc = new Location(
                     Bukkit.getWorld(Parkour.getSettingsManager().player_submitted_world),
                    centerX + 0.5,
                     Parkour.getSettingsManager().player_submitted_plot_default_y,
                    centerZ + 0.5);

            List<String> trustedUUIDs = getTrustedUUIDs(ownerUUID);

            tempMap.put(ownerName, new Plot(plotID, ownerName, ownerUUID, loc, trustedUUIDs, submitted));
        }

        return tempMap;
    }

    public static void toggleSubmitted(String UUID)
    {
        Parkour.getDatabaseManager().runAsyncQuery("UPDATE " + DatabaseManager.PLOTS_TABLE + " SET submitted=NOT submitted WHERE owner_uuid='" + UUID + "'");
    }

    public static void toggleSubmittedFromName(String playerName)
    {
        Parkour.getDatabaseManager().runAsyncQuery(
                "UPDATE " + DatabaseManager.PLOTS_TABLE + " p " +
                    "JOIN " + DatabaseManager.PLAYERS_TABLE + " pl ON p.owner_uuid=pl.uuid " +
                    "SET submitted=NOT submitted " +
                    "WHERE name=?",
                playerName);
    }
}
