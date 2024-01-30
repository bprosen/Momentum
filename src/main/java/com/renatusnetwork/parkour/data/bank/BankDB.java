package com.renatusnetwork.parkour.data.bank;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.bank.types.BankItemType;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.storage.mysql.DatabaseQueries;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BankDB
{
    // get bids from type
    public static HashMap<String, Long> getBids(BankItemType type)
    {
        HashMap<String, Long> bids = new HashMap<>();

        List<Map<String, String>> results = DatabaseQueries.getResults("bank", "*", " WHERE type='" + type.toString() + "'");

        for (Map<String, String> result : results)
            bids.put(result.get("name"), Long.parseLong(result.get("total_bid")));

        return bids;
    }

    public static void updateBid(PlayerStats playerStats, BankItemType type, int bidAmount)
    {
        // run in async!
        DatabaseQueries.runAsyncQuery(
                    "UPDATE bank SET total_bid=" + bidAmount + " WHERE type='" + type + "' AND name='" + playerStats.getName() + "'"
        );
    }
}
