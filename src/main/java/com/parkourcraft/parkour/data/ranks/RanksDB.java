package com.parkourcraft.parkour.data.ranks;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import java.util.UUID;

public class RanksDB {

    public static void updateRank(UUID uuid, int rankId) {

        PlayerStats playerStats = Parkour.getStatsManager().get(uuid.toString());
        String query = "UPDATE players SET " +
                "rank_id='" + rankId + "' " +
                "WHERE player_id=" + playerStats.getPlayerID()
                ;

        Parkour.getDatabaseManager().add(query);
    }

    public static void updateStage(UUID uuid, int stage) {

        // -1 for BIT type in database
        stage--;

        PlayerStats playerStats = Parkour.getStatsManager().get(uuid.toString());
        String query = "UPDATE players SET " +
                "rankup_stage=" + stage + " " +
                "WHERE player_id=" + playerStats.getPlayerID()
                ;

        Parkour.getDatabaseManager().add(query);
    }

    // from UUID method
    public static void updatePrestiges(UUID uuid, int newAmount) {

        Parkour.getDatabaseManager().add("UPDATE players SET rank_prestiges=" + newAmount +
                " WHERE uuid='" + uuid.toString() + "'");
    }

    // from playerName method
    public static void updatePrestiges(String playerName, int newAmount) {
        Parkour.getDatabaseManager().add("UPDATE players SET rank_prestiges=" + newAmount +
                " WHERE player_name='" + playerName + "'");
    }
}
