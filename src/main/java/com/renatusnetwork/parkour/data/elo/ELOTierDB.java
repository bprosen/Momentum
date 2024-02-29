package com.renatusnetwork.parkour.data.elo;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.storage.mysql.DatabaseManager;
import com.renatusnetwork.parkour.storage.mysql.DatabaseQueries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ELOTierDB
{
    public static HashMap<String, ELOTier> getTiers()
    {
        HashMap<String, ELOTier> tiers = new HashMap<>();
        List<Map<String, String>> results = DatabaseQueries.getResults(DatabaseManager.ELO_TIERS, "*", "");

        for (Map<String, String> result : results)
        {
            String requiredElo = result.get("required_elo");
            int requiredEloInt = requiredElo != null ? Integer.parseInt(requiredElo) : 0;
            String name = result.get("name");

            tiers.put(name, new ELOTier(name, result.get("title"), requiredEloInt, result.get("previous_elo_tier"), result.get("next_elo_tier")));
        }
        return tiers;
    }

    public static void create(String name)
    {
        DatabaseQueries.runAsyncQuery("INSERT INTO " + DatabaseManager.ELO_TIERS + " (name) VALUES(?)", name);
    }

    public static void updateTitle(String name, String title)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.ELO_TIERS + " SET title=? WHERE name=?", title, name);
    }

    public static void updateRequiredELO(String name, int requiredELO)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.ELO_TIERS + " SET required_elo=? WHERE name=?", requiredELO, name);
    }

    public static void updateNextTier(String name, String nextTier)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.ELO_TIERS + " SET next_elo_tier=? WHERE name=?", nextTier, name);
    }

    public static void updatePreviousTier(String name, String previousTier)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.ELO_TIERS + " SET previous_elo_tier=? WHERE name=?", previousTier, name);
    }

}
