package com.parkourcraft.parkour.data.races;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.levels.LevelObject;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RaceManager {

    private List<Race> runningRaceList = new ArrayList<>();

    public void startRace(Player player1, Player player2, boolean bet, double betAmount) {

        List<String> temporaryLevelList = new ArrayList<>();

        // this is what filters not in use races
        for (Race race : getRaces()) {
            for (String levelName : Parkour.getLevelManager().getRaceLevels()) {
                if (!race.getRaceLevel().getName().equalsIgnoreCase(levelName))
                    temporaryLevelList.add(levelName);
            }
        }

        // if there are no levels available
        if (temporaryLevelList.isEmpty()) {
            player1.sendMessage(Utils.translate("&cNo maps available for use, try again later"));
            player2.sendMessage(Utils.translate("&cNo maps available for use, try again later"));
            return;
        }

        // picks random map
        Random ran = new Random();
        LevelObject level = Parkour.getLevelManager().get(
                                temporaryLevelList.get(ran.nextInt(temporaryLevelList.size())
                            ));

        // make sure it is not an invalid level
        if (level != null) {
            // create object for the race
            Race newRace = new Race(player1, player2, level, bet, betAmount);
            runningRaceList.add(newRace);

            player1.teleport(level.getRaceLocation1());
            player2.teleport(level.getRaceLocation2());
        } else {
            player1.sendMessage(Utils.translate("&cInvalid level? Try again or contact an Admin"));
            player2.sendMessage(Utils.translate("&cInvalid level? Try again or contact an Admin"));
        }
    }

    public void endRace(Player winner) {

        Race raceObject = get(winner);

        if (raceObject != null) {
            Player loser = raceObject.getOpponent(winner);

            if (raceObject.hasBet()) {
                Bukkit.broadcastMessage("");
                Bukkit.broadcastMessage(Utils.translate("&4&l" + winner.getName() + " &chas beaten &4&l" + loser
                                        + " &cin a race for &6&l$" + raceObject.getBet()));
                Bukkit.broadcastMessage("");
            } else {
                Bukkit.broadcastMessage("");
                Bukkit.broadcastMessage(Utils.translate("&4&l" + winner.getName() + " &chas beaten &4&l" + loser
                                        + " &cin a race!"));
                Bukkit.broadcastMessage("");
            }

            // give winner money and take from loser
            Parkour.getEconomy().withdrawPlayer(loser, raceObject.getBet());
            Parkour.getEconomy().depositPlayer(winner, raceObject.getBet());

            // check if winner is player 1, then teleport accordingly, otherwise they are player 2
            if (raceObject.isPlayer1(winner)) {
                winner.teleport(raceObject.getOriginalPlayer1Loc());
                loser.teleport(raceObject.getOriginalPlayer2Loc());
            } else {
                loser.teleport(raceObject.getOriginalPlayer1Loc());
                winner.teleport(raceObject.getOriginalPlayer2Loc());
            }
            // remove from list
            runningRaceList.remove(raceObject);
        }
    }

    public Race get(Player player) {
        for (Race race : runningRaceList)
            if (race.getPlayer1().equals(player) || race.getPlayer2().equals(player))
                return race;

        return null;
    }

    public List<Race> getRaces() {
        return runningRaceList;
    }
}
