package com.renatusnetwork.parkour.data.ranks;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.stats.PlayerStats;

import java.util.UUID;

public class RanksDB {

    public static void updateRank(UUID uuid, int rankId) {

        PlayerStats playerStats = Parkour.getStatsManager().get(uuid.toString());
        String query = "UPDATE players SET " +
                "rank_id='" + rankId + "' " +
                "WHERE player_id=" + playerStats.getPlayerID()
                ;

        Parkour.getDatabaseManager().runAsyncQuery(query);
    }

    // from UUID method
    public static void updatePrestiges(UUID uuid, int newAmount) {

        Parkour.getDatabaseManager().runAsyncQuery("UPDATE players SET rank_prestiges=? WHERE uuid=?", newAmount, uuid);
    }

    // from playerName method
    public static void updatePrestiges(String playerName, int newAmount) {
        Parkour.getDatabaseManager().runAsyncQuery("UPDATE players SET rank_prestiges=? WHERE player_name=?", newAmount, playerName);
    }
}
