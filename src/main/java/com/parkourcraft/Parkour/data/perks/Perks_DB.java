package com.parkourcraft.Parkour.data.perks;

import com.parkourcraft.Parkour.Parkour;
import com.parkourcraft.Parkour.data.stats.PlayerStats;
import com.parkourcraft.Parkour.storage.mysql.DatabaseManager;
import com.parkourcraft.Parkour.storage.mysql.DatabaseQueries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Perks_DB {

    static Map<String, Integer> getIDCache() {
        Map<String, Integer> IDCache = new HashMap<>();

        List<Map<String, String>> perkResults = DatabaseQueries.getResults(
                "perks",
                "perk_id, perk_name",
                ""
        );

        for (Map<String, String> perkResult : perkResults)
            IDCache.put(
                    perkResult.get("perk_name"),
                    Integer.parseInt(perkResult.get("perk_id"))
            );

        Parkour.getPluginLogger().info("Perk IDs in cache: " + IDCache.size());

        return IDCache;
    }

    static void syncIDCache() {
        for (Perk perk : Parkour.perks.getPerks())
            syncIDCache(perk, Parkour.perks.getIDCache());
    }

    static void syncIDCache(Perk perk, Map<String, Integer> IDCache) {
        if (IDCache.containsKey(perk.getName()))
            perk.setID(IDCache.get(perk.getName()));
    }

    static boolean syncPerkIDs() {
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

            Parkour.database.run(finalQuery);

            return true;
        }

        return false;
    }

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
        Parkour.database.add(
                "INSERT INTO ledger (player_id, perk_id, date)"
                        + " VALUES "
                        + "(" + playerStats.getPlayerID()
                        + ", " + perk.getID()
                        + ", FROM_UNIXTIME(" + (date / 1000) + "))"
        );
    }

}
