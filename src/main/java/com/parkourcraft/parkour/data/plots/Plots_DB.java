package com.parkourcraft.parkour.data.plots;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.storage.mysql.DatabaseQueries;
import org.bukkit.Bukkit;
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

    public static boolean hasPlotFromName(String playerName) {
        List<Map<String, String>> results = DatabaseQueries.getResults(
                "plots",
                "*", " WHERE player_name='" + playerName + "'");

        if (!results.isEmpty())
            return true;
        return false;
    }

    public static boolean playerNameHasPlot(String playerName) {
        List<Map<String, String>> results = DatabaseQueries.getResults(
                "plots",
                "*", " WHERE player_name='" + playerName + "'");

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

    public static String getPlotCenterFromName(String playerName) {
        if (playerNameHasPlot(playerName)) {
            List<Map<String, String>> results = DatabaseQueries.getResults(
                    "plots",
                    "center_x, center_z", " WHERE player_name='" + playerName + "'");

            for (Map<String, String> result : results)
                return result.get("center_x") + ":" + result.get("center_z");
        }
        return null;
    }

    public static List<String> getTrustedPlayers(String UUID) {
        List<String> trustedPlayers = new ArrayList<>();

        if (hasPlot(UUID)) {
            List<Map<String, String>> results = DatabaseQueries.getResults(
                    "plots",
                    "trusted_players", " WHERE uuid='" + UUID + "'");

            for (Map<String, String> result : results) {

                String[] split = result.get("trusted_players").split(":");

                for (String playerName : split)
                    trustedPlayers.add(playerName);
            }
        }
        return trustedPlayers;
    }

    public static void addTrustedPlayer(Player player, Player trustedPlayer) {

        if (hasPlot(player.getUniqueId().toString())) {
            List<String> trustedPlayers = getTrustedPlayers(player.getUniqueId().toString());

            // add into acceptable string
            String joinedString = trustedPlayer.getName();
            for (String playerString : trustedPlayers)
                joinedString += ":" + playerString;

            Parkour.getDatabaseManager().run("UPDATE plots SET trusted_players='" + joinedString
                                             + "' WHERE uuid='" + player.getUniqueId().toString() + "'");
        }
    }

    public static void removeTrustedPlayer(Player player, Player trustedPlayer) {

        if (hasPlot(player.getUniqueId().toString())) {
            List<String> trustedPlayers = getTrustedPlayers(player.getUniqueId().toString());
            trustedPlayers.remove(trustedPlayer.getName());

            // add into acceptable string
            String joinedString = null;
            for (String playerString : trustedPlayers) {

                // if they are last in list, do not add semicolon
                if (trustedPlayers.get(trustedPlayers.size() - 1).equalsIgnoreCase(playerString))
                    joinedString += playerString;
                else
                    joinedString += playerString + ":";
            }

            Parkour.getDatabaseManager().run("UPDATE plots SET trusted_players='" + joinedString
                    + "' WHERE uuid='" + player.getUniqueId().toString() + "'");
        }
    }

    public static void addPlot(Player player, Location loc) {
        Parkour.getDatabaseManager().run("INSERT INTO plots " +
                "(uuid, player_name, trusted_players, center_x, center_z, submitted)" +
                " VALUES ('" +
                player.getUniqueId().toString() + "','" +
                player.getName() + "','" +
                "','" +
                loc.getBlockX() + "','" +
                loc.getBlockZ() + "','" +
                "false" +
                "')"
        );
    }

    public static void removePlot(Player player) {
        Parkour.getDatabaseManager().add("DELETE FROM plots " +
                "WHERE uuid='" + player.getUniqueId().toString() + "'");
    }


    public static List<String> getPlotOwnerUUIDs() {
        // get all plots from database
        List<Map<String, String>> results = DatabaseQueries.getResults(
                "plots",
                "uuid", "");

        List<String> tempList = new ArrayList<>();
        for (Map<String, String> result : results)
            tempList.add(result.get("uuid"));

        return tempList;
    }

    public static String getPlotOwnerName(String UUID) {
        if (hasPlot(UUID)) {
            List<Map<String, String>> results = DatabaseQueries.getResults(
                    "plots",
                    "player_name", " WHERE uuid='" + UUID + "'");

            for (Map<String, String> result : results)
                return result.get("player_name");
        }
        return null;
    }

    public static boolean isSubmitted(String UUID) {
        boolean submitted = false;

        if (hasPlot(UUID)) {
            List<Map<String, String>> results = DatabaseQueries.getResults(
                    "plots",
                    "submitted", " WHERE uuid='" + UUID + "'");

            for (Map<String, String> result : results)
                submitted = Boolean.parseBoolean(result.get("submitted"));
        }
        return submitted;
    }

    public static boolean isSubmittedFromName(String playerName) {
        boolean submitted = false;

        if (hasPlotFromName(playerName)) {
            List<Map<String, String>> results = DatabaseQueries.getResults(
                    "plots",
                    "submitted", " WHERE player_name='" + playerName + "'");

            for (Map<String, String> result : results)
                submitted = Boolean.parseBoolean(result.get("submitted"));
        }
        return submitted;
    }

    public static void toggleSubmitted(String UUID) {
        if (hasPlot(UUID)) {
            boolean currentlySubmitted = isSubmitted(UUID);
            Parkour.getDatabaseManager().add("UPDATE plots SET submitted='" + !currentlySubmitted + "' WHERE UUID='" + UUID + "'");
        }
    }

    public static void toggleSubmittedFromName(String playerName) {
        if (hasPlotFromName(playerName)) {
            boolean currentlySubmitted = isSubmittedFromName(playerName);
            Parkour.getDatabaseManager().add("UPDATE plots SET submitted='" + !currentlySubmitted + "' WHERE player_name='" + playerName + "'");
        }
    }
}
