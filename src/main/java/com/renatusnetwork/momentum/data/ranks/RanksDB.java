package com.renatusnetwork.momentum.data.ranks;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.stats.PlayerStats;

import java.util.UUID;

public class RanksDB {

    public static void updateRank(UUID uuid, int rankId) {

        PlayerStats playerStats = Momentum.getStatsManager().get(uuid.toString());
        String query = "UPDATE players SET " +
                "rank_id='" + rankId + "' " +
                "WHERE player_id=" + playerStats.getPlayerID()
                ;

        Momentum.getDatabaseManager().runAsyncQuery(query);
    }

    public static void updateStage(UUID uuid, int stage) {

        // -1 for BIT type in database
        stage--;

        PlayerStats playerStats = Momentum.getStatsManager().get(uuid.toString());
        String query = "UPDATE players SET " +
                "rankup_stage=" + stage + " " +
                "WHERE player_id=" + playerStats.getPlayerID()
                ;

        Momentum.getDatabaseManager().runAsyncQuery(query);
    }

    // from UUID method
    public static void updatePrestiges(UUID uuid, int newAmount) {

        Momentum.getDatabaseManager().runAsyncQuery("UPDATE players SET rank_prestiges=? WHERE uuid=?", newAmount, uuid.toString());
    }

    // from playerName method
    public static void updatePrestiges(String playerName, int newAmount) {
        Momentum.getDatabaseManager().runAsyncQuery("UPDATE players SET rank_prestiges=? WHERE player_name=?", newAmount, playerName);
    }
}
