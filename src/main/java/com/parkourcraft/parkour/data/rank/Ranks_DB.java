package com.parkourcraft.parkour.data.rank;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import java.util.UUID;

public class Ranks_DB {

    public static void updateRank(UUID uuid, int rankId) {

        PlayerStats playerStats = Parkour.getStatsManager().get(uuid.toString());
        String query = "UPDATE players SET " +
                "rank_id='" + rankId + "' " +
                "WHERE player_id=" + playerStats.getPlayerID()
                ;

        Parkour.getDatabaseManager().add(query);
    }
}
