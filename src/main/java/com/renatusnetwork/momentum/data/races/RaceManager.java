package com.renatusnetwork.momentum.data.races;

import com.connorlinfoot.titleapi.TitleAPI;
import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.levels.Level;
import com.renatusnetwork.momentum.data.levels.LevelManager;
import com.renatusnetwork.momentum.data.stats.*;
import com.renatusnetwork.momentum.storage.mysql.DatabaseQueries;
import com.renatusnetwork.momentum.utils.Utils;
import com.renatusnetwork.momentum.utils.dependencies.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class RaceManager {

    private Set<Race> runningRaceList = new HashSet<>();
    private Set<RaceRequest> raceRequests = new HashSet<>();
    private HashMap<String, BukkitTask> confirmMap = new HashMap<>();
    private HashMap<Integer, RaceLBPosition> raceLeaderboard = new HashMap<>(Momentum.getSettingsManager().max_race_leaderboard_size);

    public RaceManager() {
        new BukkitRunnable() {
            @Override
            public void run() {
                loadLeaderboard();
            }
        }.runTaskAsynchronously(Momentum.getPlugin());
    }

    /*
        Race Section
     */
    public void startRace(PlayerStats player1, PlayerStats player2, Level selectedLevel, boolean bet, double betAmount) {

        // make sure it is not an invalid level
        if (selectedLevel != null) {
            // create object for the race
            Race newRace = new Race(player1.getPlayer(), player2.getPlayer(), selectedLevel, bet, betAmount);
            runningRaceList.add(newRace);

            StatsManager statsManager = Momentum.getStatsManager();
            // toggle off elytra
            statsManager.toggleOffElytra(player1);
            statsManager.toggleOffElytra(player2);

            player1.startedRace();
            player2.startedRace();
            player1.setLevel(selectedLevel);
            player2.setLevel(selectedLevel);

            player1.getPlayer().teleport(selectedLevel.getRaceLocation1());
            player2.getPlayer().teleport(selectedLevel.getRaceLocation2());

            if (bet)
            {
                statsManager.removeCoins(player1, betAmount);
                statsManager.removeCoins(player2, betAmount);
            }

            // remove potion effects
            player1.clearPotionEffects();
            player2.clearPotionEffects();

            Level finalChosenLevel = selectedLevel; // need to make final for inner class usage

            // freeze and do countdown
            new BukkitRunnable() {
                int runCycles = 0;

                public void run() {

                    if (!runningRaceList.contains(newRace)) {
                        cancel();
                        return;
                    }

                    // cancel, send last title and return
                    if (runCycles == 100) {
                        cancel();
                        TitleAPI.sendTitle(player1.getPlayer(), 0, 20, 10, "&cRACE", "");
                        TitleAPI.sendTitle(player2.getPlayer(), 0, 20, 10, "&cRACE", "");
                        player1.startedLevel();
                        player2.startedLevel();
                        player1.getPlayer().playSound(player1.getPlayer().getLocation(), Sound.BLOCK_NOTE_BELL, 8F, 2F);
                        player2.getPlayer().playSound(player2.getPlayer().getLocation(), Sound.BLOCK_NOTE_BELL, 8F, 2F);
                        return;
                    }

                    // race location variables
                    double race1X = finalChosenLevel.getRaceLocation1().getX();
                    double race1Z = finalChosenLevel.getRaceLocation1().getZ();
                    double race2X = finalChosenLevel.getRaceLocation2().getX();
                    double race2Z = finalChosenLevel.getRaceLocation2().getZ();

                    // player location variables
                    double player1X = player1.getPlayer().getLocation().getX();
                    double player1Z = player1.getPlayer().getLocation().getZ();
                    double player2X = player2.getPlayer().getLocation().getX();
                    double player2Z = player2.getPlayer().getLocation().getZ();

                    // teleport back if moved
                    if (race1X != player1X || race1Z != player1Z) {
                        Location raceLoc1 = finalChosenLevel.getRaceLocation1().clone();
                        raceLoc1.setYaw(player1.getPlayer().getLocation().getYaw());
                        raceLoc1.setPitch(player1.getPlayer().getLocation().getPitch());
                        player1.getPlayer().teleport(raceLoc1);
                    }

                    if (race2X != player2X || race2Z != player2Z) {
                        Location raceLoc2 = finalChosenLevel.getRaceLocation2().clone();
                        raceLoc2.setYaw(player2.getPlayer().getLocation().getYaw());
                        raceLoc2.setPitch(player2.getPlayer().getLocation().getPitch());
                        player2.getPlayer().teleport(raceLoc2);
                    }

                    // countdown if-else
                    if (runCycles == 0) {
                        TitleAPI.sendTitle(player1.getPlayer(), 0, 20, 0, "&a5", "");
                        TitleAPI.sendTitle(player2.getPlayer(), 0, 20, 0, "&a5", "");
                        player1.getPlayer().playSound(player1.getPlayer().getLocation(), Sound.BLOCK_NOTE_HAT, 8F, 2F);
                        player2.getPlayer().playSound(player2.getPlayer().getLocation(), Sound.BLOCK_NOTE_HAT, 8F, 2F);
                    } else if (runCycles == 20) {
                        TitleAPI.sendTitle(player1.getPlayer(), 0, 20, 0, "&e4", "");
                        TitleAPI.sendTitle(player2.getPlayer(), 0, 20, 0, "&e4", "");
                        player1.getPlayer().playSound(player1.getPlayer().getLocation(), Sound.BLOCK_NOTE_HAT, 8F, 2F);
                        player2.getPlayer().playSound(player2.getPlayer().getLocation(), Sound.BLOCK_NOTE_HAT, 8F, 2F);
                    } else if (runCycles == 40) {
                        TitleAPI.sendTitle(player1.getPlayer(), 0, 20, 0, "&63", "");
                        TitleAPI.sendTitle(player2.getPlayer(), 0, 20, 0, "&63", "");
                        player1.getPlayer().playSound(player1.getPlayer().getLocation(), Sound.BLOCK_NOTE_HAT, 8F, 2F);
                        player2.getPlayer().playSound(player2.getPlayer().getLocation(), Sound.BLOCK_NOTE_HAT, 8F, 2F);
                    } else if (runCycles == 60) {
                        TitleAPI.sendTitle(player1.getPlayer(), 0, 20, 0, "&c2", "");
                        TitleAPI.sendTitle(player2.getPlayer(), 0, 20, 0, "&c2", "");
                        player1.getPlayer().playSound(player1.getPlayer().getLocation(), Sound.BLOCK_NOTE_HAT, 8F, 2F);
                        player2.getPlayer().playSound(player2.getPlayer().getLocation(), Sound.BLOCK_NOTE_HAT, 8F, 2F);
                    } else if (runCycles == 80) {
                        TitleAPI.sendTitle(player1.getPlayer(), 0, 20, 0, "&41", "");
                        TitleAPI.sendTitle(player2.getPlayer(), 0, 20, 0, "&41", "");
                        player1.getPlayer().playSound(player1.getPlayer().getLocation(), Sound.BLOCK_NOTE_HAT, 8F, 2F);
                        player2.getPlayer().playSound(player2.getPlayer().getLocation(), Sound.BLOCK_NOTE_HAT, 8F, 2F);
                    }
                    runCycles++;
                }
            }.runTaskTimer(Momentum.getPlugin(), 1, 1);
        } else {
            player1.getPlayer().sendMessage(Utils.translate("&cInvalid level? Try again or contact an Admin"));
            player2.getPlayer().sendMessage(Utils.translate("&cInvalid level? Try again or contact an Admin"));
        }
    }

    public void forceEndRace(Race endedRace, boolean shutdown) {

        StatsManager statsManager = Momentum.getStatsManager();
        Player player1 = endedRace.getPlayer1();
        Player player2 = endedRace.getPlayer2();

        // teleport back
        player1.teleport(endedRace.getOriginalPlayer1Loc());
        player2.teleport(endedRace.getOriginalPlayer2Loc());

        // if server is not shutting down
        if (!shutdown) {
            player1.sendMessage(Utils.translate("&7You ran out of time to complete the race!"));
            player2.sendMessage(Utils.translate("&7You ran out of time to complete the race!"));
            // send title
            String titleString = Utils.translate("&7Ran Out of Time in Your Race");
            TitleAPI.sendTitle(endedRace.getPlayer1(), 10, 60, 10, titleString);
            TitleAPI.sendTitle(endedRace.getPlayer2(), 10, 60, 10, titleString);
        }

        // if has bet, give bet back
        if (endedRace.hasBet())
        {
            PlayerStats player1Stats = statsManager.get(player1);
            PlayerStats player2Stats = statsManager.get(player2);

            statsManager.addCoins(player1Stats, endedRace.getBet());
            statsManager.addCoins(player2Stats, endedRace.getBet());
        }

        // set level in cache and toggle back on elytra
        ProtectedRegion player1Region = WorldGuard.getRegion(endedRace.getPlayer1().getLocation());
        ProtectedRegion player2Region = WorldGuard.getRegion(endedRace.getPlayer2().getLocation());
        if (player1Region != null) {
            Level level = Momentum.getLevelManager().get(player1Region.getId());
            PlayerStats playerStats = statsManager.get(player1);
            playerStats.endedRace();
            playerStats.disableLevelStartTime();
            playerStats.setLevel(level);

            // if elytra level, give elytra
            if (level != null && level.isElytraLevel())
                statsManager.toggleOnElytra(playerStats);
        }

        if (player2Region != null) {
            Level level = Momentum.getLevelManager().get(player2Region.getId());
            PlayerStats playerStats = statsManager.get(endedRace.getPlayer2());
            playerStats.endedRace();
            playerStats.disableLevelStartTime();
            playerStats.setLevel(level);

            // if elytra level, give elytra
            if (level != null && level.isElytraLevel())
                statsManager.toggleOnElytra(playerStats);
        }

        // remove from list
        runningRaceList.remove(endedRace);
    }

    public void endRace(Player winner, boolean disconnected) {

        Race raceObject = get(winner);

        if (raceObject != null) {
            Player loser = raceObject.getOpponent(winner);
            StatsManager statsManager = Momentum.getStatsManager();

            PlayerStats winnerStats = statsManager.get(winner);
            PlayerStats loserStats = statsManager.get(loser);

            // apply completion stats to level
            LevelManager levelManager = Momentum.getLevelManager();

            // get max timer and cancel right away
            raceObject.getMaxTimer().cancel();

            // if they have not completed this individual level, then add
            if (winnerStats.getLevelCompletionsCount(raceObject.getRaceLevel().getName()) < 1)
                winnerStats.setIndividualLevelsBeaten(winnerStats.getIndividualLevelsBeaten() + 1);

            long elapsedTime = (System.currentTimeMillis() - winnerStats.getLevelStartTime());
            if (disconnected)
                elapsedTime = 0;

            LevelCompletion levelCompletion = new LevelCompletion(
                    System.currentTimeMillis(),
                    elapsedTime
            );

            levelCompletion.setPlayerName(winner.getName());
            winnerStats.setTotalLevelCompletions(winnerStats.getTotalLevelCompletions() + 1);
            StatsDB.insertCompletion(winnerStats, raceObject.getRaceLevel(), levelCompletion);
            levelManager.addTotalLevelCompletion();
            raceObject.getRaceLevel().addCompletion(winner.getName(), levelCompletion);

            // Update player information
            winnerStats.levelCompletion(raceObject.getRaceLevel().getName(), levelCompletion);

            if (raceObject.hasBet())
                Bukkit.broadcastMessage(Utils.translate("&4" + winner.getDisplayName() + " &7has beaten &4" + loser.getDisplayName()
                                        + " &7in a race for &6" + Utils.formatNumber(raceObject.getBet()) + " &eCoins &7on " + raceObject.getRaceLevel().getFormattedTitle()));
            else
                Bukkit.broadcastMessage(Utils.translate("&4" + winner.getDisplayName() + " &7has beaten &4" + loser.getDisplayName()
                                        + " &7in a race on " + raceObject.getRaceLevel().getFormattedTitle()));

            // give winner money and take from loser if betted on race
            if (raceObject.hasBet())
                statsManager.addCoins(winnerStats, (raceObject.getBet() * 2));

            // check if winner is player 1, then teleport accordingly, otherwise they are player 2
            if (raceObject.isPlayer1(winner)) {
                winner.teleport(raceObject.getOriginalPlayer1Loc());
                loser.teleport(raceObject.getOriginalPlayer2Loc());
            } else {
                loser.teleport(raceObject.getOriginalPlayer1Loc());
                winner.teleport(raceObject.getOriginalPlayer2Loc());
            }

            // send title to winner and loser
            String titleString = Utils.translate("&c" + winner.getDisplayName() + " &7has won the Race!");
            TitleAPI.sendTitle(winner, 10, 60, 10, titleString);
            TitleAPI.sendTitle(loser, 10, 60, 10, titleString);

            // update winner wins
            winnerStats.endedRace();
            winnerStats.disableLevelStartTime();
            winnerStats.setRaceWins(winnerStats.getRaceWins() + 1);
            RaceDB.updateRaceWins(winnerStats.getUUID(), winnerStats.getRaceWins());

            // set winner race win rate
            if (winnerStats.getRaceLosses() > 0)
                winnerStats.setRaceWinRate(Float.parseFloat(Utils.formatDecimal((double) winnerStats.getRaceWins() / winnerStats.getRaceLosses())));
            else
                winnerStats.setRaceWinRate(winnerStats.getRaceWins());

            // update loser losses
            loserStats.endedRace();
            loserStats.disableLevelStartTime();
            loserStats.setRaceLosses(loserStats.getRaceLosses() + 1);
            RaceDB.updateRaceLosses(loserStats.getUUID(), loserStats.getRaceLosses());

            // set loser race win rate (will be > 0, so no need to check)
            loserStats.setRaceWinRate(Float.parseFloat(Utils.formatDecimal((double) loserStats.getRaceWins() / loserStats.getRaceLosses())));

            // load leaderboard if they will have a lb position
            if (scoreWillBeLB(winnerStats.getRaceWins()) ||
                raceLeaderboard.size() < Momentum.getSettingsManager().max_race_leaderboard_size) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        loadLeaderboard();
                    }
                }.runTaskAsynchronously(Momentum.getPlugin());
            }

            ProtectedRegion winnerRegion = WorldGuard.getRegion(winner.getLocation());
            ProtectedRegion loserRegion = WorldGuard.getRegion(loser.getLocation());
            if (winnerRegion != null) {
                Level winnerLevel = Momentum.getLevelManager().get(winnerRegion.getId());
                winnerStats.setLevel(winnerLevel);

                // if elytra level, give elytra
                if (winnerLevel != null && winnerLevel.isElytraLevel())
                    Momentum.getStatsManager().toggleOnElytra(winnerStats);
            }

            if (loserRegion != null) {
                Level loserLevel = Momentum.getLevelManager().get(loserRegion.getId());
                loserStats.setLevel(loserLevel);

                // if elytra level, give elytra
                if (loserLevel != null && loserLevel.isElytraLevel())
                    Momentum.getStatsManager().toggleOnElytra(loserStats);
            }

            // remove from list
            runningRaceList.remove(raceObject);

            Momentum.getStatsManager().runGGTimer();
        }
    }

    public boolean scoreWillBeLB(int score) {
        int lowestWins = 0;
        // gets lowest score
        for (RaceLBPosition raceLBPosition : raceLeaderboard.values())
            if (lowestWins == 0 || raceLBPosition.getWins() < lowestWins)
                lowestWins = raceLBPosition.getWins();

        return lowestWins <= score;
    }

    public Race get(Player player) {
        for (Race race : runningRaceList)
            if (race.getPlayer1().equals(player) || race.getPlayer2().equals(player))
                return race;

        return null;
    }

    /*
        Race Requests Section
     */
    public void sendRequest(PlayerStats player1, PlayerStats player2, boolean randomLevel, Level selectedLevel, boolean bet, double betAmount) {

        List<String> temporaryLevelList;
        if (randomLevel) {
            temporaryLevelList = getNotInUseRaceLevels();

            // if there are no levels available
            if (temporaryLevelList.isEmpty()) {
                player1.getPlayer().sendMessage(Utils.translate("&cNo maps available for use, try again later"));
                player2.getPlayer().sendMessage(Utils.translate("&cNo maps available for use, try again later"));
                return;
            } else {
                // picks random map
                Random ran = new Random();
                selectedLevel = Momentum.getLevelManager().get(
                        temporaryLevelList.get(ran.nextInt(temporaryLevelList.size())
                        ));
            }
        }

        if (selectedLevel == null) {
            player1.getPlayer().sendMessage(Utils.translate("&cInvalid level? Try again"));
            return;
        }

        // otherwise, put them in and ask them to confirm within 5 seconds
        String senderString;
        String opponentString;

        if (bet) {
            opponentString = Utils.translate(
                    "&4" + player1.getPlayer().getName() + " &7has sent you a race request with bet amount &4$" + Utils.formatNumber(betAmount) + "&7! &a&nClick here to accept the race&r"
            );
            senderString = Utils.translate("&7You sent &4" + player2.getPlayer().getName() + " &7a race request with bet amount &4$" + Utils.formatNumber(betAmount));
        } else {
            opponentString = Utils.translate("&4" + player1.getPlayer().getName() + " &7has sent you a race request&7! &a&nClick here to accept the race&r");
            senderString = Utils.translate("&7You sent &4" + player2.getPlayer().getName() + " &7a race request");
        }

        if (!randomLevel) {
            senderString += Utils.translate(" &7on &c" + selectedLevel.getFormattedTitle());
            opponentString += Utils.translate(" &7on &c" + selectedLevel.getFormattedTitle());
        }

        TextComponent opponentComponent = new TextComponent(TextComponent.fromLegacyText(opponentString));
        opponentComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(Utils.translate("&aClick to accept!"))));
        opponentComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/race accept " + player1.getPlayer().getName()));

        // send made messages
        player1.getPlayer().sendMessage(senderString);
        player2.getPlayer().spigot().sendMessage(opponentComponent); // send clickable

        RaceRequest raceRequest = new RaceRequest(player1, player2);

        // set selected level if not null
        if (selectedLevel != null)
            raceRequest.setSelectedLevel(selectedLevel);

        // set bet if doing bet
        if (bet)
            raceRequest.setBet(betAmount);

        raceRequests.add(raceRequest);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (getRequest(player1.getPlayer(), player2.getPlayer()) != null) {
                    removeRequest(raceRequest);
                    player1.getPlayer().sendMessage(Utils.translate("&4" + player2.getPlayer().getName() + " &cdid not accept your race request in time"));
                }
            }
        }.runTaskLater(Momentum.getPlugin(), 20 * 30);
    }

    public void acceptRequest(PlayerStats player1, PlayerStats player2) {

        RaceRequest raceRequest = getRequest(player1.getPlayer(), player2.getPlayer());

        // request exists
        if (raceRequest != null && !raceRequest.getPlayer1().equals(player1)) {

            boolean doingBet = raceRequest.hasBet();
            double betAmount = raceRequest.getBet();

            if (player1.isSpectating()) {
                player1.getPlayer().sendMessage(Utils.translate("&cYou cannot do this while in spectator"));
                removeRequest(raceRequest);
                return;
            }

            if (player2.isSpectating()) {
                player1.getPlayer().sendMessage(Utils.translate("&cYou cannot do this while they are in spectator"));
                removeRequest(raceRequest);
                return;
            }

            if (player1.inPracticeMode()) {
                player1.getPlayer().sendMessage(Utils.translate("&cYou cannot do this while in practice mode"));
                removeRequest(raceRequest);
                return;
            }

            if (player2.inPracticeMode()) {
                player1.getPlayer().sendMessage(Utils.translate("&cYou cannot do this while they are in practice mode"));
                removeRequest(raceRequest);
                return;
            }

            // if accepting race while in race
            if (player1.inRace()) {
                player1.getPlayer().sendMessage(Utils.translate("&cYou cannot race someone else while in a race"));
                removeRequest(raceRequest);
                return;
            }

            // if accepting race while in race
            if (player2.inRace()) {
                player1.getPlayer().sendMessage(Utils.translate("&cYou cannot race someone else while they are in a race"));
                removeRequest(raceRequest);
                return;
            }

            // if other player is in an elytra level and not on the ground, do not continue
            if (player2.inLevel() && player2.getLevel().isElytraLevel() && !player2.getPlayer().isOnGround()) {
                player1.getPlayer().sendMessage(Utils.translate("&cYou cannot race someone when " + player2.getPlayerName() + " is not on the ground in an elytra level"));
                removeRequest(raceRequest);
                return;
            }

            // make sure they still have enough money for the bet
            double accepterBalance = player1.getCoins();
            double senderBalance = player2.getCoins();
            if (accepterBalance < betAmount) {
                player1.getPlayer().sendMessage(Utils.translate("&7You do not have enough money for this bet!" +
                        " Your Balance &4$" + Utils.formatNumber(senderBalance)));
                removeRequest(raceRequest);
                return;
            }

            if (senderBalance < betAmount) {
                player1.getPlayer().sendMessage(Utils.translate("&c" + player2.getPlayerName() + " &7does not have enough to do this bet" +
                        " - &cTheir Balance &4$" + Utils.formatNumber(senderBalance)));
                removeRequest(raceRequest);
                return;
            }

            // if in elytra and not on the ground, dont send
            if (player1.inLevel() && player1.getLevel().isElytraLevel())
                Momentum.getStatsManager().toggleOffElytra(player1);

            // if in elytra and not on the ground, dont send
            if (player2.inLevel() && player2.getLevel().isElytraLevel())
                Momentum.getStatsManager().toggleOffElytra(player2);

            // otherwise do race and disable any current time on levels
            player1.disableLevelStartTime();
            player2.disableLevelStartTime();

            Level chosenLevel = null;
            if (!raceRequest.randomLevel())
                chosenLevel = raceRequest.getSelectedLevel();

            startRace(player1, player2, chosenLevel, doingBet, betAmount);
            removeRequest(raceRequest);
        } else {
            player1.getPlayer().sendMessage(Utils.translate("&cYou do not have a request from &4" + player2.getPlayerName()));
        }
    }

    public RaceRequest getRequest(Player player1, Player player2) {
        for (RaceRequest raceRequest : raceRequests) {
            String requestPlayer1Name = raceRequest.getPlayer1().getPlayerName();
            String requestPlayer2Name = raceRequest.getPlayer2().getPlayerName();

            if ((requestPlayer1Name.equalsIgnoreCase(player1.getName()) &&
                requestPlayer2Name.equalsIgnoreCase(player2.getName())) ||
               (requestPlayer1Name.equalsIgnoreCase(player2.getName()) &&
                requestPlayer2Name.equalsIgnoreCase(player1.getName())))
                return raceRequest;
        }
        return null;
    }

    public void removeRequest(RaceRequest raceRequest) {
        raceRequests.remove(raceRequest);
    }

    public List<String> getNotInUseRaceLevels() {

        List<String> notInUseRaceLevels = Momentum.getLevelManager().getRaceLevels();

        // get in use race levels, to then remove from total race levels
        for (Race race : getRaces())
            notInUseRaceLevels.remove(race.getRaceLevel().getName());

        return notInUseRaceLevels;
    }

    public Set<Race> getRaces() {
        return runningRaceList;
    }

    public Set<RaceRequest> getRaceRequests() { return raceRequests; }

    public void shutdown() {
        for (Race race : runningRaceList)
            forceEndRace(race, true);
    }

    /*
        Leaderboard Section
     */
    public void loadLeaderboard() {
        try {

            HashMap<Integer, RaceLBPosition> leaderboard = getLeaderboard();
            leaderboard.clear();

            List<Map<String, String>> scoreResults = DatabaseQueries.getResults(
                    "players",
                    "player_name, race_wins, race_losses",
                    " WHERE race_wins > 0" +
                            " ORDER BY race_wins DESC" +
                            " LIMIT " + Momentum.getSettingsManager().max_race_leaderboard_size);

            int leaderboardPos = 1;
            outer: for (Map<String, String> scoreResult : scoreResults) {

                // quick loop to make sure there are no duplicates
                for (RaceLBPosition entry : leaderboard.values())
                    if (entry.getName().equalsIgnoreCase(scoreResult.get("player_name")))
                        continue outer;

                int wins = Integer.parseInt(scoreResult.get("race_wins"));
                int losses = Integer.parseInt(scoreResult.get("race_losses"));

                // avoid divided by 0 error
                float winRate;
                if (losses > 0)
                    winRate = Float.parseFloat(Utils.formatDecimal((double) wins / losses));
                else
                    winRate = wins;

                leaderboard.put(leaderboardPos,
                        new RaceLBPosition(
                                scoreResult.get("player_name"),
                                wins,
                                winRate)
                );
                leaderboardPos++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HashMap<Integer, RaceLBPosition> getLeaderboard() { return raceLeaderboard; }
}
