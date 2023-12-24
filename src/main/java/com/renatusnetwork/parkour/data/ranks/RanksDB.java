package com.renatusnetwork.parkour.data.ranks;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.storage.mysql.DatabaseManager;

import java.util.UUID;

public class RanksDB {

    public static void updateRank(String uuid, String rank)
    {
        Parkour.getDatabaseManager().runAsyncQuery("UPDATE " + DatabaseManager.PLAYERS_TABLE + " SET rank=? WHERE uuid=?", rank, uuid);
    }

    // from UUID method
    public static void updatePrestiges(String uuid, int newAmount)
    {
        Parkour.getDatabaseManager().runAsyncQuery("UPDATE " + DatabaseManager.PLAYERS_TABLE + " SET prestiges=? WHERE uuid=?", newAmount, uuid);
    }

    // from playerName method
    public static void updatePrestigesFromName(String playerName, int newAmount)
    {
        Parkour.getDatabaseManager().runAsyncQuery("UPDATE " + DatabaseManager.PLAYERS_TABLE + " SET prestiges=? WHERE name=?", newAmount, playerName);
    }
}
