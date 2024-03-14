package com.renatusnetwork.momentum.data.races;

import com.renatusnetwork.momentum.Momentum;

public class RaceDB {

    public static void updateRaceLosses(String playerUUID, int newRaceLosses) {
        String query = "UPDATE players SET race_losses=" + newRaceLosses +
                       " WHERE uuid='" + playerUUID + "'";

        Momentum.getDatabaseManager().runAsyncQuery(query);
    }

    public static void updateRaceWins(String playerUUID, int newRaceWins) {
        String query = "UPDATE players SET race_wins=" + newRaceWins +
                " WHERE uuid='" + playerUUID + "'";

        Momentum.getDatabaseManager().runAsyncQuery(query);
    }
}
