package com.renatusnetwork.parkour.data.perks;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.storage.mysql.DatabaseManager;
import com.renatusnetwork.parkour.storage.mysql.DatabaseQueries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerksDB
{

    public static void loadPerks(PlayerStats playerStats) {
        List<Map<String, String>> perksResults = DatabaseQueries.getResults(
                DatabaseManager.PERKS_OWNED_TABLE,
                "perk_name, " +
                        "(UNIX_TIMESTAMP(date_received) * 1000) AS date",
                        "WHERE uuid='" + playerStats.getUUID() + "'");

        for (Map<String, String> perkResult : perksResults)
            playerStats.addPerk(
                    perkResult.get("perk_name"),
                    Long.parseLong(perkResult.get("date"))
            );
    }

    public static void addOwnedPerk(PlayerStats playerStats, Perk perk, Long date) {
        Parkour.getDatabaseManager().runAsyncQuery(
                "INSERT INTO " + DatabaseManager.PERKS_OWNED_TABLE + " (uuid, perk_name, date_received) VALUES " +
                    "(" + playerStats.getUUID() + ", " + perk.getName() + ", FROM_UNIXTIME(" + (date / 1000) + "))"
        );
    }

    public static void updateInfiniteBlock(PlayerStats playerStats, Perk perk)
    {
        Parkour.getDatabaseManager().runAsyncQuery("UPDATE " + DatabaseManager.PLAYERS_TABLE + " SET infinite_block='" + perk.getInfiniteBlock().name() + "' WHERE uuid='" + playerStats.getUUID() + "'");
    }
}