package com.parkourcraft.parkour.data.races;

import com.parkourcraft.parkour.Parkour;

public class RaceDB {

    public static void updateRaceLosses(String playerUUID, int newRaceLosses) {
        String query = "UPDATE players SET race_losses=" + newRaceLosses +
                       " WHERE uuid='" + playerUUID + "'";

        Parkour.getDatabaseManager().add(query);
    }

    public static void updateRaceWins(String playerUUID, int newRaceWins) {
        String query = "UPDATE players SET race_wins=" + newRaceWins +
                " WHERE uuid='" + playerUUID + "'";

        Parkour.getDatabaseManager().add(query);
    }
}
