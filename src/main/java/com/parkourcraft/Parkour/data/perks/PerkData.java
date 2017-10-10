package com.parkourcraft.Parkour.data.perks;

import com.parkourcraft.Parkour.Parkour;
import com.parkourcraft.Parkour.data.stats.PlayerStats;
import com.parkourcraft.Parkour.storage.mysql.DatabaseManager;
import com.parkourcraft.Parkour.storage.mysql.DatabaseQueries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PerkData {

    public static void loadPerks(PlayerStats playerStats) {
        List<Map<String, String>> perksResults = DatabaseQueries.getResults(
                "ledger",
                "perk.perk_name, " +
                        "(UNIX_TIMESTAMP(date) * 1000) AS date",
                "JOIN perks perk" +
                        " on perk.perk_id=ledger.perk_id" +
                        " WHERE player_id=" + playerStats.getPlayerID()
        );

        for (Map<String, String> perkResult : perksResults)
            playerStats.addPerk(
                    perkResult.get("perk_name"),
                    Long.parseLong(perkResult.get("date"))
            );
    }

    public static void insertPerk(PlayerStats playerStats, Perk perk, Long date) {
        DatabaseManager.addUpdateQuery(
                "INSERT INTO ledger (player_id, perk_id, date)"
                        + " VALUES "
                        + "(" + playerStats.getPlayerID()
                        + ", " + perk.getID()
                        + ", FROM_UNIXTIME(" + (date / 1000) + "))"
        );
    }

    public static void syncPerkID(Perk perk) {
        if (Parkour.perks.getPerkIDCache().containsKey(perk.getName()))
            perk.setID(Parkour.perks.getPerkIDCache().get(perk.getName()));
    }

    private static void syncPerkIDCache() {
        for (Perk perk : Parkour.perks.getPerks())
            syncPerkID(perk);
    }

    public static void loadPerkIDCache() {
        List<Map<String, String>> perkResults = DatabaseQueries.getResults(
                "perks",
                "perk_id, perk_name",
                ""
        );

        for (Map<String, String> perkResult : perkResults)
            Parkour.perks.getPerkIDCache().put(
                    perkResult.get("perk_name"),
                    Integer.parseInt(perkResult.get("perk_id"))
            );

        syncPerkIDCache();
    }

    public static void syncPerkIDs() {
        List<String> insertQueries = new ArrayList<>();

        for (Perk perk : Parkour.perks.getPerks())
            if (perk.getID() == -1)
                insertQueries.add(
                        "INSERT INTO perks " +
                                "(perk_name)" +
                                " VALUES " +
                                "('" + perk.getName() + "')"
                );

        if (insertQueries.size() > 0) {
            String finalQuery = "";
            for (String sql : insertQueries)
                finalQuery = finalQuery + sql + "; ";

            DatabaseManager.runQuery(finalQuery);
            loadPerkIDCache();
        }
    }

}
