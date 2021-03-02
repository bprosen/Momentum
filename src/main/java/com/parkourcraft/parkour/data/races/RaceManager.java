package com.parkourcraft.parkour.data.races;

import com.connorlinfoot.titleapi.TitleAPI;
import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.levels.LevelObject;
import com.parkourcraft.parkour.data.levels.Levels_YAML;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.utils.Utils;
import com.parkourcraft.parkour.utils.dependencies.WorldGuard;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RaceManager {

    private List<Race> runningRaceList = new ArrayList<>();

    public void startRace(Player player1, Player player2, boolean bet, double betAmount) {

        List<String> temporaryLevelList = new ArrayList<>();

        // if races are in use, then filter through which ones are in use
        if (!getRaces().isEmpty()) {
            // this is what filters not in use races
            for (Race race : getRaces()) {
                for (String levelName : Parkour.getLevelManager().getRaceLevels()) {
                    if (!race.getRaceLevel().getName().equalsIgnoreCase(levelName)
                            && Parkour.getLevelManager().get(levelName).hasValidRaceLocations()) {

                        temporaryLevelList.add(levelName);
                    }
                }
            }

            // if there are no levels available
            if (temporaryLevelList.isEmpty()) {
                player1.sendMessage(Utils.translate("&cNo maps available for use, try again later"));
                player2.sendMessage(Utils.translate("&cNo maps available for use, try again later"));
                return;
            }
        } else {
            // otherwise, add all race levels if none are in use
            temporaryLevelList.addAll(Parkour.getLevelManager().getRaceLevels());
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

            // set level and set inRace to true
            PlayerStats player1Stats = Parkour.getStatsManager().get(player1);
            PlayerStats player2Stats = Parkour.getStatsManager().get(player2);
            player1Stats.startedRace();
            player2Stats.startedRace();
            player1Stats.setLevel(level.getName());
            player2Stats.setLevel(level.getName());

            player1.teleport(level.getRaceLocation1());
            player2.teleport(level.getRaceLocation2());

            // remove potion effects
            for (PotionEffect effects : player1.getActivePotionEffects()) {
                player1.removePotionEffect(effects.getType());
            }

            for (PotionEffect effects : player2.getActivePotionEffects()) {
                player2.removePotionEffect(effects.getType());
            }

            // freeze and do countdown
            new BukkitRunnable() {
                int runCycles = 0;
                public void run() {

                    // cancel, send last title and return
                    if (runCycles == 100) {
                        cancel();
                        TitleAPI.sendTitle(player1, 0, 20, 10, "&cRACE", "");
                        TitleAPI.sendTitle(player2, 0, 20, 10, "&cRACE", "");
                        return;
                    }

                    // race location variables
                    double race1X = level.getRaceLocation1().getX();
                    double race1Z = level.getRaceLocation1().getZ();
                    double race2X = level.getRaceLocation2().getX();
                    double race2Z = level.getRaceLocation2().getZ();

                    // player location variables
                    double player1X = player1.getLocation().getX();
                    double player1Z = player1.getLocation().getZ();
                    double player2X = player2.getLocation().getX();
                    double player2Z = player2.getLocation().getZ();

                    // teleport back if moved
                    if (race1X != player1X || race1Z != player1Z) {
                        Location raceLoc1 = level.getRaceLocation1().clone();
                        raceLoc1.setYaw(player1.getLocation().getYaw());
                        raceLoc1.setPitch(player1.getLocation().getPitch());
                        player1.teleport(raceLoc1);
                    }

                    if (race2X != player2X || race2Z != player2Z) {
                        Location raceLoc2 = level.getRaceLocation2().clone();
                        raceLoc2.setYaw(player2.getLocation().getYaw());
                        raceLoc2.setPitch(player2.getLocation().getPitch());
                        player2.teleport(raceLoc2);
                    }

                    // countdown if-else
                    if (runCycles == 0) {
                        TitleAPI.sendTitle(player1, 0, 20, 0, "&a5", "");
                        TitleAPI.sendTitle(player2, 0, 20, 0, "&a5", "");
                    } else if (runCycles == 20) {
                        TitleAPI.sendTitle(player1, 0, 20, 0, "&e4", "");
                        TitleAPI.sendTitle(player2, 0, 20, 0, "&e4", "");
                    } else if (runCycles == 40) {
                        TitleAPI.sendTitle(player1, 0, 20, 0, "&63", "");
                        TitleAPI.sendTitle(player2, 0, 20, 0, "&63", "");
                    } else if (runCycles == 60) {
                        TitleAPI.sendTitle(player1, 0, 20, 0, "&c2", "");
                        TitleAPI.sendTitle(player2, 0, 20, 0, "&c2", "");
                    } else if (runCycles == 80) {
                        TitleAPI.sendTitle(player1, 0, 20, 0, "&41", "");
                        TitleAPI.sendTitle(player2, 0, 20, 0, "&41", "");
                    }
                    runCycles++;
                }
            }.runTaskTimer(Parkour.getPlugin(), 1, 1);
        } else {
            player1.sendMessage(Utils.translate("&cInvalid level? Try again or contact an Admin"));
            player2.sendMessage(Utils.translate("&cInvalid level? Try again or contact an Admin"));
        }
    }

    public void endRace(Player winner) {

        Race raceObject = get(winner);

        if (raceObject != null) {
            Player loser = raceObject.getOpponent(winner);

            if (raceObject.hasBet())
                Bukkit.broadcastMessage(Utils.translate("&4&l" + winner.getName() + " &chas beaten &4&l" + loser.getName()
                                        + " &cin a race for &6$" + raceObject.getBet()));
            else
                Bukkit.broadcastMessage(Utils.translate("&4&l" + winner.getName() + " &chas beaten &4&l" + loser.getName()
                                        + " &cin a race!"));

            // give winner money and take from loser if betted on race
            if (raceObject.hasBet()) {
                Parkour.getEconomy().withdrawPlayer(loser, raceObject.getBet());
                Parkour.getEconomy().depositPlayer(winner, raceObject.getBet());
            }

            // check if winner is player 1, then teleport accordingly, otherwise they are player 2
            if (raceObject.isPlayer1(winner)) {
                winner.teleport(raceObject.getOriginalPlayer1Loc());
                loser.teleport(raceObject.getOriginalPlayer2Loc());
            } else {
                loser.teleport(raceObject.getOriginalPlayer1Loc());
                winner.teleport(raceObject.getOriginalPlayer2Loc());
            }
            PlayerStats winnerStats = Parkour.getStatsManager().get(winner);
            PlayerStats loserStats = Parkour.getStatsManager().get(loser);
            winnerStats.endedRace();
            loserStats.endedRace();

            List<String> winnerRegions = WorldGuard.getRegions(winner.getLocation());
            List<String> loserRegions = WorldGuard.getRegions(loser.getLocation());
            if (!winnerRegions.isEmpty())
                winnerStats.setLevel(winnerRegions.get(0));

            if (!loserRegions.isEmpty())
                loserStats.setLevel(loserRegions.get(0));

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