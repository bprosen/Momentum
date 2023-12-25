package com.renatusnetwork.parkour.data.ranks;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.storage.mysql.DatabaseManager;
import com.renatusnetwork.parkour.storage.mysql.DatabaseQueries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RanksDB {

    public static HashMap<String, Rank> loadRanks()
    {
        List<Map<String, String>> results = DatabaseQueries.getResults(DatabaseManager.RANKS_TABLE, "*", "");

        HashMap<String, Rank> tempMap = new HashMap<>();

        for (Map<String, String> result : results)
        {
            String rankName = result.get("name");
            String rankTitle = result.get("title");
            String rankUpLevel = result.get("rankup_level");
            String nextRank = result.get("next_rank");

            tempMap.put(rankName, new Rank(rankName, rankTitle, rankUpLevel, nextRank));
        }

        return tempMap;
    }

    // from UUID method
    public static void updatePrestiges(String uuid, int newAmount)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.PLAYERS_TABLE + " SET prestiges=? WHERE uuid=?", newAmount, uuid);
    }

    // from playerName method
    public static void updatePrestigesFromName(String playerName, int newAmount)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.PLAYERS_TABLE + " SET prestiges=? WHERE name=?", newAmount, playerName);
    }

    public static void addRank(String name)
    {
        DatabaseQueries.runAsyncQuery("INSERT INTO " + DatabaseManager.RANKS_TABLE + " (name) VALUES('" + name + "')");
    }

    public static void removeRank(String rankName)
    {
        DatabaseQueries.runAsyncQuery("DELETE FROM " + DatabaseManager.RANKS_TABLE + " WHERE name='" + rankName + "'");
    }

    public static void updateTitle(String rankName, String title)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.RANKS_TABLE + " SET title=? WHERE name=?", title, rankName);
    }

    public static void updateRankupLevel(String rankName, String rankupLevel)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.RANKS_TABLE + " SET rankup_level=? WHERE name=?", rankupLevel, rankName);
    }

    public static void updateNextRank(String rankName, String nextRank)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.RANKS_TABLE + " SET next_rank=? WHERE name=?", nextRank, rankName);
    }
}
