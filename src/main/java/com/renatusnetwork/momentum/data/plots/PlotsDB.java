package com.renatusnetwork.momentum.data.plots;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.storage.mysql.DatabaseManager;
import com.renatusnetwork.momentum.storage.mysql.DatabaseQueries;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

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

    public static List<String> getTrustedUUIDs(int plotID)
    {
        List<String> trustedUUIDs = new ArrayList<>();

        List<Map<String, String>> results = DatabaseQueries.getResults(
                DatabaseManager.PLOTS_TRUSTED_PLAYERS_TABLE,
                "trusted_uuid",
                "WHERE plot_id=?", plotID);

        for (Map<String, String> result : results)
            trustedUUIDs.add(result.get("trusted_uuid"));

        return trustedUUIDs;
    }


    public static void addTrustedPlayer(int plotID, PlayerStats trustedPlayer)
    {
        DatabaseQueries.runAsyncQuery(
                "INSERT INTO " + DatabaseManager.PLOTS_TRUSTED_PLAYERS_TABLE + " (plot_id, trusted_uuid) " +
                    "VALUES (?,?)", plotID, trustedPlayer.getUUID());
    }

    public static void addTrustedPlayer(int plotID, String trustedPlayerUUID) {
        DatabaseQueries.runAsyncQuery(
                "INSERT INTO " + DatabaseManager.PLOTS_TRUSTED_PLAYERS_TABLE + " (plot_id, trusted_uuid) " +
                        "VALUES (?,?)", plotID, trustedPlayerUUID);
    }

    public static void removeTrustedPlayer(int plotID, PlayerStats trustedPlayer)
    {
        DatabaseQueries.runAsyncQuery(
                "DELETE FROM " + DatabaseManager.PLOTS_TRUSTED_PLAYERS_TABLE + " WHERE plot_id=? AND trusted_uuid=?", plotID, trustedPlayer.getUUID());
    }

    public static void removeTrustedPlayer(int plotID, String trustedPlayerUUID)  {
        DatabaseQueries.runAsyncQuery(
                "DELETE FROM " + DatabaseManager.PLOTS_TRUSTED_PLAYERS_TABLE + " WHERE plot_id=? AND trusted_uuid=?", plotID, trustedPlayerUUID);
    }

    public static int getPlotID(Player player)
    {
        List<Map<String, String>> results = DatabaseQueries.getResults(DatabaseManager.PLOTS_TABLE, "id", "WHERE owner_uuid=?", player.getUniqueId().toString());

        return results.isEmpty() ? -1 : Integer.parseInt(results.get(0).get("id"));
    }

    public static void addPlot(PlayerStats playerStats, Location loc)
    {
        DatabaseQueries.runAsyncQuery("INSERT INTO " + DatabaseManager.PLOTS_TABLE +
                " (owner_uuid, center_x, center_z)" +
                " VALUES (?,?,?)", playerStats.getUUID(), loc.getBlockX(), loc.getBlockZ()
        );
    }

    public static void removePlot(String uuid, boolean async)
    {
        if (async)
            DatabaseQueries.runAsyncQuery("DELETE FROM " + DatabaseManager.PLOTS_TABLE + " WHERE owner_uuid=?", uuid);
        else
            DatabaseQueries.runQuery("DELETE FROM " + DatabaseManager.PLOTS_TABLE + " WHERE owner_uuid=?", uuid);
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
                     Bukkit.getWorld(Momentum.getSettingsManager().player_submitted_world),
                    centerX + 0.5,
                     Momentum.getSettingsManager().player_submitted_plot_default_y,
                    centerZ + 0.5);

            List<String> trustedUUIDs = getTrustedUUIDs(plotID);

            tempMap.put(ownerName, new Plot(plotID, ownerName, ownerUUID, loc, trustedUUIDs, submitted));
        }

        return tempMap;
    }

    public static int getCurrentMaxPlotID()
    {
        Map<String, String> result = DatabaseQueries.getResult(DatabaseManager.PLOTS_TABLE, "MAX(id) AS last_id", "");

        return !result.isEmpty() ? Integer.parseInt(result.get("last_id")) : 0;
    }

    public static void toggleSubmitted(String uuid)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.PLOTS_TABLE + " SET submitted=NOT submitted WHERE owner_uuid=?", uuid);
    }

    public static void toggleSubmittedFromName(String playerName)
    {
        DatabaseQueries.runAsyncQuery(
                "UPDATE " + DatabaseManager.PLOTS_TABLE + " p " +
                    "JOIN " + DatabaseManager.PLAYERS_TABLE + " pl ON p.owner_uuid=pl.uuid " +
                    "SET submitted=NOT submitted " +
                    "WHERE name=?",
                playerName);
    }

    public static Location[] getLastTwoPlotLocations()
    {
        List<Map<String, String>> results = DatabaseQueries.getResults(
                DatabaseManager.PLOTS_TABLE,
                "center_x, center_z",
                "ORDER BY id DESC LIMIT 2");

        Location[] array = new Location[2];

        int index = 0;
        for (Map<String, String> result : results)
        {
            array[index] = new Location(
                    Bukkit.getWorld(Momentum.getSettingsManager().player_submitted_world),
                    Double.parseDouble(result.get("center_x")),
                    Momentum.getSettingsManager().player_submitted_plot_default_y,
                    Double.parseDouble(result.get("center_z"))
            );
            index++;
        }

        return array;
    }
}
