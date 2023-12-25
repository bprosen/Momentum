package com.renatusnetwork.parkour.data.races;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.storage.mysql.DatabaseQueries;

public class RaceDB {

    public static void updateRaceLosses(String playerUUID, int newRaceLosses) {
        String query = "UPDATE players SET race_losses=" + newRaceLosses +
                       " WHERE uuid='" + playerUUID + "'";

        DatabaseQueries.runAsyncQuery(query);
    }

    public static void updateRaceWins(String playerUUID, int newRaceWins) {
        String query = "UPDATE players SET race_wins=" + newRaceWins +
                " WHERE uuid='" + playerUUID + "'";

        DatabaseQueries.runAsyncQuery(query);
    }
}
