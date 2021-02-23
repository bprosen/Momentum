package com.parkourcraft.parkour.data.races;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class RaceManager {

    private List<Race> runningRaceList = new ArrayList<>();

    public void startRace(Player player1, Player player2) {
        Race newRace = new Race(player1, player2);
        runningRaceList.add(newRace);
    }

    public void endRace(Player winner, Player loser) {

    }

    public Race get(Player player) {
        for (Race race : runningRaceList)
            if (race.getPlayer1().equals(player) || race.getPlayer2().equals(player))
                return race;

        return null;
    }
}
