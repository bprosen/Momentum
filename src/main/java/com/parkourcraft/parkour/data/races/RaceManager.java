package com.parkourcraft.parkour.data.races;

import com.connorlinfoot.titleapi.TitleAPI;
import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.infinite.InfinitePKLBPosition;
import com.parkourcraft.parkour.data.levels.Level;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.storage.mysql.DatabaseQueries;
import com.parkourcraft.parkour.utils.Utils;
import com.parkourcraft.parkour.utils.dependencies.WorldGuard;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class RaceManager {

    private Set<Race> runningRaceList = new HashSet<>();
    private LinkedHashSet<RaceLBPosition> raceLeaderboard = new LinkedHashSet<>(Parkour.getSettingsManager().max_race_leaderboard_size);

    public RaceManager() {
        new BukkitRunnable() {
            @Override
            public void run() {
                loadLeaderboard();
            }
        }.runTaskAsynchronously(Parkour.getPlugin());
    }

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

        if (temporaryLevelList.size() > 0) {
            // picks random map
            Random ran = new Random();
            Level level = Parkour.getLevelManager().get(
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
                            player1.playSound(player1.getLocation(), Sound.BLOCK_NOTE_BELL, 8F, 2F);
                            player2.playSound(player2.getLocation(), Sound.BLOCK_NOTE_BELL, 8F, 2F);
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
                            player1.playSound(player1.getLocation(), Sound.BLOCK_NOTE_HAT, 8F, 2F);
                            player2.playSound(player2.getLocation(), Sound.BLOCK_NOTE_HAT, 8F, 2F);
                        } else if (runCycles == 20) {
                            TitleAPI.sendTitle(player1, 0, 20, 0, "&e4", "");
                            TitleAPI.sendTitle(player2, 0, 20, 0, "&e4", "");
                            player1.playSound(player1.getLocation(), Sound.BLOCK_NOTE_HAT, 8F, 2F);
                            player2.playSound(player2.getLocation(), Sound.BLOCK_NOTE_HAT, 8F, 2F);
                        } else if (runCycles == 40) {
                            TitleAPI.sendTitle(player1, 0, 20, 0, "&63", "");
                            TitleAPI.sendTitle(player2, 0, 20, 0, "&63", "");
                            player1.playSound(player1.getLocation(), Sound.BLOCK_NOTE_HAT, 8F, 2F);
                            player2.playSound(player2.getLocation(), Sound.BLOCK_NOTE_HAT, 8F, 2F);
                        } else if (runCycles == 60) {
                            TitleAPI.sendTitle(player1, 0, 20, 0, "&c2", "");
                            TitleAPI.sendTitle(player2, 0, 20, 0, "&c2", "");
                            player1.playSound(player1.getLocation(), Sound.BLOCK_NOTE_HAT, 8F, 2F);
                            player2.playSound(player2.getLocation(), Sound.BLOCK_NOTE_HAT, 8F, 2F);
                        } else if (runCycles == 80) {
                            TitleAPI.sendTitle(player1, 0, 20, 0, "&41", "");
                            TitleAPI.sendTitle(player2, 0, 20, 0, "&41", "");
                            player1.playSound(player1.getLocation(), Sound.BLOCK_NOTE_HAT, 8F, 2F);
                            player2.playSound(player2.getLocation(), Sound.BLOCK_NOTE_HAT, 8F, 2F);
                        }
                        runCycles++;
                    }
                }.runTaskTimer(Parkour.getPlugin(), 1, 1);
            } else {
                player1.sendMessage(Utils.translate("&cInvalid level? Try again or contact an Admin"));
                player2.sendMessage(Utils.translate("&cInvalid level? Try again or contact an Admin"));
            }
        } else {
            player1.sendMessage(Utils.translate("&cNo maps have been made for races"));
            player2.sendMessage(Utils.translate("&cNo maps have been made for races"));
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
            // update winner wins
            winnerStats.endedRace();
            winnerStats.setRaceWins(winnerStats.getRaceWins() + 1);
            RaceDB.updateRaceWins(winnerStats.getUUID(), winnerStats.getRaceWins());

            // set winner race win rate
            if (winnerStats.getRaceLosses() > 0)
                winnerStats.setRaceWinRate(Float.parseFloat(Utils.formatDecimal((double) winnerStats.getRaceWins() / winnerStats.getRaceLosses())));
            else
                winnerStats.setRaceWinRate(winnerStats.getRaceWins());

            // update loser losses
            loserStats.endedRace();
            loserStats.setRaceLosses(loserStats.getRaceLosses() + 1);
            RaceDB.updateRaceLosses(loserStats.getUUID(), loserStats.getRaceLosses());

            // set loser race win rate (will be > 0, so no need to check)
            loserStats.setRaceWinRate(Float.parseFloat(Utils.formatDecimal((double) loserStats.getRaceWins() / loserStats.getRaceLosses())));

            // load leaderboard if they will have a lb position
            if (scoreWillBeLB(winnerStats.getRaceWins()) ||
                raceLeaderboard.size() < Parkour.getSettingsManager().max_race_leaderboard_size) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        loadLeaderboard();
                    }
                }.runTaskAsynchronously(Parkour.getPlugin());
            }

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

    public void loadLeaderboard() {
        try {

            LinkedHashSet<RaceLBPosition> leaderboard = getLeaderboard();
            leaderboard.clear();

            List<Map<String, String>> scoreResults = DatabaseQueries.getResults(
                    "players",
                    "player_name, race_wins, race_losses",
                    " WHERE race_wins > 0" +
                            " ORDER BY race_wins DESC" +
                            " LIMIT " + Parkour.getSettingsManager().max_race_leaderboard_size);

            outer: for (Map<String, String> scoreResult : scoreResults) {

                // quick loop to make sure there are no duplicates
                for (RaceLBPosition raceLBPosition : leaderboard)
                    if (raceLBPosition.getName().equalsIgnoreCase(scoreResult.get("player_name")))
                        continue outer;

                int wins = Integer.parseInt(scoreResult.get("race_wins"));
                int losses = Integer.parseInt(scoreResult.get("race_losses"));

                // avoid divided by 0 error
                float winRate;
                if (losses > 0)
                    winRate = Float.parseFloat(Utils.formatDecimal((double) wins / losses));
                else
                    winRate = wins;

                leaderboard.add(
                        new RaceLBPosition(
                                scoreResult.get("player_name"),
                                wins,
                                winRate)
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean scoreWillBeLB(int score) {
        int lowestWins = 0;
        // gets lowest score
        for (RaceLBPosition raceLBPosition : raceLeaderboard)
            if (lowestWins == 0 || raceLBPosition.getWins() < lowestWins)
                lowestWins = raceLBPosition.getWins();

        if (lowestWins <= score)
            return true;
        return false;
    }

    public Race get(Player player) {
        for (Race race : runningRaceList)
            if (race.getPlayer1().equals(player) || race.getPlayer2().equals(player))
                return race;

        return null;
    }

    public Set<Race> getRaces() {
        return runningRaceList;
    }

    public LinkedHashSet<RaceLBPosition> getLeaderboard() { return raceLeaderboard; }
}
